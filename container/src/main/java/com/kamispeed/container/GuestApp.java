package com.kamispeed.container;

/* compiled from: GuestApp.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nGuestApp.kt\nKotlin\n*S Kotlin\n*F\n+ 1 GuestApp.kt\ncom/speedmaster/container/GuestApp\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,201:1\n288#2,2:202\n*S KotlinDebug\n*F\n+ 1 GuestApp.kt\ncom/speedmaster/container/GuestApp\n*L\n99#1:202,2\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/GuestApp.class */
public final class GuestApp {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.GuestApp.Companion Companion = new com.kamispeed.container.GuestApp.Companion(null);

    @org.jetbrains.annotations.NotNull
    private final java.lang.String packageName;

    @org.jetbrains.annotations.NotNull
    private final android.content.pm.ApplicationInfo appInfo;

    @org.jetbrains.annotations.NotNull
    private final java.io.File dataDir;

    @org.jetbrains.annotations.Nullable
    private final java.lang.String launcherActivity;

    @org.jetbrains.annotations.Nullable
    private android.content.Context hostContext;

    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy activityInfoCache$delegate;

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-GuestApp";

    public GuestApp(@org.jetbrains.annotations.NotNull java.lang.String packageName, @org.jetbrains.annotations.NotNull android.content.pm.ApplicationInfo appInfo, @org.jetbrains.annotations.NotNull java.io.File dataDir, @org.jetbrains.annotations.Nullable java.lang.String launcherActivity) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(appInfo, "appInfo");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(dataDir, "dataDir");
        this.packageName = packageName;
        this.appInfo = appInfo;
        this.dataDir = dataDir;
        this.launcherActivity = launcherActivity;
        this.activityInfoCache$delegate = kotlin.LazyKt.lazy(new kotlin.jvm.functions.Function0<java.util.HashMap<java.lang.String, android.content.pm.ActivityInfo>>() { // from class: com.kamispeed.container.GuestApp$activityInfoCache$2
            @org.jetbrains.annotations.NotNull
            @Override
            public final java.util.HashMap<java.lang.String, android.content.pm.ActivityInfo> invoke() {
                android.content.pm.PackageManager manager;
                android.content.pm.ActivityInfo[] activityInfoArr;
                java.util.HashMap map = new java.util.HashMap();
                manager = com.kamispeed.container.GuestApp.this.getPm();
                if (manager == null) {
                    return map;
                }
                try {
                    android.content.pm.PackageInfo pi = manager.getPackageInfo(com.kamispeed.container.GuestApp.this.getPackageName(), 129);
                    if (pi != null && (activityInfoArr = pi.activities) != null) {
                        for (android.content.pm.ActivityInfo activityInfo : activityInfoArr) {
                            java.util.HashMap hashMap = map;
                            java.lang.String str = activityInfo.name;
                            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(str, "name");
                            kotlin.jvm.internal.Intrinsics.checkNotNull(activityInfo);
                            hashMap.put(str, activityInfo);
                        }
                    }
                } catch (java.lang.Throwable t) {
                    android.util.Log.w("SM-GuestApp", "load activities of " + com.kamispeed.container.GuestApp.this.getPackageName() + " failed: " + t.getMessage());
                }
                return map;
            }
        });
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPackageName() {
        return this.packageName;
    }

    @org.jetbrains.annotations.NotNull
    public final android.content.pm.ApplicationInfo getAppInfo() {
        return this.appInfo;
    }

