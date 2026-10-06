package com.kamispeed.container;

/* compiled from: FakeReceiver.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nFakeReceiver.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FakeReceiver.kt\ncom/speedmaster/container/FakeReceiver\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 4 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,217:1\n1#2:218\n1#2:232\n1747#3,3:219\n11383#4,9:222\n13309#4:231\n13310#4:233\n11392#4:234\n*S KotlinDebug\n*F\n+ 1 FakeReceiver.kt\ncom/speedmaster/container/FakeReceiver\n*L\n210#1:232\n119#1:219,3\n210#1:222,9\n210#1:231\n210#1:233\n210#1:234\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/FakeReceiver.class */
public final class FakeReceiver {

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-FakeReceiver";

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.FakeReceiver INSTANCE = new com.kamispeed.container.FakeReceiver();

    @org.jetbrains.annotations.NotNull
    private static final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    @org.jetbrains.annotations.NotNull
    private static final java.util.WeakHashMap<android.content.BroadcastReceiver, com.kamispeed.container.FakeReceiver.Reg> filterRegistry = new java.util.WeakHashMap<>();

    private FakeReceiver() {
    }

    public final boolean shouldHandle(@org.jetbrains.annotations.Nullable android.content.Intent intent) {
        if (intent == null) {
            return false;
        }
        android.content.ComponentName it = intent.getComponent();
        if (it != null) {
            com.kamispeed.container.GuestAppManager guestAppManager = com.kamispeed.container.GuestAppManager.INSTANCE;
            java.lang.String packageName = it.getPackageName();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(packageName, "getPackageName(...)");
            return guestAppManager.peek(packageName) != null;
        }
        java.lang.String pkg = intent.getPackage();
        if (pkg == null) {
            return intent.getAction() != null && hasLocalListener(intent.getAction());
        }
        if (com.kamispeed.container.GuestAppManager.INSTANCE.peek(pkg) != null) {
            return true;
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(pkg, com.kamispeed.container.StubRegistry.INSTANCE.hostPackageName()) && intent.getAction() != null) {
            return hasLocalListener(intent.getAction());
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: FakeReceiver.kt */
    /* loaded from: classes.jar:com/speedmaster/container/FakeReceiver$Reg.class */
    public static final class Reg {

        @org.jetbrains.annotations.NotNull
        private final android.content.IntentFilter filter;

        @org.jetbrains.annotations.NotNull
        private final android.content.Context context;

        public Reg(@org.jetbrains.annotations.NotNull android.content.IntentFilter filter, @org.jetbrains.annotations.NotNull android.content.Context context) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(filter, "filter");
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(context, "context");
            this.filter = filter;
            this.context = context;
        }

        @org.jetbrains.annotations.NotNull
        public final android.content.IntentFilter getFilter() {
            return this.filter;
        }

        @org.jetbrains.annotations.NotNull
        public final android.content.Context getContext() {
            return this.context;
        }
    }

    public static /* synthetic */ void recordReceiver$default(com.kamispeed.container.FakeReceiver fakeReceiver, android.content.BroadcastReceiver broadcastReceiver, android.content.IntentFilter intentFilter, android.content.Context context, int i, java.lang.Object obj) {
        if ((i & 4) != 0) {
            context = null;
        }
        fakeReceiver.recordReceiver(broadcastReceiver, intentFilter, context);
    }

    public final void recordReceiver(@org.jetbrains.annotations.Nullable android.content.BroadcastReceiver receiver, @org.jetbrains.annotations.Nullable android.content.IntentFilter filter, @org.jetbrains.annotations.Nullable android.content.Context context) {
        if (receiver == null || filter == null || context == null) {
            return;
        }
        synchronized (filterRegistry) {
            filterRegistry.put(receiver, new com.kamispeed.container.FakeReceiver.Reg(filter, context));
            kotlin.Unit unit = kotlin.Unit.INSTANCE;
        }
    }

    public final void deliverAsync(@org.jetbrains.annotations.NotNull android.content.Intent intent) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(intent, "intent");
        android.content.Intent copy = new android.content.Intent(intent);
        mainHandler.post(() -> {
            deliverAsync$lambda$2(copy);
        });
    }

