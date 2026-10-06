# KamiSpeed —— 免 root 安卓游戏变速器（1x ~ 20x）

一个完整可编译的 Android Studio 工程：**不需要 root、不需要 Xposed、不需要虚拟机**，
在**自己的进程里**把游戏跑起来，然后 Hook 掉这个进程里所有模块对 libc 时间函数的调用，
实现 1.0x ~ 20.0x 的任意变速。

```
KamiSpeed/
├── speedhack/   ← 变速引擎（C + JNI）：ELF GOT Hook + 时间域线性扭曲
├── container/   ← 免 root 容器：把游戏装进自己进程里跑（ActivityThread + AM 代理）
└── app/         ← UI：应用列表、倍率滑杆、悬浮面板、前台服务
```

---

## 1. 原理（为什么能免 root）

改别人的时间必须有「注入到目标进程」的能力。免 root 的唯一正路是
**把目标 App 拉进自己的进程里运行**（App 虚拟化 / 双开容器技术），
然后在自己进程里 Hook 时间函数 —— 游戏和变速引擎在同一个进程，无需任何提权。

两条链路：

| 层 | 模块 | 做什么 |
|---|---|---|
| 容器 | `container/` | 让游戏跑在本进程：替换 `ActivityThread.mInstrumentation`、拦 `mH` 消息把 Stub `intent/activityInfo` 还原成访客的、用动态代理改写 `IActivityTaskManager` 的 `startActivity` |
| 引擎 | `speedhack/` | 遍历进程内所有已加载 `.so`，改写它们 GOT 表里的时间函数槽位，把时间换成 `virtual = anchor + (real - anchor) * scale` |

### 引擎细节（`speedhack/src/main/cpp/`）

* `elf_util.c`：不依赖 xHook，自己解析 `dl_iterate_phdr` + `PT_DYNAMIC`，
  找 `.rela.plt / .rela.dyn`，匹配符号名后 `mprotect` 改 GOT 槽。
  **只改调用方的 GOT，不动函数机器码**，所以不需要指令重定位，不会把 libc 改坏。
* `speedhack.c`：被接管的时间函数

  | 函数 | 处理 |
  |---|---|
  | `clock_gettime` | 实时域 / 单调域分别线性放大；`*_CPUTIME_ID`、`MONOTONIC_RAW` 原样透传 |
  | `gettimeofday` / `time` | 实时域放大（影响 `System.currentTimeMillis`） |
  | `nanosleep` / `usleep` / `sleep` | 时长 **÷ scale**（加速时睡得更短） |
  | `clock_nanosleep` | `TIMER_ABSTIME` 走「虚拟绝对时间 → 真实绝对时间」反算 |
  | `pthread_cond_timedwait` | 同上反算，这是 Unity 帧节拍/`Object.wait` 的关键路径 |
  | `epoll_wait` / `poll` / `select` | 可选（默认关闭），网络超时也一起缩放 |
  | `dlopen` / `android_dlopen_ext` | 新 `.so` 一加载立刻补挂；另有 300ms 看门狗线程兜底 |

* 因为 `libart.so` 自己的 GOT 也被接管，所以 **Java 层的 `System.nanoTime()`、
  `System.currentTimeMillis()`、`SystemClock.uptimeMillis()` 一起变速** ——
  Unity(il2cpp)、Cocos、UE、原生 Java 游戏全覆盖。

### 容器细节

不改 `ContextImpl`，只改 `ApplicationInfo` 副本里的 `uid / dataDir`
指向 `virtual/<包名>/`，于是 framework 自己 new 出来的 `LoadedApk`、
`ClassLoader`、`Resources`、`ContextImpl` **天然就是访客的**。
这一招兼容性最好，也是不需要自己造 Context 的原因。

---

## 2. 编译

需要 **Android Studio Hedgehog / Iguana 以上**（AGP 8.5.2 + Gradle 8.7 + JDK 17 + NDK 26.1）。

