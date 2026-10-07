/*
 * speedhack.c —— 免 root 变速核心（时间域扭曲）
 *
 * 原理：
 *   1. 用 GOT Hook 把所有已加载模块（libunity.so / libil2cpp.so / libart.so …）
 *      对 libc 时间函数的调用重定向到本文件里的桩函数；
 *   2. 桩函数把真实时间做线性映射：
 *          virtual = anchor_virt + (real - anchor_real) * scale
 *      实时域（CLOCK_REALTIME / gettimeofday / time）与单调域
 *      （CLOCK_MONOTONIC / CLOCK_BOOTTIME）各自维护一组锚点，互不污染；
 *   3. 睡眠类函数按 scale 反向缩放，让 sleep / nano sleep / 条件变量超时也跟着变速；
 *   4. 后台看门狗线程每 300ms 重扫一次已加载模块，dlopen 之后新加载的 .so 也会被
 *      自动挂上 Hook（Unity 的 libmain.so / libunity.so 是启动后才 dlopen 的）。
 *
 * 因为变速发生在“调用方 GOT 槽”，libc 内部自身的实现完全不动，所以不会崩虚拟机。
 */
#include <jni.h>

#include <android/log.h>
#include <dlfcn.h>
#include <errno.h>
#include <pthread.h>
#include <stdatomic.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#include <poll.h>
#include <sys/epoll.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>

#include "elf_util.h"

#define TAG "SpeedHack"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define NS_PER_SEC  1000000000LL
#define NS_PER_MSEC 1000000LL
#define NS_PER_USEC 1000LL

/* ------------------------------------------------------------------ *
 *  原始函数指针
 * ------------------------------------------------------------------ */
typedef int    (*fn_clock_gettime)(clockid_t, struct timespec *);
typedef int    (*fn_gettimeofday)(struct timeval *, void *);
typedef time_t (*fn_time)(time_t *);
typedef int    (*fn_nanosleep)(const struct timespec *, struct timespec *);
typedef int    (*fn_usleep)(useconds_t);
typedef unsigned int (*fn_sleep)(unsigned int);
typedef int    (*fn_clock_nanosleep)(clockid_t, int, const struct timespec *, struct timespec *);
typedef int    (*fn_cond_timedwait)(pthread_cond_t *, pthread_mutex_t *, const struct timespec *);
typedef int    (*fn_epoll_wait)(int, struct epoll_event *, int, int);
typedef int    (*fn_poll)(struct pollfd *, nfds_t, int);
typedef int    (*fn_select)(int, fd_set *, fd_set *, fd_set *, struct timeval *);
typedef void * (*fn_dlopen)(const char *, int);
typedef void * (*fn_android_dlopen_ext)(const char *, int, const void *);

static fn_clock_gettime        real_clock_gettime;
static fn_gettimeofday         real_gettimeofday;
static fn_time                 real_time;
static fn_nanosleep            real_nanosleep;
static fn_usleep               real_usleep;
static fn_sleep                real_sleep;
static fn_clock_nanosleep      real_clock_nanosleep;
static fn_cond_timedwait       real_cond_timedwait;
static fn_epoll_wait           real_epoll_wait;
static fn_poll                 real_poll;
static fn_select               real_select;
static fn_dlopen               real_dlopen;
static fn_android_dlopen_ext   real_android_dlopen_ext;

/* ------------------------------------------------------------------ *
 *  全局状态
 * ------------------------------------------------------------------ */
static atomic_int  g_enabled      = 0;
static atomic_int  g_scale_milli  = 1000;   /* scale * 1000 */
static atomic_int  g_scale_io     = 0;      /* 是否同时缩放 poll/select/epoll 超时 */
static atomic_int  g_watch_run    = 1;

static volatile int64_t g_mono_anchor_real = 0;
static volatile int64_t g_mono_anchor_virt = 0;
static volatile int64_t g_rt_anchor_real   = 0;
static volatile int64_t g_rt_anchor_virt   = 0;

static int  g_inited = 0;
static unsigned char g_rescan_guard = 0;
static pthread_mutex_t g_init_lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_t g_watchdog_thread;

/* ------------------------------------------------------------------ *
 *  工具
 * ------------------------------------------------------------------ */
