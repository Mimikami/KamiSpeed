package com.kamispeed.container;

/* compiled from: FakeService.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nFakeService.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FakeService.kt\ncom/speedmaster/container/FakeService\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,153:1\n1#2:154\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/FakeService.class */
public final class FakeService {

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-FakeService";

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.FakeService INSTANCE = new com.kamispeed.container.FakeService();

    @org.jetbrains.annotations.NotNull
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.String, android.app.Service> running = new java.util.concurrent.ConcurrentHashMap<>();

    @org.jetbrains.annotations.NotNull
    private static final java.util.concurrent.atomic.AtomicInteger startId = new java.util.concurrent.atomic.AtomicInteger(0);

    @org.jetbrains.annotations.NotNull
    private static final kotlin.Lazy dummyActivityManager$delegate = kotlin.LazyKt.lazy(new kotlin.jvm.functions.Function0<java.lang.Object>() { // from class: com.kamispeed.container.FakeService$dummyActivityManager$2
        public final java.lang.Object invoke() {
            java.lang.Object obj;
            try {
                java.lang.Class iface = java.lang.Class.forName("android.app.IActivityManager");
                obj = java.lang.reflect.Proxy.newProxyInstance(iface.getClassLoader(), new java.lang.Class[]{iface}, (obj2, method2, objArr2) -> invoke$lambda$0(obj2, method2, objArr2));
            } catch (java.lang.Throwable th) {
                obj = new java.lang.Object();
            }
            return obj;
        }

        private static final java.lang.Object invoke$lambda$0(java.lang.Object obj, java.lang.reflect.Method method, java.lang.Object[] objArr) {
            java.lang.Class<?> returnType = method.getReturnType();
            if (kotlin.jvm.internal.Intrinsics.areEqual(returnType, java.lang.Boolean.TYPE)) {
                return false;
            }
            if (kotlin.jvm.internal.Intrinsics.areEqual(returnType, java.lang.Integer.TYPE)) {
                return 0;
            }
            return kotlin.jvm.internal.Intrinsics.areEqual(returnType, java.lang.Long.TYPE) ? 0L : null;
        }
    });

    private FakeService() {
    }

    private final java.lang.String key(android.content.ComponentName component) {
        return component.getPackageName() + "/" + component.getClassName();
    }

    public final boolean isGuestTarget(@org.jetbrains.annotations.Nullable android.content.Intent intent) {
        android.content.ComponentName component;
        if (intent == null || (component = intent.getComponent()) == null) {
            return false;
        }
        com.kamispeed.container.GuestAppManager guestAppManager = com.kamispeed.container.GuestAppManager.INSTANCE;
        java.lang.String packageName = component.getPackageName();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(packageName, "getPackageName(...)");
        return guestAppManager.peek(packageName) != null;
    }

