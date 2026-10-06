package com.kamispeed.container;

/* compiled from: StubRegistry.kt */
/* loaded from: classes.jar:com/speedmaster/container/StubRegistry.class */
public final class StubRegistry {

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-StubRegistry";

    @org.jetbrains.annotations.NotNull
    public static final java.lang.String KEY_TOKEN = "_sm_token_";

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.StubRegistry INSTANCE = new com.kamispeed.container.StubRegistry();

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String[] stubClasses = {"com.kamispeed.container.StubActivity0", "com.kamispeed.container.StubActivity1", "com.kamispeed.container.StubActivity2", "com.kamispeed.container.StubActivity3", "com.kamispeed.container.StubActivity4", "com.kamispeed.container.StubActivity5", "com.kamispeed.container.StubActivity6", "com.kamispeed.container.StubActivity7", "com.kamispeed.container.StubActivity8", "com.kamispeed.container.StubActivity9", "com.kamispeed.container.StubActivity10", "com.kamispeed.container.StubActivity11", "com.kamispeed.container.StubActivity12", "com.kamispeed.container.StubActivity13", "com.kamispeed.container.StubActivity14", "com.kamispeed.container.StubActivity15"};

    @org.jetbrains.annotations.NotNull
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.Long, com.kamispeed.container.StubRecord> records = new java.util.concurrent.ConcurrentHashMap<>();

    @org.jetbrains.annotations.NotNull
    private static final java.util.concurrent.atomic.AtomicLong counter = new java.util.concurrent.atomic.AtomicLong(0);

    @org.jetbrains.annotations.NotNull
    private static java.lang.String hostPackage = "com.kamispeed.app";

    private StubRegistry() {
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String[] getStubClasses() {
        return stubClasses;
    }

    public final void init(@org.jetbrains.annotations.NotNull android.content.Context host) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(host, "host");
        java.lang.String packageName = host.getPackageName();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(packageName, "getPackageName(...)");
        hostPackage = packageName;
    }

    public final boolean isStubComponent(@org.jetbrains.annotations.Nullable android.content.ComponentName component) {
        if (component != null && kotlin.jvm.internal.Intrinsics.areEqual(component.getPackageName(), hostPackage)) {
            java.lang.String[] strArr = stubClasses;
            java.lang.String className = component.getClassName();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(className, "getClassName(...)");
            if (kotlin.collections.ArraysKt.contains(strArr, className)) {
                return true;
            }
        }
        return false;
    }

    public final boolean isStubClassName(@org.jetbrains.annotations.Nullable java.lang.String className) {
        return className != null && kotlin.collections.ArraysKt.contains(stubClasses, className);
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String hostPackageName() {
        return hostPackage;
    }

    @org.jetbrains.annotations.Nullable
    public final com.kamispeed.container.StubRecord byStubName(@org.jetbrains.annotations.Nullable java.lang.String className) {
        if (className == null) {
            return null;
        }
        com.kamispeed.container.StubRecord best = null;
        for (com.kamispeed.container.StubRecord r : records.values()) {
            if (kotlin.jvm.internal.Intrinsics.areEqual(r.getStub().getClassName(), className) && (best == null || r.getToken() > best.getToken())) {
                best = r;
            }
        }
        return best;
    }

    /* JADX WARN: Code restructure failed: missing block: B:4:0x0018, code lost:
    
        if (r0 == null) goto L7;
     */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final android.content.Intent toStubIntent(@org.jetbrains.annotations.NotNull com.kamispeed.container.GuestApp r16, @org.jetbrains.annotations.NotNull android.content.Intent r17) {
        /*
            Method dump skipped, instructions count: 292
            To view this dump add '--comments-level debug' option
        */
        java.lang.String activityClass = r17.getComponent() != null ? r17.getComponent().getClassName() : resolveActivityClass(r16, r17);
        if (activityClass == null) {
            android.util.Log.w(TAG, "cannot resolve activity of " + r17);
            return null;
        }
        long token = counter.incrementAndGet();
        android.content.pm.ActivityInfo info = r16.activityInfo(activityClass);
        int launchMode = info != null ? info.launchMode : android.content.pm.ActivityInfo.LAUNCH_MULTIPLE;
        int idx;
        if (launchMode == android.content.pm.ActivityInfo.LAUNCH_MULTIPLE) {
            idx = (int) (token % stubClasses.length);
        } else {
            idx = java.lang.Math.floorMod(activityClass.hashCode(), stubClasses.length);
        }
        android.content.ComponentName stub = new android.content.ComponentName(hostPackage, stubClasses[idx]);
        android.content.Intent stubIntent = new android.content.Intent(r17);
        stubIntent.setComponent(stub);
        stubIntent.putExtra(KEY_TOKEN, token);
        if (launchMode == android.content.pm.ActivityInfo.LAUNCH_SINGLE_TOP) {
            stubIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (launchMode == android.content.pm.ActivityInfo.LAUNCH_SINGLE_TASK || launchMode == android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            stubIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        records.put(java.lang.Long.valueOf(token), new com.kamispeed.container.StubRecord(
            token, r16, stub, new android.content.Intent(r17), activityClass,
            kotlin.jvm.internal.Intrinsics.areEqual(activityClass, r16.getLauncherActivity()), false));
        android.util.Log.i(TAG, "alloc token=" + token + " stub=" + stub.getClassName() + " -> " + r16.getPackageName() + "/" + activityClass + " (mode=" + launchMode + ")");
        return stubIntent;
    }

    @org.jetbrains.annotations.Nullable
    public final com.kamispeed.container.StubRecord byToken(long token) {
        return records.get(java.lang.Long.valueOf(token));
    }

    @org.jetbrains.annotations.Nullable
    public final android.content.Intent toGuestIntent(@org.jetbrains.annotations.Nullable android.content.Intent stubIntent) {
        com.kamispeed.container.StubRecord rec;
        if (stubIntent == null) {
            return null;
        }
        long token = stubIntent.getLongExtra(KEY_TOKEN, -1L);
        if (token <= 0 || (rec = records.get(java.lang.Long.valueOf(token))) == null) {
            return null;
        }
        android.content.Intent gi = new android.content.Intent(rec.getTargetIntent());
        gi.putExtra(KEY_TOKEN, token);
        if (!rec.isLauncherActivity()) {
            if (kotlin.jvm.internal.Intrinsics.areEqual(gi.getAction(), "android.intent.action.MAIN")) {
                gi.setAction(null);
            }
            gi.removeCategory("android.intent.category.LAUNCHER");
        }
        return gi;
    }

    @org.jetbrains.annotations.Nullable
    public final com.kamispeed.container.StubRecord recordOf(@org.jetbrains.annotations.Nullable android.content.Intent stubIntent) {
        if (stubIntent == null) {
            return null;
        }
        long token = stubIntent.getLongExtra(KEY_TOKEN, -1L);
        if (token > 0) {
            return records.get(java.lang.Long.valueOf(token));
        }
        return null;
    }

    public final void markStarted(long token) {
        com.kamispeed.container.StubRecord stubRecord = records.get(java.lang.Long.valueOf(token));
        if (stubRecord == null) {
            return;
        }
        stubRecord.setStarted(true);
    }

    public final void release(long token) {
        records.remove(java.lang.Long.valueOf(token));
    }

    private final java.lang.String resolveActivityClass(com.kamispeed.container.GuestApp guest, android.content.Intent intent) {
        return guest.queryActivity(intent);
    }
}