```bash
# 用 Android Studio 直接 Open 本目录即可（首次会自动补 gradle wrapper）
# 或者自己补 wrapper 后命令行构建：
gradle wrapper --gradle-version 8.7
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

首次打开时在 `local.properties` 里确认 SDK 路径：

```properties
sdk.dir=C\:\\Users\\<你>\\AppData\\Local\\Android\\Sdk
```

### 2.1 备用路径：预编译 .so（CMake/Ninja 不可用时）

`speedhack` 默认走 CMake 源码构建。如果你的环境里 **ninja 无法创建子进程**
（受限沙箱、企业安全软件等，表现为 `:speedhack:configureCMakeDebug` 永久卡住、
ninja CPU 占用恒为 0），可以用 NDK 的 clang 直接出产物，再让 Gradle 只做打包：

```bash
pwsh tools/build-native.ps1 -Ndk <NDK 路径>
gradle -PnativePrebuilt=true :app:assembleDebug
```

脚本会按 `CMakeLists.txt` 里同样的编译选项，把
`libspeedhack.so` 生成到 `speedhack/src/main/jniLibs/<abi>/`。
`-PnativePrebuilt=true` 会关闭 `externalNativeBuild`，只使用这些预编译产物。

---

## 3. 使用

1. 安装 APK，打开 KamiSpeed，授予 **「显示在其他应用上层」**（悬浮面板必需）
   和通知权限。首次启动会一次性申请一批运行时权限（网络、存储、电话状态、
   相机等）——**这些是替访客申请的，必须给**，否则游戏在容器里联网/读写会被
   系统按 uid 拒掉。
2. 列表里找到游戏 → 点 **「加速运行」**。
3. **如果进去是白屏或立刻退回**：长按该应用 → 在弹窗里选真正的游戏 Activity
   （见下面 3.1），然后重新点「加速运行」。
4. 屏幕上出现悬浮面板，拖动左侧 `⚡ 变速` 标题移动位置，滑杆在
   **1.0x ~ 20.0x** 之间实时调速；`−` / `+` 每次 0.5x；开关可随时旁路。

### 悬浮面板 / 半圆小球

- **面板 ✕** → 收成**半圆小球**，自动吸附在屏幕左/右边缘（露出一半）；
- 小球可**拖动**（拖动时完整显示，松手重新吸附到最近的边缘），**轻点**展开回面板；
- 悬浮窗**跟随游戏所在的屏幕**（MuMu 等多屏模拟器给每个任务分配独立 display，
  不加处理会出现「变速器页面看得到、游戏里没有」）；
- **KamiSpeed 自己的页面在前台时自动隐藏**，切回游戏自动恢复。
  判断依据是「最前台的本进程任务」（`ActivityManager.getRunningTasks`，
  失败时退回反射 `ActivityTaskManager.getFocusedRootTaskInfo()`），
  因为多屏下每个窗口各自持有焦点，单窗口的 focus 信号不可靠。

### 3.1 长按选择启动 Activity（重要）

很多国内游戏的 `android.intent.category.LAUNCHER` **不是游戏本体**，
而是渠道 SDK 的权限申请页。例如「暴走英雄坛」的入口是：

```
com.maple.madherogo.mumayi/com.u8.sdk.permission.U8PermissionActivity   ← 渠道权限闸门
com.maple.madherogo.mumayi/com.maple.madherogo.AppActivity              ← 真正的 Cocos 游戏
```

这类闸门页在容器里容易因为权限/网络校验卡住而白屏。

**长按应用条目** → 弹出该包内所有 Activity 的列表（标注 `[LAUNCHER]`、
`[疑似SDK闸门]`），选好之后按包名持久化，列表项会显示「启动页：XXX」。

也可以用 adb 直接指定，便于自动化：

```bash
adb shell am start -n com.kamispeed.app/.MainActivity \
    --es launch_pkg com.maple.madherogo.mumayi \
    --es target_activity com.maple.madherogo.AppActivity
```

---

## 4. 代码里怎么调

```kotlin
// 宿主 Application
VirtualCore.attach(this)                       // 装容器 + 加载引擎

// 启动游戏并直接以 5 倍速运行
VirtualCore.launch("com.tencent.tmgp.sgame", speed = 5.0, enableSpeed = true)

// 运行时改倍率（1.0 ~ 20.0 之外会被夹紧）
VirtualCore.applySpeed(12.5, enabled = true)

