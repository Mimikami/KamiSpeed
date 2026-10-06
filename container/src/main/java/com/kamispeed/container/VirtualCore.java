package com.kamispeed.container;

/* compiled from: VirtualCore.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nVirtualCore.kt\nKotlin\n*S Kotlin\n*F\n+ 1 VirtualCore.kt\ncom/speedmaster/container/VirtualCore\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,179:1\n1549#2:180\n1620#2,3:181\n*S KotlinDebug\n*F\n+ 1 VirtualCore.kt\ncom/speedmaster/container/VirtualCore\n*L\n173#1:180\n173#1:181,3\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/VirtualCore.class */
public final class VirtualCore {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.VirtualCore INSTANCE = new com.kamispeed.container.VirtualCore();

    private static final java.lang.String TAG = "SM-VirtualCore";

    @org.jetbrains.annotations.Nullable
    private static volatile android.content.Context hostContext;
    private static boolean amProxyInstalled;
    private static boolean activityThreadHooked;

    private VirtualCore() {
    }

    @org.jetbrains.annotations.NotNull
    public final android.content.Context getHost() {
        android.content.Context context = hostContext;
        if (context == null) {
            throw new java.lang.IllegalStateException("VirtualCore.attach() not called".toString());
        }
        return context;
    }

    public final boolean getAmProxyInstalled() {
        return amProxyInstalled;
    }

    public final boolean getActivityThreadHooked() {
        return activityThreadHooked;
    }

