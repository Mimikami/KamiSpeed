package com.kamispeed.container;

/* compiled from: AmProxy.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nAmProxy.kt\nKotlin\n*S Kotlin\n*F\n+ 1 AmProxy.kt\ncom/speedmaster/container/AmProxy\n+ 2 ArrayIntrinsics.kt\nkotlin/ArrayIntrinsicsKt\n+ 3 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,211:1\n26#2:212\n26#2:216\n26#2:217\n13374#3,3:213\n*S KotlinDebug\n*F\n+ 1 AmProxy.kt\ncom/speedmaster/container/AmProxy\n*L\n102#1:212\n129#1:216\n173#1:217\n107#1:213,3\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/AmProxy.class */
public final class AmProxy {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.AmProxy INSTANCE = new com.kamispeed.container.AmProxy();

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-AmProxy";
    private static boolean installed;

    private AmProxy() {
    }

    public final boolean install() {
        boolean z;
        boolean ok;
        if (installed) {
            return true;
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                boolean taskOk = installSingleton("android.app.ActivityTaskManager", new java.lang.String[]{"IActivityTaskManagerSingleton", "sServiceSingleton", "sSingleton"}, "android.app.IActivityTaskManager");
                boolean amOk = installSingleton("android.app.ActivityManager", new java.lang.String[]{"IActivityManagerSingleton", "sServiceSingleton", "sSingleton"}, "android.app.IActivityManager");
                ok = taskOk && amOk;
            } else {
                ok = installSingleton("android.app.ActivityManager", new java.lang.String[]{"IActivityManagerSingleton", "sServiceSingleton", "sSingleton"}, "android.app.IActivityManager");
            }
            android.util.Log.i(TAG, "install = " + ok);
            z = ok;
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "install failed: " + t.getMessage());
            z = false;
        }
        installed = z;
        return installed;
    }

    private final boolean installSingleton(java.lang.String holderName, java.lang.String[] fieldNames, java.lang.String ifaceName) {
        try {
            java.lang.Class holder = java.lang.Class.forName(holderName);
            java.lang.Object singleton = null;
            int i = 0;
            int length = fieldNames.length;
            while (true) {
                if (i >= length) {
                    break;
                }
                java.lang.String n = fieldNames[i];
                com.kamispeed.container.Reflect reflect = com.kamispeed.container.Reflect.INSTANCE;
                kotlin.jvm.internal.Intrinsics.checkNotNull(holder);
                singleton = reflect.getStatic(holder, n);
                if (singleton == null) {
                    i++;
                } else {
                    android.util.Log.i(TAG, "found " + holderName + "." + n);
                    break;
                }
            }
            if (singleton == null) {
                android.util.Log.w(TAG, "singleton field not found in " + holderName);
                return false;
            }
            java.lang.Object real = com.kamispeed.container.Reflect.INSTANCE.call(singleton, "get", new java.lang.Object[0]);
            if (real == null) {
                android.util.Log.w(TAG, "singleton.get() returned null");
                return false;
            }
            if (java.lang.reflect.Proxy.isProxyClass(real.getClass())) {
                return true;
            }
            java.lang.Class iface = java.lang.Class.forName(ifaceName);
            java.lang.Object proxy = java.lang.reflect.Proxy.newProxyInstance(iface.getClassLoader(), new java.lang.Class[]{iface}, (v1, v2, v3) -> {
                return installSingleton$lambda$1(real, v1, v2, v3);
            });
            com.kamispeed.container.Reflect.INSTANCE.set(singleton, "mInstance", proxy);
            android.util.Log.i(TAG, ifaceName + " -> proxy installed (" + holderName + ")");
            return true;
        } catch (java.lang.Throwable th) {
            android.util.Log.w(TAG, "no " + holderName);
            return false;
        }
    }

    private static final java.lang.Object installSingleton$lambda$1(java.lang.Object $real, java.lang.Object obj, java.lang.reflect.Method method, java.lang.Object[] args) throws java.lang.Throwable {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter($real, "$real");
        com.kamispeed.container.AmProxy amProxy = INSTANCE;
        kotlin.jvm.internal.Intrinsics.checkNotNull(method);
        return amProxy.dispatch($real, method, args);
    }

    private final java.lang.Object dispatch(java.lang.Object real, java.lang.reflect.Method method, java.lang.Object[] args) throws java.lang.Throwable {
        android.content.Intent intent;
        android.content.Intent intent2;
        android.content.Intent intent3;
        android.content.Intent intent4;
        try {
            java.lang.String name = method.getName();
            if (kotlin.jvm.internal.Intrinsics.areEqual(method.getDeclaringClass(), java.lang.Object.class)) {
                java.lang.Object[] objArr = args;
                if (objArr == null) {
                    objArr = new java.lang.Object[0];
                }
                java.lang.Object[] objArr2 = objArr;
                return method.invoke(real, java.util.Arrays.copyOf(objArr2, objArr2.length));
            }
            kotlin.jvm.internal.Intrinsics.checkNotNull(name);
            if (kotlin.text.StringsKt.contains(name, "startActivit", true)) {
                if (args != null) {
                    int index$iv = 0;
                    for (java.lang.Object item$iv : args) {
                        int i = index$iv;
                        index$iv++;
                        if ((item$iv instanceof java.lang.String) && com.kamispeed.container.GuestAppManager.INSTANCE.isGuestPackage((java.lang.String) item$iv)) {
                            args[i] = com.kamispeed.container.StubRegistry.INSTANCE.hostPackageName();
                            android.util.Log.i(TAG, "callerPackage " + item$iv + " -> " + com.kamispeed.container.StubRegistry.INSTANCE.hostPackageName());
                        } else if (item$iv instanceof android.content.Intent) {
                            args[i] = INSTANCE.rewrite((android.content.Intent) item$iv);
                        } else if (item$iv instanceof java.lang.Object[]) {
                            if ((!(((java.lang.Object[]) item$iv).length == 0)) && (((java.lang.Object[]) item$iv)[0] instanceof android.content.Intent)) {
                                android.content.Intent[] arr = (android.content.Intent[]) item$iv;
                                int length = arr.length;
                                for (int k = 0; k < length; k++) {
                                    arr[k] = INSTANCE.rewrite(arr[k]);
                                }
                            }
                        }
                    }
                }
                java.lang.Object[] objArr3 = args;
                if (objArr3 == null) {
                    objArr3 = new java.lang.Object[0];
                }
                java.lang.Object[] objArr4 = objArr3;
                return method.invoke(real, java.util.Arrays.copyOf(objArr4, objArr4.length));
            }
            if ((name.startsWith("startService") || name.startsWith("startForegroundService")) && (intent = findIntent(args)) != null && com.kamispeed.container.FakeService.INSTANCE.isGuestTarget(intent)) {
                android.content.ComponentName component = com.kamispeed.container.FakeService.INSTANCE.start(intent);
                return defaultValueFor(method, component);
            }
            if (name.startsWith("bindService") && (intent4 = findIntent(args)) != null && com.kamispeed.container.FakeService.INSTANCE.isGuestTarget(intent4)) {
                boolean ok = com.kamispeed.container.FakeService.INSTANCE.bind(intent4);
                return defaultValueFor(method, java.lang.Boolean.valueOf(ok));
            }
            if (name.startsWith("stopService") && (intent3 = findIntent(args)) != null && com.kamispeed.container.FakeService.INSTANCE.isGuestTarget(intent3)) {
                boolean ok2 = com.kamispeed.container.FakeService.INSTANCE.stop(intent3);
                return defaultValueFor(method, java.lang.Boolean.valueOf(ok2));
            }
            if (name.startsWith("broadcastIntent") && (intent2 = findIntent(args)) != null && com.kamispeed.container.FakeReceiver.INSTANCE.shouldHandle(intent2)) {
                com.kamispeed.container.FakeReceiver.INSTANCE.deliverAsync(intent2);
                if (kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), java.lang.Integer.TYPE) || kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), java.lang.Integer.class)) {
                    return 0;
                }
                return true;
            }
            java.lang.Object[] objArr5 = args;
            if (objArr5 == null) {
                objArr5 = new java.lang.Object[0];
            }
            java.lang.Object[] objArr6 = objArr5;
            return method.invoke(real, java.util.Arrays.copyOf(objArr6, objArr6.length));
        } catch (java.lang.reflect.InvocationTargetException e) {
            java.lang.Throwable targetException = e.getTargetException();
            if (targetException == null) {
                throw e;
            }
            throw targetException;
        }
    }

    private final android.content.Intent findIntent(java.lang.Object[] args) {
        if (args == null) {
            return null;
        }
        java.util.Iterator it = kotlin.jvm.internal.ArrayIteratorKt.iterator(args);
        while (it.hasNext()) {
            java.lang.Object a = it.next();
            if (a instanceof android.content.Intent) {
                return (android.content.Intent) a;
            }
        }
        return null;
    }

    private final java.lang.Object defaultValueFor(java.lang.reflect.Method method, java.lang.Object success) {
        if (kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), android.content.ComponentName.class)) {
            return success;
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), java.lang.Boolean.TYPE) || kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), java.lang.Boolean.class)) {
            return true;
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), java.lang.Integer.TYPE) || kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), java.lang.Integer.class)) {
            return 1;
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(method.getReturnType(), java.lang.Long.TYPE)) {
            return 1L;
        }
        return success;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0025, code lost:
    
        if (r0 == null) goto L13;
     */
    @org.jetbrains.annotations.NotNull
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final android.content.Intent rewrite(@org.jetbrains.annotations.NotNull android.content.Intent r5) {
        /*
            r4 = this;
            r0 = r5
            java.lang.String r1 = "intent"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r5
            android.content.ComponentName r0 = r0.getComponent()
            r6 = r0
            r0 = r6
            if (r0 == 0) goto L1c
            com.kamispeed.container.StubRegistry r0 = com.kamispeed.container.StubRegistry.INSTANCE
            r1 = r6
            boolean r0 = r0.isStubComponent(r1)
            if (r0 == 0) goto L1c
            r0 = r5
            return r0
        L1c:
            r0 = r6
            r1 = r0
            if (r1 == 0) goto L28
            java.lang.String r0 = r0.getPackageName()
            r1 = r0
            if (r1 != 0) goto L2d
        L28:
        L29:
            r0 = r5
            java.lang.String r0 = r0.getPackage()
        L2d:
            r7 = r0
            r0 = r7
            if (r0 != 0) goto L34
            r0 = r5
            return r0
        L34:
            com.kamispeed.container.GuestAppManager r0 = com.kamispeed.container.GuestAppManager.INSTANCE
            r1 = r7
            com.kamispeed.container.GuestApp r0 = r0.peek(r1)
            r1 = r0
            if (r1 != 0) goto L42
        L40:
            r0 = r5
            return r0
        L42:
            r8 = r0
            com.kamispeed.container.StubRegistry r0 = com.kamispeed.container.StubRegistry.INSTANCE
            r1 = r8
            r2 = r5
            android.content.Intent r0 = r0.toStubIntent(r1, r2)
            r9 = r0
            r0 = r9
            r1 = r0
            if (r1 != 0) goto L57
        L56:
            r0 = r5
        L57:
            return r0
        */
        android.content.ComponentName comp = r5.getComponent();
        if (comp != null && com.kamispeed.container.StubRegistry.INSTANCE.isStubComponent(comp)) return r5;
        java.lang.String pkg = comp != null ? comp.getPackageName() : r5.getPackage();
        if (pkg == null) return r5;
        com.kamispeed.container.GuestApp guest = com.kamispeed.container.GuestAppManager.INSTANCE.peek(pkg);
        if (guest == null) return r5;
        android.content.Intent stub = com.kamispeed.container.StubRegistry.INSTANCE.toStubIntent(guest, r5);
        return stub != null ? stub : r5;
    }
}