// 直接操作引擎
Speed.enabled = true
Speed.scale   = 20.0
Speed.hookedSlotCount   // 自检：已被改写的时间调用槽位数
```

`Speed` 的接口（`speedhack/`）：

```kotlin
Speed.init()                 // 加载 so + 安装 GOT Hook
Speed.scale = 3.0            // 1.0 ~ 20.0
Speed.enabled = true
Speed.scaleIoTimeouts = true // 可选：连 epoll_wait/poll/select 超时一起缩放
Speed.rescan()               // 手动补扫模块（一般不用，有看门狗）
```

---

## 5. 已验证 / 已知限制

**支持**

* Android 7.0 (API 24) ~ Android 14 (API 34)，arm64-v8a / armeabi-v7a
* Unity(il2cpp/Mono)、Cocos2d-x、UE4、纯 Java 游戏的时间逻辑加速
* 单进程游戏（绝大多数游戏）
* 游戏内部的 `startActivity` / `startActivityForResult`（AM 代理改写）

**限制（务必先看）**

1. **多进程游戏不行**。声明了 `android:process=":xxx"` 的游戏，其子进程里没有引擎。
   解决办法是把 `:xxx` 声明到宿主 Manifest 并在子进程 `Application.onCreate` 里
   再调一次 `VirtualCore.attach()` + `Speed.init()`。
2. **渲染帧率不会变**。变速改的是游戏逻辑时间（deltaTime），
   屏幕刷新率仍受系统 V-Sync 限制（60/90/120Hz）。这正是所有免 root 变速器的行为。
3. **`PendingIntent` / 通知跳转 / 外部 App 拉起游戏** 无法正确还原到容器里。
4. **签名校验 / 完整性校验**类游戏会在容器里检测到包名或路径异常而拒绝启动。
5. **反外挂**（如带内核驱动的商业反作弊）会检测同一进程内的 GOT 改写，
   本工程不包含任何反检测手段。
6. **倍率越高越容易出问题**：20x 时 ART 的 GC / JIT 计时也被放大，
   个别游戏可能出现音频撕裂、物理穿模、GC 抖动。建议 2x ~ 5x 常驻。
7. 隐藏 API 绕过（`HiddenApi.bypass()`）在部分深度定制 ROM（MIUI / EMUI 某些版本）
   上可能失效，此时 `tvEngine` 里会显示 `mH=FAIL AM代理=FAIL`。

**自检与排错**

启动失败时，APP 会**直接在弹窗里显示真实异常和堆栈**（含被 `Reflect` 吞掉的反射异常），
点「复制」即可粘贴出来，不必接 adb。主界面底部的引擎信息栏会显示上次失败卡在哪一步，
点它也能重新查看详情。

失败阶段名（`Diag.lastStage`）含义：

| 阶段 | 含义 |
|---|---|
| `resolve-intent` | 找不到可启动的 Activity |
| `create-guest/getApplicationInfo` | 拿不到访客的 ApplicationInfo |
| `create-guest/prepareDataDir` | 建不出 `virtual/<pkg>/` 私有目录 |
| `create-guest/patchApplicationInfo` | 改造 uid / dataDir 失败 |
| `hook-activitythread` | 装 `ActivityThread.mH` 拦截失败 |
| `resolve-activityinfo` | 拿不到访客 Activity 的 ActivityInfo |
| `alloc-stub` | 无法为 Activity 分配 Stub |
| `start-activity` | `startActivity(stub)` 抛异常 |
| `patch-message/<what>` | 处理 ActivityThread 消息时抛异常 |

### 架构演进说明（重要）

容器**只做两件事**：

1. 把访客的 `ApplicationInfo` 改造成容器内可用（`uid` = 当前进程，`dataDir` = `virtual/<pkg>/`）；
2. 在 `ActivityThread` 的消息队列里把 `intent` / `activityInfo` 还原成访客的。

`LoadedApk`、`ClassLoader`、`Resources`、`Application` 的创建**全部交给 framework 自己**。
早期版本曾用反射去调 `ActivityThread.getPackageInfoNoCheck` / `LoadedApk.getResources` /
`ContextImpl.createAppContext` 来自建这些对象，结果是：这几个隐藏 API 的**签名随
Android 版本变化，还可能被 ROM 拦**，在真实设备上连续踩坑。改成「只改
ApplicationInfo，剩下交给系统」之后，这条路才是版本无关的。

### 真机上踩到并修掉的坑（Android 15 / MuMu，Cocos2d-x 游戏）

这些都是拿 `adb logcat` 逐层定位出来的，值得记下来：

| # | 现象 | 根因 | 修法 |
|---|---|---|---|
| 1 | 访客 Volley 报 `SecurityException: missing INTERNET permission?`，游戏白屏 | **宿主没声明 `INTERNET`**。系统按 **uid** 判权限，宿主没有的权限访客在容器里一律没有 | 宿主 Manifest 补齐访客常用权限（网络 / 存储 / 电话状态 / 相机 …） |
| 2 | `LoadedApk.getResources(ActivityThread)` 反射返回 null | Android 15 上该 1 参签名已不存在 | 不再自建 LoadedApk，交给 framework |
| 3 | 游戏一启动就 `[Workaround] Ignore the activity started from icon!` 然后 `finish()` | Cocos 的 `isTaskRoot() && CATEGORY_LAUNCHER && ACTION_MAIN` 保护：Stub 与宿主 Activity 同 task → 不是 task root；且还原的 Intent 带着 LAUNCHER 标记 | ①Stub 单独 `taskAffinity`，使其成为 task root；②非 LAUNCHER 的内部页面还原时剥掉 `ACTION_MAIN`/`CATEGORY_LAUNCHER` |
| 4 | `SecurityException: Given caller package <访客包> is not running in process <宿主进程>`，崩在 `Cocos2dxHelper.registerBatteryLevelReceiver` | `ContextImpl.mBasePackageName` 是访客包名，`registerReceiver` 把它当 caller 上报，系统校验「包属于本进程 uid」失败 | `ContextPackage.scrub`：把 ContextImpl 上等于访客包名的 String 字段改成宿主包名 |
| 5 | 访客热更新报 `CLEARTEXT communication … not permitted by network security policy`（`http://cdn.…/upgrade.json`） | 国内游戏热更新 CDN 走明文 HTTP；容器里生效的是**宿主**的网络安全策略 | 宿主 Manifest `android:usesCleartextTraffic="true"` |
| 6 | `IActivityTaskManager.startActivity` 报 `Permission Denial: package=<访客包> does not belong to uid=<宿主 uid>`（U8 渠道 SDK 一调就中） | `startActivity` 的第一个参数就是 `String callingPackage`，访客填的是自己的包名 | `AmProxy` 同时改写 String 参数（访客包名 → 宿主包名） |
| 7 | 输入密码即闪退：`PasswordTransformationMethod` → `Settings.System.getInt` → `SettingsProvider` 抛 `Package <访客包> does not belong to <宿主 uid>`；U8 SDK 读 `android_id` 失败，`mobileinfo=jsonerror`，登录被拒 | Android 15 的 `ContextImpl.getAttributionSource()` 即时计算且取访客包名，无法改字段；`ContentResolver` 归因随访客 | 访客的 `ContentResolver` 直接换用**宿主的**（`HostContextWrapper.getContentResolver()`），所有 provider 调用的归因都是宿主包名 |
| 8 | 游戏登录后/重复拉起时 SIGSEGV：`v8::HandleScope::Initialize` ← `se::ScriptEngine::init`（`libcocos2djs.so`） | 游戏 AppActivity 声明 `launchMode=singleTask`，原生环境重启走 `onNewIntent` 复用实例；容器里 Stub 是 standard + 轮换分配 → 每次新建实例 → V8 引擎二次初始化崩溃 | Stub 扩到 16 个；singleTop/singleTask/singleInstance 的 Activity **按类名固定映射同一个 Stub**，并加 `SINGLE_TOP`/`CLEAR_TOP` 标志，让系统复用栈内实例 |
| 9 | 官服版登录成功后卡死：`SecurityException: Not allowed to start service Intent { cmp=<访客包>/com.tfy.sdk.game.service.LongRunningService } not exported from uid <访客 uid>`，SDK 登录成功回调中断 | 访客 `startService` 目标是**属于另一个 uid 的包**的服务，系统直接拒绝 —— 容器必须自己托管访客的 Service / 自广播 | `FakeService`：拦截 `IActivityManager.startService/stopService/bindService`，目标为访客包时用访客 ClassLoader 实例化服务并在**本进程**直接跑 onCreate/onStartCommand/onDestroy；`FakeReceiver`：显式指向访客包的自广播同样在本进程内投递 onReceive |
| 10 | 官服版**仍然**「提示登录成功但进不了游戏」：游戏靠 `AppActivity` 注册的动态接收器（filter 含 `login_success`）接收 SDK 的登录结果广播，但广播从没送达 | 三连环：① 本机系统广播队列被 `android.process.acore` 的 INFINITE_DEFER 卡死（MuMu Android 15 系统缺陷），所有广播排队不派发、数小时后丢弃；② `Intent.prepareToLeaveProcess()` 会给「无 component 无 package」的广播自动补上 `getBasePackageName()`，而访客的 basePackageName 被我们洗成了宿主包名，于是广播被识别成「发往宿主的」；③ 进程内 `ReceiverDispatcher` 没有 filter 字段（filter 只存在系统侧 BroadcastFilter），无法本地匹配 | `FakeReceiver` 在 `IActivityManager.broadcastIntent` 处拦截访客自广播（含被宿主戳标记的），**绕过系统队列在本进程内直接投递**：经 `HostContextWrapper.registerReceiver` 登记 (receiver→filter+context) 映射，从访客 `LoadedApk.mReceivers` 枚举 ReceiverDispatcher，按 action 精确匹配后主线程 `onReceive`；被拦截的广播不再交给系统，避免队列恢复后二次投递 |
| 11 | 切回 1x 后场景切换卡加载 | `nativeSetScale` 先存新倍率再用 `scale_now()`（=新倍率）推进锚点：旧时段被按新倍率折算，虚拟时间瞬间**回跳**（10x 跑 30s 后切 1x 会回跳 270s）；游戏在加速期排好的定时器/加载超时全部悬在未来，要等「偏移量」那么久才触发 —— 表现就是加载卡死 | 时间连续性不变量：① 切倍率先用**旧倍率**推进锚点、再存新倍率；② 1x/关闭状态不再直通真实时间，而是 `real + 累计偏移`（速率正常、时间连续）；③ 绝对超时（`cond_timedwait`/`clock_nanosleep(ABSTIME)`）的反变换同样偏移感知。副作用：加速期间积累的墙钟偏移会保留到进程重启（游戏以服务器时间为准，无实际影响） |