static inline double scale_now(void) {
    int m = atomic_load_explicit(&g_scale_milli, memory_order_relaxed);
    if (m <= 0) m = 1;
    return (double) m / 1000.0;
}

/*
 * 生效倍率：引擎关闭时视同 1x。
 * 注意「1x」不是直通真实时间，而是 real + (锚点偏移) —— 偏移是此前加速期间
 * 积累的虚拟时间差。这样任何时刻切换倍率/开关，虚拟时间都**连续、不回跳**：
 * 否则游戏在加速期排好的定时器（加载超时、场景过渡 timer）会落在未来的
 * 虚拟时刻，切回 1x 后要等「偏移量」那么久才触发 —— 表现就是切回正常速度后
 * 场景切换卡加载。
 */
static inline double eff_scale(void) {
    if (!atomic_load_explicit(&g_enabled, memory_order_relaxed)) return 1.0;
    return scale_now();
}

static inline int64_t ts_to_ns(const struct timespec *ts) {
    return (int64_t) ts->tv_sec * NS_PER_SEC + (int64_t) ts->tv_nsec;
}

static inline void ns_to_ts(int64_t ns, struct timespec *ts) {
    if (ns < 0) ns = 0;
    ts->tv_sec  = (time_t) (ns / NS_PER_SEC);
    ts->tv_nsec = (long)   (ns % NS_PER_SEC);
}

static void read_real_ns(int64_t *mono_ns, int64_t *rt_ns) {
    struct timespec ts;
    if (real_clock_gettime != NULL) {
        if (mono_ns && real_clock_gettime(CLOCK_MONOTONIC, &ts) == 0) *mono_ns = ts_to_ns(&ts);
        if (rt_ns   && real_clock_gettime(CLOCK_REALTIME,  &ts) == 0) *rt_ns   = ts_to_ns(&ts);
    }
}

/*
 * 重新锚定：保证切换倍率/开关瞬间，对外呈现的虚拟时间连续、不跳变。
 * 写入顺序 real 在前 virt 在后，读端可能读到“新 real + 旧 virt”，
 * 但两组锚点同源于同一时基，差值在纳秒级，不会产生跳变。
 */
static void reanchor(void) {
    int64_t real_mono = 0, real_rt = 0;
    read_real_ns(&real_mono, &real_rt);
    double s = eff_scale();

    int64_t old_mr = g_mono_anchor_real;
    int64_t old_mv = g_mono_anchor_virt;
    int64_t old_rr = g_rt_anchor_real;
    int64_t old_rv = g_rt_anchor_virt;

    int64_t new_mv = old_mv + (int64_t) ((double) (real_mono - old_mr) * s);
    int64_t new_rv = old_rv + (int64_t) ((double) (real_rt   - old_rr) * s);

    g_mono_anchor_real = real_mono;
    g_mono_anchor_virt = new_mv;
    g_rt_anchor_real   = real_rt;
    g_rt_anchor_virt   = new_rv;
}

static inline int64_t warp_mono(int64_t real_ns) {
    double s = eff_scale();
    int64_t ar = g_mono_anchor_real;
    int64_t av = g_mono_anchor_virt;
    return av + (int64_t) ((double) (real_ns - ar) * s);
}

static inline int64_t warp_rt(int64_t real_ns) {
    double s = eff_scale();
    int64_t ar = g_rt_anchor_real;
    int64_t av = g_rt_anchor_virt;
    return av + (int64_t) ((double) (real_ns - ar) * s);
}

/* 虚拟 → 真实（用于把虚拟绝对超时还原成真实绝对超时） */
static inline int64_t unwarp_rt(int64_t virt_ns) {
    double s = eff_scale();
    int64_t ar = g_rt_anchor_real;
    int64_t av = g_rt_anchor_virt;
    return ar + (int64_t) ((double) (virt_ns - av) / s);
}

static inline int64_t unwarp_mono(int64_t virt_ns) {
    double s = eff_scale();
    int64_t ar = g_mono_anchor_real;
    int64_t av = g_mono_anchor_virt;
    return ar + (int64_t) ((double) (virt_ns - av) / s);
}

static inline int64_t shrink(int64_t ns) {
    if (ns <= 0) return ns;
    double s = scale_now();
    if (s <= 1.0) return ns;
    int64_t out = (int64_t) ((double) ns / s);
    return out > 0 ? out : 1;
}

