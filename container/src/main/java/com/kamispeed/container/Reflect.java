package com.kamispeed.container;

/* compiled from: Reflect.kt */
/* loaded from: classes.jar:com/speedmaster/container/Reflect.class */
public final class Reflect {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.Reflect INSTANCE = new com.kamispeed.container.Reflect();

    private Reflect() {
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.Class<?> cls(@org.jetbrains.annotations.NotNull java.lang.String name) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        try {
            return java.lang.Class.forName(name);
        } catch (java.lang.ClassNotFoundException e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.Class<?> clsOrNull(@org.jetbrains.annotations.NotNull java.lang.String name) {
        java.lang.Class<?> cls;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        try {
            cls = java.lang.Class.forName(name);
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("Class.forName(" + name + ")", t);
            cls = null;
        }
        return cls;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Unexpected missing predecessor for block: B:7:0x001b
        	at jadx.core.dex.visitors.blocks.BlockSplitter.addTempConnectionsForExcHandlers(BlockSplitter.java:275)
        	at jadx.core.dex.visitors.blocks.BlockSplitter.visit(BlockSplitter.java:68)
        */
    @org.jetbrains.annotations.Nullable
    public final java.lang.reflect.Field findField(@org.jetbrains.annotations.NotNull java.lang.Class<?> r4, @org.jetbrains.annotations.NotNull java.lang.String r5) {
        /*
            r3 = this;
            r0 = r4
            java.lang.String r1 = "clazz"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r5
            java.lang.String r1 = "name"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r4
            r6 = r0
        Le:
            r0 = r6
            if (r0 == 0) goto L36
            r0 = r6
            java.lang.Class<java.lang.Object> r1 = java.lang.Object.class
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r0, r1)
            if (r0 != 0) goto L36
        L1c:
            r0 = r6
            r1 = r5
            java.lang.reflect.Field r0 = r0.getDeclaredField(r1)     // Catch: java.lang.NoSuchFieldException -> L2c
            r7 = r0
            r0 = r7
            r1 = 1
            r0.setAccessible(r1)     // Catch: java.lang.NoSuchFieldException -> L2c
            r0 = r7
            return r0
        L2c:
            r7 = move-exception
            r0 = r6
            java.lang.Class r0 = r0.getSuperclass()
            r6 = r0
            goto Le
        L36:
            r0 = 0
            return r0
        */
        java.lang.Class<?> c = r4;
        while (c != null && !kotlin.jvm.internal.Intrinsics.areEqual(c, java.lang.Object.class)) {
            try {
                java.lang.reflect.Field f = c.getDeclaredField(r5);
                f.setAccessible(true);
                return f;
            } catch (java.lang.NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.Object get(@org.jetbrains.annotations.Nullable java.lang.Object obj, @org.jetbrains.annotations.NotNull java.lang.String name) {
        java.lang.reflect.Field f;
        java.lang.Object obj2;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        if (obj == null || (f = findField(obj.getClass(), name)) == null) {
            return null;
        }
        try {
            obj2 = f.get(obj);
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("get " + obj.getClass().getSimpleName() + "." + name, t);
            obj2 = null;
        }
        return obj2;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getStatic(@org.jetbrains.annotations.NotNull java.lang.Class<?> cls, @org.jetbrains.annotations.NotNull java.lang.String name) {
        java.lang.Object obj;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cls, "clazz");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        java.lang.reflect.Field f = findField(cls, name);
        if (f == null) {
            return null;
        }
        try {
            obj = f.get(null);
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("getStatic " + cls.getSimpleName() + "." + name, t);
            obj = null;
        }
        return obj;
    }

    public final boolean set(@org.jetbrains.annotations.Nullable java.lang.Object obj, @org.jetbrains.annotations.NotNull java.lang.String name, @org.jetbrains.annotations.Nullable java.lang.Object value) {
        java.lang.reflect.Field f;
        boolean z;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        if (obj == null || (f = findField(obj.getClass(), name)) == null) {
            return false;
        }
        try {
            f.set(obj, value);
            z = true;
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("set " + obj.getClass().getSimpleName() + "." + name, t);
            z = false;
        }
        return z;
    }

    public final boolean setStatic(@org.jetbrains.annotations.NotNull java.lang.Class<?> cls, @org.jetbrains.annotations.NotNull java.lang.String name, @org.jetbrains.annotations.Nullable java.lang.Object value) {
        boolean z;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cls, "clazz");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        java.lang.reflect.Field f = findField(cls, name);
        if (f == null) {
            return false;
        }
        try {
            f.set(null, value);
            z = true;
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("setStatic " + cls.getSimpleName() + "." + name, t);
            z = false;
        }
        return z;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Unexpected missing predecessor for block: B:7:0x0024
        	at jadx.core.dex.visitors.blocks.BlockSplitter.addTempConnectionsForExcHandlers(BlockSplitter.java:275)
        	at jadx.core.dex.visitors.blocks.BlockSplitter.visit(BlockSplitter.java:68)
        */
    @org.jetbrains.annotations.Nullable
    public final java.lang.reflect.Method findMethod(@org.jetbrains.annotations.NotNull java.lang.Class<?> r6, @org.jetbrains.annotations.NotNull java.lang.String r7, @org.jetbrains.annotations.NotNull java.lang.Class<?>... r8) {
        /*
            r5 = this;
            r0 = r6
            java.lang.String r1 = "clazz"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r7
            java.lang.String r1 = "name"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r8
            java.lang.String r1 = "paramTypes"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r6
            r9 = r0
        L15:
            r0 = r9
            if (r0 == 0) goto L4b
            r0 = r9
            java.lang.Class<java.lang.Object> r1 = java.lang.Object.class
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r0, r1)
            if (r0 != 0) goto L4b
        L25:
            r0 = r9
            r1 = r7
            r2 = r8
            r3 = r8
            int r3 = r3.length     // Catch: java.lang.NoSuchMethodException -> L3f
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r3)     // Catch: java.lang.NoSuchMethodException -> L3f
            java.lang.Class[] r2 = (java.lang.Class[]) r2     // Catch: java.lang.NoSuchMethodException -> L3f
            java.lang.reflect.Method r0 = r0.getDeclaredMethod(r1, r2)     // Catch: java.lang.NoSuchMethodException -> L3f
            r10 = r0
            r0 = r10
            r1 = 1
            r0.setAccessible(r1)     // Catch: java.lang.NoSuchMethodException -> L3f
            r0 = r10
            return r0
        L3f:
            r10 = move-exception
            r0 = r9
            java.lang.Class r0 = r0.getSuperclass()
            r9 = r0
            goto L15
        L4b:
            r0 = 0
            return r0
        */
        java.lang.Class<?> c = r6;
        while (c != null && !kotlin.jvm.internal.Intrinsics.areEqual(c, java.lang.Object.class)) {
            try {
                java.lang.reflect.Method m = c.getDeclaredMethod(r7, r8);
                m.setAccessible(true);
                return m;
            } catch (java.lang.NoSuchMethodException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.reflect.Method findMethodLoose(@org.jetbrains.annotations.NotNull java.lang.Class<?> cls, @org.jetbrains.annotations.NotNull java.lang.String name, int argCount) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cls, "clazz");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        java.lang.Class cls2 = cls;
        while (true) {
            java.lang.Class c = cls2;
            if (c != null && !kotlin.jvm.internal.Intrinsics.areEqual(c, java.lang.Object.class)) {
                java.lang.reflect.Method[] declaredMethods = c.getDeclaredMethods();
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(declaredMethods, "getDeclaredMethods(...)");
                for (java.lang.reflect.Method m : declaredMethods) {
                    if (kotlin.jvm.internal.Intrinsics.areEqual(m.getName(), name) && m.getParameterTypes().length == argCount) {
                        m.setAccessible(true);
                        return m;
                    }
                }
                cls2 = c.getSuperclass();
            } else {
                return null;
            }
        }
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.Object call(@org.jetbrains.annotations.Nullable java.lang.Object obj, @org.jetbrains.annotations.NotNull java.lang.String name, @org.jetbrains.annotations.NotNull java.lang.Object... args) {
        java.lang.reflect.Method m;
        java.lang.Object obj2;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(args, "args");
        if (obj == null || (m = findMethodLoose(obj.getClass(), name, args.length)) == null) {
            return null;
        }
        try {
            obj2 = m.invoke(obj, java.util.Arrays.copyOf(args, args.length));
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("call " + obj.getClass().getSimpleName() + "." + name + "/" + args.length, t);
            obj2 = null;
        }
        return obj2;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.Object callTyped(@org.jetbrains.annotations.Nullable java.lang.Object obj, @org.jetbrains.annotations.NotNull java.lang.String name, @org.jetbrains.annotations.NotNull java.lang.Object... args) {
        java.lang.Object obj2;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(args, "args");
        if (obj == null) {
            return null;
        }
        java.lang.Class cls = obj.getClass();
        while (true) {
            java.lang.Class c = cls;
            if (c != null && !kotlin.jvm.internal.Intrinsics.areEqual(c, java.lang.Object.class)) {
                java.lang.reflect.Method[] declaredMethods = c.getDeclaredMethods();
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(declaredMethods, "getDeclaredMethods(...)");
                for (java.lang.reflect.Method m : declaredMethods) {
                    if (kotlin.jvm.internal.Intrinsics.areEqual(m.getName(), name) && m.getParameterTypes().length == args.length) {
                        boolean ok = true;
                        int i = 0;
                        int length = args.length;
                        while (true) {
                            if (i >= length) {
                                break;
                            }
                            java.lang.Class pt = m.getParameterTypes()[i];
                            java.lang.Object a = args[i];
                            if (a == null) {
                                if (pt.isPrimitive()) {
                                    ok = false;
                                    break;
                                }
                                i++;
                            } else {
                                kotlin.jvm.internal.Intrinsics.checkNotNull(pt);
                                if (!boxed(pt).isAssignableFrom(a.getClass()) && !kotlin.jvm.internal.Intrinsics.areEqual(pt, a.getClass())) {
                                    ok = false;
                                    break;
                                }
                                i++;
                            }
                        }
                        if (ok) {
                            m.setAccessible(true);
                            try {
                                obj2 = m.invoke(obj, java.util.Arrays.copyOf(args, args.length));
                            } catch (java.lang.Throwable t) {
                                com.kamispeed.container.Diag.INSTANCE.noteReflect("callTyped " + obj.getClass().getSimpleName() + "." + name, t);
                                obj2 = null;
                            }
                            return obj2;
                        }
                    }
                }
                cls = c.getSuperclass();
            } else {
                return null;
            }
        }
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.Object callStatic(@org.jetbrains.annotations.NotNull java.lang.Class<?> cls, @org.jetbrains.annotations.NotNull java.lang.String name, @org.jetbrains.annotations.NotNull java.lang.Object... args) {
        java.lang.Object obj;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cls, "clazz");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(name, "name");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(args, "args");
        java.lang.reflect.Method m = findMethodLoose(cls, name, args.length);
        if (m == null) {
            return null;
        }
        if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("callStatic " + cls.getSimpleName() + "." + name, new java.lang.IllegalStateException(cls.getSimpleName() + "." + name + " 是实例方法，不能用 callStatic，请改用 Reflect.call(instance, \"" + name + "\", ...)"));
            return null;
        }
        try {
            obj = m.invoke(null, java.util.Arrays.copyOf(args, args.length));
        } catch (java.lang.Throwable t) {
            com.kamispeed.container.Diag.INSTANCE.noteReflect("callStatic " + cls.getSimpleName() + "." + name + "/" + args.length, t);
            obj = null;
        }
        return obj;
    }

    private final java.lang.Class<?> boxed(java.lang.Class<?> cls) {
        return kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Boolean.TYPE) ? java.lang.Boolean.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Byte.TYPE) ? java.lang.Byte.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Character.TYPE) ? java.lang.Character.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Short.TYPE) ? java.lang.Short.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Integer.TYPE) ? java.lang.Integer.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Long.TYPE) ? java.lang.Long.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Float.TYPE) ? java.lang.Float.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Double.TYPE) ? java.lang.Double.class : kotlin.jvm.internal.Intrinsics.areEqual(cls, java.lang.Void.TYPE) ? java.lang.Void.class : cls;
    }
}
