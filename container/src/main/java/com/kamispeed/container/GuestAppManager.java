package com.kamispeed.container;

/* compiled from: GuestApp.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nGuestApp.kt\nKotlin\n*S Kotlin\n*F\n+ 1 GuestApp.kt\ncom/speedmaster/container/GuestAppManager\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,201:1\n1#2:202\n1855#3,2:203\n*S KotlinDebug\n*F\n+ 1 GuestApp.kt\ncom/speedmaster/container/GuestAppManager\n*L\n164#1:203,2\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/GuestAppManager.class */
public final class GuestAppManager {

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-GuestAppMgr";

    @org.jetbrains.annotations.Nullable
    private static android.content.Context host;

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.GuestAppManager INSTANCE = new com.kamispeed.container.GuestAppManager();

    @org.jetbrains.annotations.NotNull
    private static final java.util.HashMap<java.lang.String, com.kamispeed.container.GuestApp> cache = new java.util.HashMap<>();

    @org.jetbrains.annotations.NotNull
    private static final java.lang.Object lock = new java.lang.Object();

    private GuestAppManager() {
    }

    public final void init(@org.jetbrains.annotations.NotNull android.content.Context context) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(context, "context");
        host = context.getApplicationContext();
    }

    @org.jetbrains.annotations.Nullable
    public final com.kamispeed.container.GuestApp getOrCreate(@org.jetbrains.annotations.NotNull android.content.Context hostContext, @org.jetbrains.annotations.NotNull java.lang.String packageName) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hostContext, "hostContext");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        synchronized (lock) {
            com.kamispeed.container.GuestApp it = cache.get(packageName);
            if (it != null) {
                return it;
            }
            com.kamispeed.container.GuestApp created = INSTANCE.create(hostContext, packageName);
            if (created == null) {
                return null;
            }
            cache.put(packageName, created);
            return created;
        }
    }

    @org.jetbrains.annotations.Nullable
    public final com.kamispeed.container.GuestApp peek(@org.jetbrains.annotations.NotNull java.lang.String packageName) {
        com.kamispeed.container.GuestApp guestApp;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        synchronized (lock) {
            guestApp = cache.get(packageName);
        }
        return guestApp;
    }

    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.kamispeed.container.GuestApp> running() {
        java.util.List<com.kamispeed.container.GuestApp> list;
        synchronized (lock) {
            java.util.Collection<com.kamispeed.container.GuestApp> values = cache.values();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(values, "<get-values>(...)");
            list = kotlin.collections.CollectionsKt.toList(values);
        }
        return list;
    }

    public final boolean isGuestPackage(@org.jetbrains.annotations.Nullable java.lang.String pkg) {
        boolean containsKey;
        if (pkg != null) {
            synchronized (lock) {
                containsKey = cache.containsKey(pkg);
            }
            if (containsKey) {
                return true;
            }
        }
        return false;
    }

    public final void remove(@org.jetbrains.annotations.NotNull java.lang.String packageName) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        synchronized (lock) {
            cache.remove(packageName);
        }
    }

    private final com.kamispeed.container.GuestApp create(android.content.Context hostContext, java.lang.String packageName) {
        java.lang.String[] step = {"getApplicationInfo"};
        try {
            android.content.pm.PackageManager pm = hostContext.getPackageManager();
            android.content.pm.ApplicationInfo ai;
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                ai = pm.getApplicationInfo(packageName, android.content.pm.PackageManager.ApplicationInfoFlags.of(128L));
            } else {
                ai = pm.getApplicationInfo(packageName, 128);
            }
            step[0] = "prepareDataDir";
            java.io.File dataDir = new java.io.File(hostContext.getDataDir(), "virtual/" + packageName);
            for (java.lang.String s : new java.lang.String[]{"files", "cache", "code_cache", "no_backup", "databases", "shared_prefs", "external", "de", "ce", "obb"}) {
                new java.io.File(dataDir, s).mkdirs();
            }
            step[0] = "patchApplicationInfo";
            ai.uid = android.os.Process.myUid();
            ai.dataDir = dataDir.getAbsolutePath();
            com.kamispeed.container.Reflect.INSTANCE.set(ai, "deviceProtectedDataDir", new java.io.File(dataDir, "de").getAbsolutePath());
            com.kamispeed.container.Reflect.INSTANCE.set(ai, "credentialProtectedDataDir", new java.io.File(dataDir, "ce").getAbsolutePath());
            step[0] = "done";
            java.lang.String launcher = null;
            try {
                android.content.Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
                if (launchIntent != null && launchIntent.getComponent() != null) {
                    launcher = launchIntent.getComponent().getClassName();
                }
            } catch (java.lang.Throwable th) {
            }
            com.kamispeed.container.GuestApp it = new com.kamispeed.container.GuestApp(packageName, ai, dataDir, launcher);
            it.bindHost$container_debug(hostContext);
            android.util.Log.i(TAG, "guest ready: " + packageName + " (uid=" + ai.uid + ", dataDir=" + ai.dataDir + ", launcher=" + launcher + ")");
            com.kamispeed.container.Diag.INSTANCE.clear();
            return it;
        } catch (java.lang.Throwable t) {
            android.util.Log.e(TAG, "create(" + packageName + ") failed at " + step[0], t);
            com.kamispeed.container.Diag.INSTANCE.fail("create-guest/" + step[0], packageName, t);
            return null;
        }
    }
}