static int is_passthrough_clock(clockid_t id) {
    switch (id) {
        case CLOCK_THREAD_CPUTIME_ID:
        case CLOCK_PROCESS_CPUTIME_ID:
        case CLOCK_MONOTONIC_RAW:
            return 1;
#ifdef CLOCK_MONOTONIC_RAW_APPROX
        case CLOCK_MONOTONIC_RAW_APPROX:
            return 1;
#endif
        default:
            return 0;
    }
}

static int is_realtime_domain(clockid_t id) {
    switch (id) {
        case CLOCK_REALTIME:
#ifdef CLOCK_REALTIME_COARSE
        case CLOCK_REALTIME_COARSE:
#endif
#ifdef CLOCK_REALTIME_ALARM
        case CLOCK_REALTIME_ALARM:
#endif
            return 1;
        default:
            return 0;
    }
}

/* ------------------------------------------------------------------ *
 *  桩函数
 * ------------------------------------------------------------------ */
static int my_clock_gettime(clockid_t id, struct timespec *tp) {
    int rc = real_clock_gettime(id, tp);
    if (rc != 0 || tp == NULL) return rc;
    if (is_passthrough_clock(id)) return rc;

    int64_t real_ns = ts_to_ns(tp);
    int64_t virt_ns = is_realtime_domain(id) ? warp_rt(real_ns) : warp_mono(real_ns);
    ns_to_ts(virt_ns, tp);
    return 0;
}

static int my_gettimeofday(struct timeval *tv, void *tz) {
    int rc = real_gettimeofday(tv, tz);
    if (rc != 0 || tv == NULL) return rc;

    int64_t real_ns = (int64_t) tv->tv_sec * NS_PER_SEC + (int64_t) tv->tv_usec * NS_PER_USEC;
    int64_t virt_ns = warp_rt(real_ns);
    tv->tv_sec  = (time_t) (virt_ns / NS_PER_SEC);
    tv->tv_usec = (suseconds_t) ((virt_ns % NS_PER_SEC) / NS_PER_USEC);
    return 0;
}

static time_t my_time(time_t *tloc) {
    time_t real = real_time(NULL);
    int64_t virt_ns = warp_rt((int64_t) real * NS_PER_SEC);
    time_t out = (time_t) (virt_ns / NS_PER_SEC);
    if (tloc) *tloc = out;
    return out;
}

static int my_nanosleep(const struct timespec *req, struct timespec *rem) {
    if (req == NULL) return real_nanosleep(req, rem);
    if (!atomic_load_explicit(&g_enabled, memory_order_relaxed) || scale_now() == 1.0) {
        return real_nanosleep(req, rem);
    }
    struct timespec sreq;
    ns_to_ts(shrink(ts_to_ns(req)), &sreq);

    struct timespec srem;
    int rc = real_nanosleep(&sreq, rem ? &srem : NULL);
    if (rc == -1 && errno == EINTR && rem != NULL) {
        double s = scale_now();
        ns_to_ts((int64_t) ((double) ts_to_ns(&srem) * s), rem);
    }
    return rc;
}

static int my_usleep(useconds_t usec) {
    if (!atomic_load_explicit(&g_enabled, memory_order_relaxed) || scale_now() == 1.0) {
        return real_usleep(usec);
    }
    int64_t ns = (int64_t) usec * NS_PER_USEC;
    struct timespec req;
    ns_to_ts(shrink(ns), &req);
    int rc = real_nanosleep(&req, NULL);
    if (rc != 0) return -1;
    return 0;
}

static unsigned int my_sleep(unsigned int seconds) {
    if (!atomic_load_explicit(&g_enabled, memory_order_relaxed) || scale_now() == 1.0) {
        return real_sleep(seconds);
    }
    struct timespec req;
    ns_to_ts(shrink((int64_t) seconds * NS_PER_SEC), &req);
    struct timespec rem;
    if (real_nanosleep(&req, &rem) == 0) return 0;
    return (unsigned int) (rem.tv_sec + (rem.tv_nsec > 0 ? 1 : 0));
}