    @org.jetbrains.annotations.NotNull
    public final java.io.File getDataDir() {
        return this.dataDir;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLauncherActivity() {
        return this.launcherActivity;
    }

    @org.jetbrains.annotations.NotNull
    public final java.io.File getFilesDir() {
        return new java.io.File(this.dataDir, "files");
    }

    @org.jetbrains.annotations.NotNull
    public final java.io.File getCacheDir() {
        return new java.io.File(this.dataDir, "cache");
    }

    @org.jetbrains.annotations.NotNull
    public final java.io.File getCodeCacheDir() {
        return new java.io.File(this.dataDir, "code_cache");
    }

    @org.jetbrains.annotations.NotNull
    public final java.io.File getDatabasesDir() {
        return new java.io.File(this.dataDir, "databases");
    }

    @org.jetbrains.annotations.NotNull
    public final java.io.File getSharedPrefsDir() {
        return new java.io.File(this.dataDir, "shared_prefs");
    }

    @org.jetbrains.annotations.NotNull
    public final java.io.File getExternalDir() {
        return new java.io.File(this.dataDir, "external");
    }

    public final void bindHost$container_debug(@org.jetbrains.annotations.NotNull android.content.Context context) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(context, "context");
        this.hostContext = context.getApplicationContext();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final android.content.pm.PackageManager getPm() {
        android.content.Context context = this.hostContext;
        if (context != null) {
            return context.getPackageManager();
        }
        return null;
    }

    private final java.util.Map<java.lang.String, android.content.pm.ActivityInfo> getActivityInfoCache() {
        return (java.util.Map) this.activityInfoCache$delegate.getValue();
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Unexpected missing predecessor for block: B:8:0x0024
        	at jadx.core.dex.visitors.blocks.BlockSplitter.addTempConnectionsForExcHandlers(BlockSplitter.java:275)
        	at jadx.core.dex.visitors.blocks.BlockSplitter.visit(BlockSplitter.java:68)
        */
    @org.jetbrains.annotations.Nullable
    public final android.content.pm.ActivityInfo activityInfo(@org.jetbrains.annotations.NotNull java.lang.String r7) {
        /*
            r6 = this;
            r0 = r7
            java.lang.String r1 = "activityClassName"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r6
            android.content.pm.PackageManager r0 = r0.getPm()
            r1 = r0
            if (r1 != 0) goto L11
        Lf:
            r0 = 0
            return r0
        L11:
            r8 = r0
            r0 = r6
            java.util.Map r0 = r0.getActivityInfoCache()
            r1 = r7
            java.lang.Object r0 = r0.get(r1)
            android.content.pm.ActivityInfo r0 = (android.content.pm.ActivityInfo) r0
            r1 = r0
            if (r1 != 0) goto L5a
        L25:
            r0 = r8
            android.content.ComponentName r1 = new android.content.ComponentName     // Catch: java.lang.Throwable -> L3b
            r2 = r1
            r3 = r6
            java.lang.String r3 = r3.packageName     // Catch: java.lang.Throwable -> L3b
            r4 = r7
            r2.<init>(r3, r4)     // Catch: java.lang.Throwable -> L3b
            r2 = 0
            android.content.pm.ActivityInfo r0 = r0.getActivityInfo(r1, r2)     // Catch: java.lang.Throwable -> L3b
            r11 = r0
            goto L51
        L3b:
            r12 = move-exception
            java.lang.String r0 = "SM-GuestApp"
            r1 = r7
            r2 = r12
            java.lang.String r2 = r2.getMessage()
            java.lang.String r1 = "getActivityInfo(" + r1 + ") failed: " + r2
            int r0 = android.util.Log.w(r0, r1)
            r0 = 0
            r11 = r0
        L51:
            r0 = r11
            r1 = r0
            if (r1 != 0) goto L5a
        L58:
            r0 = 0
            return r0
        L5a:
            r9 = r0
            android.content.pm.ActivityInfo r0 = new android.content.pm.ActivityInfo
            r1 = r0
            r2 = r9
            r1.<init>(r2)
            r10 = r0
            r0 = r10
            r1 = r6
            android.content.pm.ApplicationInfo r1 = r1.appInfo
            r0.applicationInfo = r1
            r0 = r10
            r1 = r6
            java.lang.String r1 = r1.packageName
            r0.packageName = r1
            r0 = r10
            r1 = r7
            r0.name = r1
            r0 = r10
            return r0
        */
        android.content.pm.PackageManager pm = getPm();
        if (pm == null) return null;
        android.content.pm.ActivityInfo cached = (android.content.pm.ActivityInfo) getActivityInfoCache().get(r7);
        if (cached == null) {
            try {
                cached = pm.getActivityInfo(new android.content.ComponentName(this.packageName, r7), 0);
            } catch (java.lang.Throwable t) {
                android.util.Log.w(TAG, "getActivityInfo(" + r7 + ") failed: " + t.getMessage());
                cached = null;
            }
            if (cached == null) return null;
        }
        android.content.pm.ActivityInfo clone = new android.content.pm.ActivityInfo(cached);
        clone.applicationInfo = this.appInfo;
        clone.packageName = this.packageName;
        clone.name = r7;
        return clone;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String queryActivity(@org.jetbrains.annotations.NotNull android.content.Intent intent) {
        java.lang.String str;
        java.lang.Object obj;
        android.content.pm.ActivityInfo activityInfo;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(intent, "intent");
        android.content.pm.PackageManager manager = getPm();
        if (manager == null) {
            return null;
        }
        try {
            java.lang.Iterable queryIntentActivities = manager.queryIntentActivities(intent, 0);
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(queryIntentActivities, "queryIntentActivities(...)");
            java.lang.Iterable $this$firstOrNull$iv = queryIntentActivities;
            java.util.Iterator it = $this$firstOrNull$iv.iterator();
            while (true) {
                if (it.hasNext()) {
                    java.lang.Object element$iv = it.next();
                    android.content.pm.ResolveInfo it2 = (android.content.pm.ResolveInfo) element$iv;
                    android.content.pm.ActivityInfo activityInfo2 = it2.activityInfo;
                    if (kotlin.jvm.internal.Intrinsics.areEqual(activityInfo2 != null ? activityInfo2.packageName : null, this.packageName)) {
                        obj = element$iv;
                        break;
                    }
                } else {
                    obj = null;
                    break;
                }
            }
            android.content.pm.ResolveInfo resolveInfo = (android.content.pm.ResolveInfo) obj;
            str = (resolveInfo == null || (activityInfo = resolveInfo.activityInfo) == null) ? null : activityInfo.name;
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "queryActivity(" + intent + ") failed: " + t.getMessage());
            str = null;
        }
        return str;
    }

    /* compiled from: GuestApp.kt */
    /* loaded from: classes.jar:com/speedmaster/container/GuestApp$Companion.class */
    public static final class Companion {
        public /* synthetic */ Companion(kotlin.jvm.internal.DefaultConstructorMarker $constructor_marker) {
            this();
        }

        private Companion() {
        }
    }
}