    @org.jetbrains.annotations.Nullable
    public final android.content.ComponentName start(@org.jetbrains.annotations.NotNull android.content.Intent intent) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(intent, "intent");
        android.content.ComponentName component = intent.getComponent();
        if (component == null) {
            return null;
        }
        try {
            android.app.Service existing = running.get(key(component));
            if (existing != null) {
                existing.onStartCommand(intent, 0, startId.incrementAndGet());
                android.util.Log.i(TAG, "fake service restarted: " + component);
                return component;
            }
            java.lang.String packageName = component.getPackageName();
            com.kamispeed.container.FakeService.GuestEnv env = envFor(packageName);
            if (env == null) {
                return null;
            }
            java.lang.Class serviceClass = java.lang.Class.forName(component.getClassName(), true, env.getClassLoader());
            java.lang.reflect.Constructor constructor = serviceClass.getDeclaredConstructor(new java.lang.Class[0]);
            constructor.setAccessible(true);
            android.app.Service service = (android.app.Service) constructor.newInstance(new java.lang.Object[0]);
            com.kamispeed.container.Reflect.INSTANCE.call(service, "attachBaseContext", env.getApplication().getBaseContext());
            com.kamispeed.container.Reflect.INSTANCE.set(service, "mApplication", env.getApplication());
            com.kamispeed.container.Reflect.INSTANCE.set(service, "mActivityManager", getDummyActivityManager());
            com.kamispeed.container.Reflect.INSTANCE.set(service, "mThread", env.getMainThread());
            com.kamispeed.container.Reflect.INSTANCE.set(service, "mClassName", component.getClassName());
            service.onCreate();
            service.onStartCommand(intent, 0, startId.incrementAndGet());
            running.put(key(component), service);
            android.util.Log.i(TAG, "fake service started: " + component);
            return component;
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "fake service start failed: " + component, t);
            return null;
        }
    }

    public final boolean stop(@org.jetbrains.annotations.NotNull android.content.Intent intent) {
        android.app.Service service;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(intent, "intent");
        android.content.ComponentName component = intent.getComponent();
        if (component == null || (service = running.remove(key(component))) == null) {
            return false;
        }
        try {
            service.onDestroy();
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "fake service onDestroy failed: " + component, t);
        }
        android.util.Log.i(TAG, "fake service stopped: " + component);
        return true;
    }

    public final boolean bind(@org.jetbrains.annotations.NotNull android.content.Intent intent) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(intent, "intent");
        return start(intent) != null;
    }

    /* compiled from: FakeService.kt */
    /* loaded from: classes.jar:com/speedmaster/container/FakeService$GuestEnv.class */
    public static final class GuestEnv {

        @org.jetbrains.annotations.NotNull
        private final java.lang.Object mainThread;

        @org.jetbrains.annotations.NotNull
        private final java.lang.ClassLoader classLoader;

        @org.jetbrains.annotations.NotNull
        private final android.app.Application application;

        public GuestEnv(@org.jetbrains.annotations.NotNull java.lang.Object mainThread, @org.jetbrains.annotations.NotNull java.lang.ClassLoader classLoader, @org.jetbrains.annotations.NotNull android.app.Application application) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(mainThread, "mainThread");
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(classLoader, "classLoader");
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(application, "application");
            this.mainThread = mainThread;
            this.classLoader = classLoader;
            this.application = application;
        }

        @org.jetbrains.annotations.NotNull
        public final java.lang.Object getMainThread() {
            return this.mainThread;
        }

        @org.jetbrains.annotations.NotNull
        public final java.lang.ClassLoader getClassLoader() {
            return this.classLoader;
        }

        @org.jetbrains.annotations.NotNull
        public final android.app.Application getApplication() {
            return this.application;
        }
    }

    @org.jetbrains.annotations.Nullable
    public final com.kamispeed.container.FakeService.GuestEnv envFor(@org.jetbrains.annotations.NotNull java.lang.String packageName) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(packageName, "packageName");
        com.kamispeed.container.GuestApp guest = com.kamispeed.container.GuestAppManager.INSTANCE.peek(packageName);
        if (guest == null) {
            return null;
        }
        try {
            java.lang.Class atClass = java.lang.Class.forName("android.app.ActivityThread");
            java.lang.Object mainThread = com.kamispeed.container.Reflect.INSTANCE.callStatic(atClass, "currentActivityThread", new java.lang.Object[0]);
            if (mainThread == null) {
                return null;
            }
            java.lang.Class compatClass = java.lang.Class.forName("android.content.res.CompatibilityInfo");
            java.lang.Object compat = com.kamispeed.container.Reflect.INSTANCE.callStatic(compatClass, "getDefaultCompatibilityInfo", new java.lang.Object[0]);
            if (compat == null) {
                compat = com.kamispeed.container.Reflect.INSTANCE.getStatic(compatClass, "DEFAULT_COMPATIBILITY_INFO");
            }
            java.lang.Object loadedApk = com.kamispeed.container.Reflect.INSTANCE.call(mainThread, "getPackageInfoNoCheck", guest.getAppInfo(), compat);
            if (loadedApk == null) {
                return null;
            }
            java.lang.Object call = com.kamispeed.container.Reflect.INSTANCE.call(loadedApk, "getClassLoader", new java.lang.Object[0]);
            java.lang.ClassLoader classLoader = call instanceof java.lang.ClassLoader ? (java.lang.ClassLoader) call : null;
            if (classLoader == null) {
                return null;
            }
            java.lang.Object call2 = com.kamispeed.container.Reflect.INSTANCE.call(loadedApk, "makeApplication", false, null);
            android.app.Application application = call2 instanceof android.app.Application ? (android.app.Application) call2 : null;
            if (application == null) {
                return null;
            }
            return new com.kamispeed.container.FakeService.GuestEnv(mainThread, classLoader, application);
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "guestEnv(" + packageName + ") failed", t);
            return null;
        }
    }

    private final java.lang.Object getDummyActivityManager() {
        java.lang.Object value = dummyActivityManager$delegate.getValue();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(value, "getValue(...)");
        return value;
    }
}