注意第 4/7 条只改「**对系统说话时用的身份**」：`ContextImpl.getPackageName()` 走的是
`mPackageInfo`，`HostContextWrapper.getPackageName()` 又把它换回访客包名 ——
所以访客自己看到的包名依然是它自己。

### 组件支持边界

已支持：Activity（Stub 虚拟化 + launchMode 语义）、Service（进程内假服务）、
显式/隐式自广播（进程内投递，含动态接收器）、动态广播注册、系统服务归因、
ContentResolver。
暂不支持（SDK 多数自带容错，遇到再补）：访客自己的 ContentProvider（TapTap/穿山甲
等配置型 provider）、多进程组件（如穿山甲 `IndependentProcessDownloadService`）。


```bash
adb logcat -s SpeedHack SM-ATHook SM-AmProxy SM-StubRegistry SM-GuestAppMgr SM-VirtualCore KamiSpeed
```

* 面板能出来但游戏没加速 → 看 `SpeedHack: installed, patched slots = N`，N 太小说明
  引擎加载太早（游戏 so 还没 dlopen），看门狗会在 300ms 内补上。
* 点「加速运行」后是白屏/直接退出 → 看 `SM-StubRegistry` 有没有 `cannot resolve activity`。
* 进游戏后立刻跳回桌面 → 该游戏有签名/路径校验，属于限制 4。

---

## 6. 目录速查

| 路径 | 作用 |
|---|---|
| `speedhack/src/main/cpp/speedhack.c` | 时间函数桩 + 变速锚点算法 + 看门狗 |
| `speedhack/src/main/cpp/elf_util.c` | ELF / GOT Hook 实现 |
| `speedhack/src/main/java/com/kamispeed/speedhack/Speed.kt` | 引擎 Kotlin 门面（1~20 夹紧） |
| `container/src/main/java/com/kamispeed/container/VirtualCore.kt` | 容器对外 API |
| `container/.../ActivityThreadHook.kt` | `mH` 消息拦截、还原 intent/activityInfo |
| `container/.../AmProxy.kt` | `IActivityTaskManager` 动态代理 |
| `container/.../GuestApp.kt` | 访客 Application / ClassLoader / 数据目录 |
| `container/.../HiddenApi.kt` | 隐藏 API 白名单绕过 |
| `app/src/main/java/com/kamispeed/app/MainActivity.kt` | 主界面 |
| `app/src/main/java/com/kamispeed/app/FloatPanel.kt` | 悬浮变速面板 |
| `app/src/main/java/com/kamispeed/app/SpeedService.kt` | 前台服务（保活 + 托管面板） |