static int my_clock_nanosleep(clockid_t id, int flags,
                              const struct timespec *req, struct timespec *rem) {
    if (req == NULL) return real_clock_nanosleep(id, flags, req, rem);
    if (is_passthrough_clock(id)) {
        return real_clock_nanosleep(id, flags, req, rem);
    }

    struct timespec sreq;
    if (flags & TIMER_ABSTIME) {
        /* 绝对超时用的是“虚拟时间”，要还原成真实绝对时间再交给内核。
         * 即使 1x / 关闭状态也要走这里：虚拟时间带着累计偏移，直接透传
         * 会让内核按真实时钟等一个落在未来的时刻（场景加载直接卡死）。 */
        int64_t virt = ts_to_ns(req);
        int64_t real = is_realtime_domain(id) ? unwarp_rt(virt) : unwarp_mono(virt);
        ns_to_ts(real, &sreq);
        return real_clock_nanosleep(id, flags, &sreq, NULL);
    }
    ns_to_ts(shrink(ts_to_ns(req)), &sreq);

    struct timespec srem;
    int rc = real_clock_nanosleep(id, flags, &sreq, rem ? &srem : NULL);
    if (rc == EINTR && rem != NULL) {
        ns_to_ts((int64_t) ((double) ts_to_ns(&srem) * scale_now()), rem);
    }
    return rc;
}

/*
 * 条件变量超时是游戏主循环最常见的帧节拍来源（Unity 的 WaitForTargetFPS、
 * Java 的 Object.wait(timeout) 最终都会走到这里），必须还原成真实绝对时间。
 * 同样地，1x/关闭状态也要经过反变换（虚拟时间带累计偏移），否则会按真实
 * 时钟等一个未来的虚拟时刻，表现为加载/动画卡死。
 */
static int my_pthread_cond_timedwait(pthread_cond_t *cond, pthread_mutex_t *mutex,
                                     const struct timespec *abstime) {
    if (abstime == NULL) return real_cond_timedwait(cond, mutex, abstime);
    struct timespec real_abs;
    ns_to_ts(unwarp_rt(ts_to_ns(abstime)), &real_abs);
    return real_cond_timedwait(cond, mutex, &real_abs);
}

static int epoll_scale(int timeout_ms) {
    if (!atomic_load_explicit(&g_scale_io, memory_order_relaxed)) return timeout_ms;
    if (timeout_ms <= 0) return timeout_ms;
    if (!atomic_load_explicit(&g_enabled, memory_order_relaxed)) return timeout_ms;
    int64_t ns = shrink((int64_t) timeout_ms * NS_PER_MSEC);
    int ms = (int) (ns / NS_PER_MSEC);
    return ms > 0 ? ms : 1;
}

static int my_epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout) {
    return real_epoll_wait(epfd, events, maxevents, epoll_scale(timeout));
}

static int my_poll(struct pollfd *fds, nfds_t nfds, int timeout) {
    return real_poll(fds, nfds, epoll_scale(timeout));
}

static int my_select(int nfds, fd_set *r, fd_set *w, fd_set *e, struct timeval *timeout) {
    if (timeout == NULL) return real_select(nfds, r, w, e, timeout);
    struct timeval tv = *timeout;
    int ms = (int) (tv.tv_sec * 1000 + tv.tv_usec / 1000);
    int scaled = epoll_scale(ms);
    tv.tv_sec  = scaled / 1000;
    tv.tv_usec = (scaled % 1000) * 1000;
    return real_select(nfds, r, w, e, &tv);
}

static void rescan_modules(void);

static void *my_dlopen(const char *filename, int flags) {
    void *h = real_dlopen(filename, flags);
    if (h != NULL && atomic_load_explicit(&g_enabled, memory_order_relaxed)) {
        rescan_modules();
    }
    return h;
}

static void *my_android_dlopen_ext(const char *filename, int flags, const void *info) {
    void *h = real_android_dlopen_ext(filename, flags, info);
    if (h != NULL && atomic_load_explicit(&g_enabled, memory_order_relaxed)) {
        rescan_modules();
    }
    return h;
}

/* ------------------------------------------------------------------ *
 *  Hook 表
 * ------------------------------------------------------------------ */