    public final void attach(@org.jetbrains.annotations.NotNull android.content.Context context) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(context, "context");
        if (hostContext != null) {
            return;
        }
        android.content.Context app = context.getApplicationContext();
        hostContext = app;
        com.kamispeed.container.HiddenApi.INSTANCE.bypass();
        com.kamispeed.container.StubRegistry stubRegistry = com.kamispeed.container.StubRegistry.INSTANCE;
        kotlin.jvm.internal.Intrinsics.checkNotNull(app);
        stubRegistry.init(app);
        com.kamispeed.container.GuestAppManager.INSTANCE.init(app);
        com.kamispeed.container.ContextPackage.INSTANCE.init(app);
        activityThreadHooked = com.kamispeed.container.ActivityThreadHook.INSTANCE.install();
        amProxyInstalled = com.kamispeed.container.AmProxy.INSTANCE.install();
        com.kamispeed.speedhack.Speed.INSTANCE.init();
        android.util.Log.i(TAG, "attached: mH=" + activityThreadHooked + " amProxy=" + amProxyInstalled + " speed=" + com.kamispeed.speedhack.Speed.INSTANCE.getReady());
    }

    public static /* synthetic */ boolean launch$default(com.kamispeed.container.VirtualCore virtualCore, java.lang.String str, double d, boolean z, int i, java.lang.Object obj) {
        if ((i & 2) != 0) {
            d = 1.0d;
        }
        if ((i & 4) != 0) {
            z = true;
        }
        return virtualCore.launch(str, d, z);
    }

    public final boolean launch(@org.jetbrains.annotations.NotNull java.lang.String packageName, double speed, boolean enableSpeed) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        com.kamispeed.container.Diag.INSTANCE.clear();
        android.content.Intent intent = defaultLaunchIntent(packageName);
        if (intent == null) {
            android.util.Log.w(TAG, packageName + " has no launcher activity");
            com.kamispeed.container.Diag.INSTANCE.fail("resolve-intent", packageName, "找不到可启动的 Activity（无 LAUNCHER，且没有 exported 的 Activity）");
            return false;
        }
        return launchIntent(intent, speed, enableSpeed);
    }

    public static /* synthetic */ boolean launchActivity$default(com.kamispeed.container.VirtualCore virtualCore, java.lang.String str, java.lang.String str2, double d, boolean z, kotlin.jvm.functions.Function1 function1, int i, java.lang.Object obj) {
        if ((i & 4) != 0) {
            d = 1.0d;
        }
        if ((i & 8) != 0) {
            z = true;
        }
        if ((i & 16) != 0) {
            function1 = null;
        }
        return virtualCore.launchActivity(str, str2, d, z, function1);
    }

    public final boolean launchActivity(@org.jetbrains.annotations.NotNull java.lang.String packageName, @org.jetbrains.annotations.NotNull java.lang.String activityClass, double speed, boolean enableSpeed, @org.jetbrains.annotations.Nullable kotlin.jvm.functions.Function1<? super android.content.Intent, kotlin.Unit> function1) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(activityClass, "activityClass");
        com.kamispeed.container.Diag.INSTANCE.clear();
        android.content.Intent intent = new android.content.Intent().setComponent(new android.content.ComponentName(packageName, activityClass));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(intent, "setComponent(...)");
        if (function1 != null) {
            function1.invoke(intent);
        }
        return launchIntent(intent, speed, enableSpeed);
    }

    /* JADX WARN: Code restructure failed: missing block: B:4:0x0012, code lost:
    
        if (r0 == null) goto L7;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final boolean launchIntent(android.content.Intent r6, double r7, boolean r9) {
        /*
            Method dump skipped, instructions count: 250
            To view this dump add '--comments-level debug' option
        */
        java.lang.String pkg;
        android.content.ComponentName component = r6.getComponent();
        if (component != null) {
            pkg = component.getPackageName();
        } else {
            pkg = r6.getPackage();
        }
        if (pkg == null) {
            com.kamispeed.container.Diag.INSTANCE.fail("resolve-intent", null, "Intent 既没有 component 也没有 package：" + r6);
            return false;
        }
        com.kamispeed.container.GuestApp guest = com.kamispeed.container.GuestAppManager.INSTANCE.getOrCreate(getHost(), pkg);
        if (guest == null) {
            android.util.Log.e(TAG, "cannot create guest for " + pkg);
            if (com.kamispeed.container.Diag.INSTANCE.getLastError() == null) {
                com.kamispeed.container.Diag.INSTANCE.fail("create-guest", pkg, "GuestAppManager.getOrCreate 返回 null（未记录到异常）");
            }
            return false;
        }
        android.content.Intent stub = com.kamispeed.container.StubRegistry.INSTANCE.toStubIntent(guest, r6);
        if (stub == null) {
            android.util.Log.e(TAG, "cannot allocate stub for " + r6);
            com.kamispeed.container.Diag.INSTANCE.fail("alloc-stub", pkg, "无法为该 Activity 分配 Stub：" + r6);
            return false;
        }
        stub.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            com.kamispeed.speedhack.Speed.INSTANCE.apply(r9 ? r7 : 1.0d, r9);
            getHost().startActivity(stub);
            return true;
        } catch (java.lang.Throwable t) {
            android.util.Log.e(TAG, "startActivity failed", t);
            com.kamispeed.container.Diag.INSTANCE.fail("start-activity", pkg, t);
            return false;
        }
    }

    private final android.content.Intent defaultLaunchIntent(java.lang.String packageName) {
        return com.kamispeed.container.LaunchTargets.INSTANCE.resolveStartIntent(getHost(), packageName);
    }

    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.kamispeed.container.GuestActivityInfo> activitiesOf(@org.jetbrains.annotations.NotNull java.lang.String packageName) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        return com.kamispeed.container.LaunchTargets.INSTANCE.list(getHost(), packageName);
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String preferredActivity(@org.jetbrains.annotations.NotNull java.lang.String packageName) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        return com.kamispeed.container.LaunchTargets.INSTANCE.preferred(getHost(), packageName);
    }

    public final void setPreferredActivity(@org.jetbrains.annotations.NotNull java.lang.String packageName, @org.jetbrains.annotations.Nullable java.lang.String activityClass) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        com.kamispeed.container.LaunchTargets.INSTANCE.setPreferred(getHost(), packageName, activityClass);
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastError() {
        return com.kamispeed.container.Diag.INSTANCE.getLastError();
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastErrorStage() {
        return com.kamispeed.container.Diag.INSTANCE.getLastStage();
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLastErrorReport() {
        return com.kamispeed.container.Diag.INSTANCE.report();
    }

    public final void clearLastError() {
        com.kamispeed.container.Diag.INSTANCE.clear();
    }

    public final double getSpeed() {
        return com.kamispeed.speedhack.Speed.INSTANCE.getScale();
    }

    public final void setSpeed(double v) {
        com.kamispeed.speedhack.Speed.INSTANCE.setScale(kotlin.ranges.RangesKt.coerceIn(v, 1.0d, 20.0d));
    }

    public final boolean getSpeedEnabled() {
        return com.kamispeed.speedhack.Speed.INSTANCE.getEnabled();
    }

    public final void setSpeedEnabled(boolean v) {
        com.kamispeed.speedhack.Speed.INSTANCE.setEnabled(v);
    }

    public final void applySpeed(double scale, boolean enabled) {
        com.kamispeed.speedhack.Speed.INSTANCE.apply(scale, enabled);
    }

    public static /* synthetic */ void applySpeed$default(com.kamispeed.container.VirtualCore virtualCore, double d, boolean z, int i, java.lang.Object obj) {
        if ((i & 2) != 0) {
            z = true;
        }
        virtualCore.applySpeed(d, z);
    }

    public final void resetSpeed() {
        com.kamispeed.speedhack.Speed.INSTANCE.reset();
    }

    public final boolean getEngineReady() {
        return com.kamispeed.speedhack.Speed.INSTANCE.getReady();
    }

    public final int getHookedSlots() {
        return com.kamispeed.speedhack.Speed.INSTANCE.getHookedSlotCount();
    }

    public final boolean isGuestInstalled(@org.jetbrains.annotations.NotNull java.lang.String packageName) {
        boolean z;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        try {
            getHost().getPackageManager().getApplicationInfo(packageName, 0);
            z = true;
        } catch (java.lang.Throwable th) {
            z = false;
        }
        return z;
    }

    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> runningGuests() {
        java.lang.Iterable $this$map$iv = com.kamispeed.container.GuestAppManager.INSTANCE.running();
        java.util.Collection destination$iv$iv = new java.util.ArrayList(kotlin.collections.CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
        for (java.lang.Object item$iv$iv : $this$map$iv) {
            com.kamispeed.container.GuestApp it = (com.kamispeed.container.GuestApp) item$iv$iv;
            destination$iv$iv.add(it.getPackageName());
        }
        return (java.util.List) destination$iv$iv;
    }

    public final void stopGuest(@org.jetbrains.annotations.NotNull java.lang.String packageName) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        com.kamispeed.container.GuestAppManager.INSTANCE.remove(packageName);
    }
}
