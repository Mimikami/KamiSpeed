package com.kamispeed.container;

/* compiled from: LaunchTargets.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nLaunchTargets.kt\nKotlin\n*S Kotlin\n*F\n+ 1 LaunchTargets.kt\ncom/speedmaster/container/LaunchTargets\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,111:1\n1#2:112\n1282#3,2:113\n13309#3,2:115\n*S KotlinDebug\n*F\n+ 1 LaunchTargets.kt\ncom/speedmaster/container/LaunchTargets\n*L\n58#1:113,2\n74#1:115,2\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/LaunchTargets.class */
public final class LaunchTargets {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.LaunchTargets INSTANCE = new com.kamispeed.container.LaunchTargets();

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-LaunchTargets";

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PREF = "speedmaster_launch_target";

    private LaunchTargets() {
    }

    private final android.content.SharedPreferences prefs(android.content.Context ctx) {
        return ctx.getSharedPreferences(PREF, 0);
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String preferred(@org.jetbrains.annotations.NotNull android.content.Context ctx, @org.jetbrains.annotations.NotNull java.lang.String pkg) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(pkg, "pkg");
        return prefs(ctx).getString(pkg, null);
    }

    public final void setPreferred(@org.jetbrains.annotations.NotNull android.content.Context ctx, @org.jetbrains.annotations.NotNull java.lang.String pkg, @org.jetbrains.annotations.Nullable java.lang.String activityClass) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(pkg, "pkg");
        android.content.SharedPreferences.Editor $this$setPreferred_u24lambda_u240 = prefs(ctx).edit();
        java.lang.String str = activityClass;
        if (str == null || str.length() == 0) {
            $this$setPreferred_u24lambda_u240.remove(pkg);
        } else {
            $this$setPreferred_u24lambda_u240.putString(pkg, activityClass);
        }
        $this$setPreferred_u24lambda_u240.apply();
    }

    @org.jetbrains.annotations.Nullable
    public final android.content.Intent resolveStartIntent(@org.jetbrains.annotations.NotNull android.content.Context ctx, @org.jetbrains.annotations.NotNull java.lang.String pkg) {
        android.content.Intent intent;
        android.content.pm.PackageInfo pi;
        android.content.pm.ActivityInfo[] activityInfoArr;
        android.content.pm.ActivityInfo activityInfo;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctx, "ctx");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(pkg, "pkg");
        java.lang.String cls = preferred(ctx, pkg);
        if (cls != null) {
            android.util.Log.i(TAG, "use preferred activity: " + pkg + "/" + cls);
            return new android.content.Intent().setComponent(new android.content.ComponentName(pkg, cls));
        }
        android.content.pm.PackageManager pm = ctx.getPackageManager();
        android.content.Intent it = pm.getLaunchIntentForPackage(pkg);
        if (it != null) {
            return it;
        }
        pi = null;
        try {
            pi = pm.getPackageInfo(pkg, 1);
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.fail("resolve-intent", pkg, t);
            intent = null;
        }
        if (pi == null || (activityInfoArr = pi.activities) == null) {
            return null;
        }
        int i = 0;
        int length = activityInfoArr.length;
        while (true) {
            if (i < length) {
                android.content.pm.ActivityInfo activityInfo2 = activityInfoArr[i];
                if (activityInfo2.exported && activityInfo2.enabled) {
                    activityInfo = activityInfo2;
                    break;
                }
                i++;
            } else {
                activityInfo = null;
                break;
            }
        }
        if (activityInfo == null) {
            return null;
        }
        android.content.pm.ActivityInfo act = activityInfo;
        intent = new android.content.Intent().setComponent(new android.content.ComponentName(pkg, act.name));
        return intent;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(7:1|(2:3|(5:5|6|7|8|(2:10|11)(4:13|(4:15|(2:18|16)|19|20)(1:23)|21|22)))|26|6|7|8|(0)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x00a1, code lost:
    
        r12 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x00a3, code lost:
    
        android.util.Log.w(com.kamispeed.container.LaunchTargets.TAG, "list(" + r8 + ") failed: " + r12.getMessage());
        com.kamispeed.container.Diag.INSTANCE.fail("list-activities", r8, r12);
     */
    /* JADX WARN: Removed duplicated region for block: B:10:0x0046 A[Catch: Throwable -> 0x00a1, TryCatch #0 {Throwable -> 0x00a1, blocks: (B:8:0x0034, B:10:0x0046, B:13:0x004a, B:15:0x0055, B:18:0x0069), top: B:7:0x0034 }] */
    /* JADX WARN: Removed duplicated region for block: B:13:0x004a A[Catch: Throwable -> 0x00a1, TryCatch #0 {Throwable -> 0x00a1, blocks: (B:8:0x0034, B:10:0x0046, B:13:0x004a, B:15:0x0055, B:18:0x0069), top: B:7:0x0034 }] */
    @org.jetbrains.annotations.NotNull
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.util.List<com.kamispeed.container.GuestActivityInfo> list(@org.jetbrains.annotations.NotNull android.content.Context ctx, @org.jetbrains.annotations.NotNull java.lang.String pkg) {
        /*
            Method dump skipped, instructions count: 238
            To view this dump add '--comments-level debug' option
        */
        java.lang.String launcher = null;
        android.content.pm.PackageManager pm = ctx.getPackageManager();
        android.content.Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
        if (launchIntent != null && launchIntent.getComponent() != null) {
            launcher = launchIntent.getComponent().getClassName();
        }
        java.util.ArrayList<com.kamispeed.container.GuestActivityInfo> result = new java.util.ArrayList<>();
        try {
            android.content.pm.PackageInfo pi = pm.getPackageInfo(pkg, 129);
            if (pi != null && pi.activities != null) {
                for (android.content.pm.ActivityInfo info : pi.activities) {
                    result.add(toEntry(pm, info, launcher));
                }
            }
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "list(" + pkg + ") failed: " + t.getMessage());
            com.kamispeed.container.Diag.INSTANCE.fail("list-activities", pkg, t);
        }
        java.util.Comparator<com.kamispeed.container.GuestActivityInfo> byLauncherDesc = (a, b) ->
            kotlin.comparisons.ComparisonsKt.compareValues(Boolean.valueOf(b.isLauncher()), Boolean.valueOf(a.isLauncher()));
        java.util.Comparator<com.kamispeed.container.GuestActivityInfo> byGateDesc = (a, b) -> {
            int c = byLauncherDesc.compare(a, b);
            return c != 0 ? c : kotlin.comparisons.ComparisonsKt.compareValues(Boolean.valueOf(b.getLooksLikeSdkGate()), Boolean.valueOf(a.getLooksLikeSdkGate()));
        };
        java.util.Comparator<com.kamispeed.container.GuestActivityInfo> byClassAsc = (a, b) -> {
            int c = byGateDesc.compare(a, b);
            return c != 0 ? c : kotlin.comparisons.ComparisonsKt.compareValues(a.getClassName(), b.getClassName());
        };
        result.sort(byClassAsc);
        return result;
    }

    private final com.kamispeed.container.GuestActivityInfo toEntry(android.content.pm.PackageManager pm, android.content.pm.ActivityInfo info, java.lang.String launcher) {
        java.lang.String str;
        try {
            str = info.loadLabel(pm).toString();
        } catch (java.lang.Throwable th) {
            str = info.name;
        }
        java.lang.String label = str;
        java.lang.String str2 = info.name;
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(str2, "name");
        java.lang.String lower = str2.toLowerCase(java.util.Locale.ROOT);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(lower, "toLowerCase(...)");
        boolean gate = lower.contains("permission") || lower.contains(".sdk.") || lower.contains("splash") || lower.contains("privacy") || lower.contains("agreement") || lower.contains("welcome");
        java.lang.String str3 = info.name;
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(str3, "name");
        kotlin.jvm.internal.Intrinsics.checkNotNull(label);
        return new com.kamispeed.container.GuestActivityInfo(str3, label, info.exported, info.enabled, kotlin.jvm.internal.Intrinsics.areEqual(info.name, launcher), gate);
    }
}