    private static final void deliverAsync$lambda$2(android.content.Intent $copy) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter($copy, "$copy");
        INSTANCE.dispatch($copy);
    }

    /* JADX WARN: Code restructure failed: missing block: B:8:0x001d, code lost:
    
        if (r0 == null) goto L11;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final boolean dispatch(android.content.Intent r6) {
        /*
            Method dump skipped, instructions count: 273
            To view this dump add '--comments-level debug' option
        */
        try {
            boolean delivered = deliverToLocalReceivers(r6);
            java.lang.String guestPackage = r6.getComponent() != null ? r6.getComponent().getPackageName() : r6.getPackage();
            if (guestPackage != null && com.kamispeed.container.GuestAppManager.INSTANCE.peek(guestPackage) != null) {
                com.kamispeed.container.FakeService.GuestEnv env = com.kamispeed.container.FakeService.INSTANCE.envFor(guestPackage);
                if (env != null) {
                    java.lang.String explicitClass = r6.getComponent() != null ? r6.getComponent().getClassName() : null;
                    java.util.List<java.lang.String> receivers;
                    if (explicitClass != null) {
                        receivers = java.util.Collections.singletonList(explicitClass);
                    } else {
                        receivers = manifestReceiverNames(guestPackage, env);
                    }
                    for (java.lang.String name : receivers) {
                        try {
                            java.lang.Class<?> cls = java.lang.Class.forName(name, true, env.getClassLoader());
                            android.content.BroadcastReceiver receiver = (android.content.BroadcastReceiver) cls.getDeclaredConstructor().newInstance();
                            receiver.onReceive(env.getApplication().getBaseContext(), r6);
                            delivered = true;
                            android.util.Log.i(TAG, "manifest receiver: " + guestPackage + "/" + name + " (" + r6.getAction() + ")");
                        } catch (java.lang.Throwable t) {
                            android.util.Log.w(TAG, "receiver " + name + " dispatch failed: " + t);
                        }
                    }
                }
            }
            return delivered;
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "broadcast dispatch failed: " + r6.getAction(), t);
            return false;
        }
    }

    private final boolean hasLocalListener(java.lang.String action) {
        boolean z;
        if (action == null) {
            return false;
        }
        java.util.List dispatchers = allDispatchers();
        java.util.List $this$any$iv = dispatchers;
        if (!($this$any$iv instanceof java.util.Collection) || !$this$any$iv.isEmpty()) {
            java.util.Iterator it = $this$any$iv.iterator();
            while (true) {
                if (it.hasNext()) {
                    java.lang.Object element$iv = it.next();
                    if (INSTANCE.dispatcherMatches(element$iv, action)) {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
        } else {
            z = false;
        }
        boolean result = z;
        android.util.Log.i(TAG, "hasLocalListener(" + action + ") = " + result + " (dispatchers=" + dispatchers.size() + ")");
        return result;
    }

    private final boolean deliverToLocalReceivers(android.content.Intent intent) {
        com.kamispeed.container.FakeReceiver.Reg reg;
        boolean delivered = false;
        java.util.List dispatchers = allDispatchers();
        for (java.lang.Object dispatcher : dispatchers) {
            try {
                java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(dispatcher, "mReceiver");
                android.content.BroadcastReceiver broadcastReceiver = obj instanceof android.content.BroadcastReceiver ? (android.content.BroadcastReceiver) obj : null;
                if (broadcastReceiver != null) {
                    android.content.BroadcastReceiver receiver = broadcastReceiver;
                    java.lang.Object obj2 = com.kamispeed.container.Reflect.INSTANCE.get(dispatcher, "mForgotten");
                    java.lang.Boolean bool = obj2 instanceof java.lang.Boolean ? (java.lang.Boolean) obj2 : null;
                    boolean forgotten = bool != null ? bool.booleanValue() : false;
                    if (forgotten) {
                        android.util.Log.i(TAG, "skip forgotten receiver " + receiver.getClass().getName());
                    } else {
                        synchronized (filterRegistry) {
                            reg = filterRegistry.get(receiver);
                        }
                        if (reg == null) {
                            android.util.Log.i(TAG, "no registry for " + receiver.getClass().getName());
                        } else if (reg.getFilter().matchAction(intent.getAction())) {
                            receiver.onReceive(reg.getContext(), intent);
                            delivered = true;
                            android.util.Log.i(TAG, "dynamic receiver: " + receiver.getClass().getName() + " (" + intent.getAction() + ")");
                        }
                    }
                }
            } catch (java.lang.Throwable t) {
                android.util.Log.w(TAG, "deliver(" + intent.getAction() + ") to " + dispatcher.getClass().getName() + " failed", t);
            }
        }
        android.util.Log.i(TAG, "deliver(" + intent.getAction() + ") delivered=" + delivered + " (dispatchers=" + dispatchers.size() + ")");
        return delivered;
    }

    private final boolean dispatcherMatches(java.lang.Object dispatcher, java.lang.String action) {
        boolean z = false;
        android.content.BroadcastReceiver broadcastReceiver = null;
        com.kamispeed.container.FakeReceiver.Reg reg;
        try {
            java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(dispatcher, "mReceiver");
            broadcastReceiver = obj instanceof android.content.BroadcastReceiver ? (android.content.BroadcastReceiver) obj : null;
        } catch (java.lang.Throwable th) {
            z = false;
        }
        if (broadcastReceiver == null) {
            return false;
        }
        android.content.BroadcastReceiver receiver = broadcastReceiver;
        java.lang.Object obj2 = com.kamispeed.container.Reflect.INSTANCE.get(dispatcher, "mForgotten");
        java.lang.Boolean bool = obj2 instanceof java.lang.Boolean ? (java.lang.Boolean) obj2 : null;
        boolean forgotten = bool != null ? bool.booleanValue() : false;
        if (forgotten || action == null) {
            return false;
        }
        synchronized (filterRegistry) {
            reg = filterRegistry.get(receiver);
        }
        if (reg == null) {
            return false;
        }
        z = reg.getFilter().matchAction(action);
        return z;
    }

    private final java.util.List<java.lang.Object> allDispatchers() {
        java.util.ArrayList<java.lang.Object> result = new java.util.ArrayList<>();
        try {
            java.lang.Object mainThread = currentActivityThread();
            if (mainThread == null) {
                return result;
            }
            java.lang.Class compatClass = java.lang.Class.forName("android.content.res.CompatibilityInfo");
            java.lang.Object compat = com.kamispeed.container.Reflect.INSTANCE.callStatic(compatClass, "getDefaultCompatibilityInfo", new java.lang.Object[0]);
            if (compat == null) {
                compat = com.kamispeed.container.Reflect.INSTANCE.getStatic(compatClass, "DEFAULT_COMPATIBILITY_INFO");
            }
            for (com.kamispeed.container.GuestApp guest : com.kamispeed.container.GuestAppManager.INSTANCE.running()) {
                try {
                    java.lang.Object loadedApk = com.kamispeed.container.Reflect.INSTANCE.call(mainThread, "getPackageInfoNoCheck", guest.getAppInfo(), compat);
                    if (loadedApk == null) continue;
                    java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(loadedApk, "mReceivers");
                    java.util.Map map = obj instanceof java.util.Map ? (java.util.Map) obj : null;
                    if (map == null) continue;
                    for (java.lang.Object inner : map.values()) {
                        java.util.Map m = inner instanceof java.util.Map ? (java.util.Map) inner : null;
                        if (m != null) {
                            result.addAll(kotlin.collections.CollectionsKt.filterNotNull(m.values()));
                        }
                    }
                } catch (java.lang.Throwable t2) {
                    android.util.Log.w(TAG, "scan receivers of " + guest.getPackageName() + " failed: " + t2.getMessage());
                }
            }
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "dispatcher scan failed: " + t.getMessage());
        }
        return result;
    }

    private final java.lang.Object currentActivityThread() {
        java.lang.Object obj;
        try {
            com.kamispeed.container.Reflect reflect = com.kamispeed.container.Reflect.INSTANCE;
            java.lang.Class<?> cls = java.lang.Class.forName("android.app.ActivityThread");
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(cls, "forName(...)");
            obj = reflect.callStatic(cls, "currentActivityThread", new java.lang.Object[0]);
        } catch (java.lang.Throwable th) {
            obj = null;
        }
        return obj;
    }

    private final java.util.List<java.lang.String> manifestReceiverNames(java.lang.String packageName, com.kamispeed.container.FakeService.GuestEnv env) {
        java.util.List<java.lang.String> emptyList;
        java.util.List<java.lang.String> emptyList2;
        android.content.pm.ActivityInfo[] activityInfoArr;
        try {
            android.content.pm.PackageManager pm = env.getApplication().getBaseContext().getPackageManager();
            android.content.pm.PackageInfo pi = pm.getPackageInfo(packageName, 2);
            if (pi == null || (activityInfoArr = pi.receivers) == null) {
                emptyList2 = kotlin.collections.CollectionsKt.emptyList();
            } else {
                java.util.Collection destination$iv$iv = new java.util.ArrayList();
                for (android.content.pm.ActivityInfo activityInfo : activityInfoArr) {
                    java.lang.String str = activityInfo.name;
                    if (str != null) {
                        destination$iv$iv.add(str);
                    }
                }
                emptyList2 = (java.util.List) destination$iv$iv;
            }
            emptyList = emptyList2;
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "list receivers of " + packageName + " failed: " + t.getMessage());
            emptyList = kotlin.collections.CollectionsKt.emptyList();
        }
        return emptyList;
    }
}
