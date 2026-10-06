/*
 * elf_util.h - 轻量 ELF GOT/PLT Hook（arm64 / armv7 / x86_64 / x86）
 *
 * 采用 GOT 表项改写方案：不修改目标函数的机器码，只把调用方 GOT 槽位里的
 * 目标地址换成我们的桩函数，因此不需要指令重定位，也不会破坏原函数。
 * 这是免 root 变速器的标准做法（xHook / bhook 同思路）。
 */
#ifndef SPEEDHACK_ELF_UTIL_H
#define SPEEDHACK_ELF_UTIL_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    const char *name;      /* 符号名，如 "clock_gettime" */
    void       *new_func;  /* 替换后的函数地址 */
    void       *old_func;  /* 输出：原始函数地址（首个命中的 GOT 槽） */
    int         hits;      /* 输出：本次扫描被改写的槽位数 */
} hook_sym_t;

/* 对所有已加载模块安装 Hook；返回被改写的槽位总数 */
int elf_hook_all(hook_sym_t *syms, int nsyms);

/* 只对指定模块路径安装 Hook */
int elf_hook_one(const char *module_path, hook_sym_t *syms, int nsyms);

/* 把 /data/app/.../base.apk!/lib/arm64-v8a/libxx.so 之类的名字裁剪成 libxx.so */
void elf_basename(const char *path, char *out, size_t out_len);

#ifdef __cplusplus
}
#endif

#endif /* SPEEDHACK_ELF_UTIL_H */