typedef enum {
    H_CLOCK_GETTIME = 0,
    H_GETTIMEOFDAY,
    H_TIME,
    H_NANOSLEEP,
    H_USLEEP,
    H_SLEEP,
    H_CLOCK_NANOSLEEP,
    H_COND_TIMEDWAIT,
    H_EPOLL_WAIT,
    H_POLL,
    H_SELECT,
    H_DLOPEN,
    H_ANDROID_DLOPEN_EXT,
    H_COUNT
} hook_index_t;

static hook_sym_t g_hooks[H_COUNT] = {
    { "clock_gettime",        (void *) my_clock_gettime,        NULL, 0 },
    { "gettimeofday",         (void *) my_gettimeofday,         NULL, 0 },
    { "time",                 (void *) my_time,                 NULL, 0 },
    { "nanosleep",            (void *) my_nanosleep,            NULL, 0 },
    { "usleep",               (void *) my_usleep,               NULL, 0 },
    { "sleep",                (void *) my_sleep,                NULL, 0 },
    { "clock_nanosleep",      (void *) my_clock_nanosleep,      NULL, 0 },
    { "pthread_cond_timedwait", (void *) my_pthread_cond_timedwait, NULL, 0 },
    { "epoll_wait",           (void *) my_epoll_wait,           NULL, 0 },
    { "poll",                 (void *) my_poll,                 NULL, 0 },
    { "select",               (void *) my_select,               NULL, 0 },
    { "dlopen",               (void *) my_dlopen,               NULL, 0 },
    { "android_dlopen_ext",   (void *) my_android_dlopen_ext,   NULL, 0 },
};

/* 全部符号一起挂：新 dlopen 进来的 .so 连 dlopen 本身也要接管 */
static int hook_time_only(void) {
    return elf_hook_all(g_hooks, H_COUNT);
}

static void rescan_modules(void) {
    if (__atomic_test_and_set(&g_rescan_guard, __ATOMIC_ACQ_REL)) {
        return; /* 防重入 */
    }
    hook_time_only();
    __atomic_clear(&g_rescan_guard, __ATOMIC_RELEASE);
}

static void *watchdog_main(void *arg) {
    (void) arg;
    LOGI("watchdog started");
    int last_total = -1;
    while (atomic_load_explicit(&g_watch_run, memory_order_relaxed)) {
        struct timespec ts;
        ts.tv_sec  = 0;
        ts.tv_nsec = 300 * NS_PER_MSEC;
        if (real_nanosleep) real_nanosleep(&ts, NULL);
        else usleep(300 * 1000);
        rescan_modules();
        int total = 0;
        for (int i = 0; i < H_COUNT; i++) total += g_hooks[i].hits;
        if (total != last_total) {
            LOGI("watchdog scan: %d slots hooked", total);
            last_total = total;
        }
    }
    return NULL;
}

/* ------------------------------------------------------------------ *
 *  初始化
 * ------------------------------------------------------------------ */
static void *resolve_sym(const char *name) {
    void *p = dlsym(RTLD_NEXT, name);
    if (p == NULL) p = dlsym(RTLD_DEFAULT, name);
    if (p == NULL) {
        void *libc = dlopen("libc.so", RTLD_NOW | RTLD_NOLOAD);
        if (libc != NULL) p = dlsym(libc, name);
    }
    return p;
}

static int resolve_all(void) {
    real_clock_gettime      = (fn_clock_gettime)      resolve_sym("clock_gettime");
    real_gettimeofday       = (fn_gettimeofday)       resolve_sym("gettimeofday");
    real_time               = (fn_time)               resolve_sym("time");
    real_nanosleep          = (fn_nanosleep)          resolve_sym("nanosleep");
    real_usleep             = (fn_usleep)             resolve_sym("usleep");
    real_sleep              = (fn_sleep)              resolve_sym("sleep");
    real_clock_nanosleep    = (fn_clock_nanosleep)    resolve_sym("clock_nanosleep");
    real_cond_timedwait     = (fn_cond_timedwait)     resolve_sym("pthread_cond_timedwait");
    real_epoll_wait         = (fn_epoll_wait)         resolve_sym("epoll_wait");
    real_poll               = (fn_poll)               resolve_sym("poll");
    real_select             = (fn_select)             resolve_sym("select");
    real_dlopen             = (fn_dlopen)             resolve_sym("dlopen");
    real_android_dlopen_ext = (fn_android_dlopen_ext) resolve_sym("android_dlopen_ext");

    if (real_clock_gettime == NULL) {
        LOGE("failed to resolve clock_gettime");
        return 0;
    }
    if (real_nanosleep == NULL) {
        LOGW("failed to resolve nanosleep, sleep scaling disabled");
    }
    return 1;
}

static int speedhack_start(void) {
    pthread_mutex_lock(&g_init_lock);
    if (g_inited) {
        pthread_mutex_unlock(&g_init_lock);
        return 1;
    }
    if (!resolve_all()) {
        pthread_mutex_unlock(&g_init_lock);
        return 0;
    }

    /* 未启用时也先挂钩，保证 enable 后立即生效 */
    int patched = hook_time_only();

    LOGI("installed, patched slots = %d", patched);

    atomic_store(&g_enabled, 0);
    reanchor();

    g_inited = 1;
    pthread_create(&g_watchdog_thread, NULL, watchdog_main, NULL);
    pthread_mutex_unlock(&g_init_lock);
    return 1;
}

/* ------------------------------------------------------------------ *
 *  JNI 接口
 * ------------------------------------------------------------------ */
JNIEXPORT jboolean JNICALL
Java_com_kamispeed_speedhack_Speed_nativeInit(JNIEnv *env, jclass clazz) {
    (void) env; (void) clazz;
    return speedhack_start() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_kamispeed_speedhack_Speed_nativeSetScale(JNIEnv *env, jclass clazz, jdouble scale) {
    (void) env; (void) clazz;
    if (scale < 0.05) scale = 0.05;
    if (scale > 20.0) scale = 20.0;
    int milli = (int) (scale * 1000.0 + 0.5);
    if (milli < 1) milli = 1;
    /* 关键顺序：先用**旧倍率**把虚拟时间推进到当前时刻（保证连续），
     * 再写入新倍率并重新锚定。顺序反了的话，旧时段会被按新倍率折算，
     * 虚拟时间瞬间回跳，游戏在加速期排好的定时器全部悬在未来 ——
     * 切回 1x 后场景切换会卡在加载里。 */
    reanchor();
    atomic_store(&g_scale_milli, milli);
    reanchor();
    LOGI("scale -> %.3f (offset mono +%lldms rt +%lldms)",
         scale,
         (long long) ((g_mono_anchor_virt - g_mono_anchor_real) / NS_PER_MSEC),
         (long long) ((g_rt_anchor_virt - g_rt_anchor_real) / NS_PER_MSEC));
}

JNIEXPORT jdouble JNICALL
Java_com_kamispeed_speedhack_Speed_nativeGetScale(JNIEnv *env, jclass clazz) {
    (void) env; (void) clazz;
    return (jdouble) scale_now();
}

JNIEXPORT void JNICALL
Java_com_kamispeed_speedhack_Speed_nativeSetEnabled(JNIEnv *env, jclass clazz, jboolean on) {
    (void) env; (void) clazz;
    reanchor();                 /* 先按旧状态推进到当前时刻 */
    atomic_store(&g_enabled, on ? 1 : 0);
    reanchor();                 /* 再以新状态重新锚定 */
    LOGI("enabled -> %d", (int) on);
}

JNIEXPORT jboolean JNICALL
Java_com_kamispeed_speedhack_Speed_nativeIsEnabled(JNIEnv *env, jclass clazz) {
    (void) env; (void) clazz;
    return atomic_load(&g_enabled) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_kamispeed_speedhack_Speed_nativeSetScaleIo(JNIEnv *env, jclass clazz, jboolean on) {
    (void) env; (void) clazz;
    atomic_store(&g_scale_io, on ? 1 : 0);
}

JNIEXPORT jint JNICALL
Java_com_kamispeed_speedhack_Speed_nativeHookCount(JNIEnv *env, jclass clazz) {
    (void) env; (void) clazz;
    int total = 0;
    for (int i = 0; i < H_COUNT; i++) total += g_hooks[i].hits;
    return (jint) total;
}

JNIEXPORT void JNICALL
Java_com_kamispeed_speedhack_Speed_nativeRescan(JNIEnv *env, jclass clazz) {
    (void) env; (void) clazz;
    rescan_modules();
}
