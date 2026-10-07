package com.kamispeed.container;

/* compiled from: ContextPackage.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nContextPackage.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ContextPackage.kt\ncom/speedmaster/container/ContextPackage\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,376:1\n348#1,5:378\n348#1,5:383\n348#1,5:388\n1#2:377\n*S KotlinDebug\n*F\n+ 1 ContextPackage.kt\ncom/speedmaster/container/ContextPackage\n*L\n319#1:378,5\n320#1:383,5\n321#1:388,5\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/ContextPackage.class */
public final class ContextPackage {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.ContextPackage INSTANCE = new com.kamispeed.container.ContextPackage();

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-ContextPackage";

    @org.jetbrains.annotations.Nullable
    private static volatile java.lang.String hostPackage;

    @org.jetbrains.annotations.Nullable
    private static volatile android.content.Context hostContext;

    @org.jetbrains.annotations.Nullable
    private static volatile java.lang.String guestPackage;

    private ContextPackage() {
    }

    public final void init(@org.jetbrains.annotations.NotNull android.content.Context context) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(context, "context");
        hostContext = context.getApplicationContext();
        hostPackage = context.getPackageName();
    }

    @org.jetbrains.annotations.Nullable
    public final android.content.ContentResolver getHostResolver() {
        android.content.Context context = hostContext;
        if (context != null) {
            return context.getContentResolver();
        }
        return null;
    }

    public final void fix(@org.jetbrains.annotations.Nullable android.content.Context context) {
        java.lang.String str;
        java.lang.String host = hostPackage;
        if (host == null || context == null) {
            return;
        }
        try {
            str = context.getPackageName();
        } catch (java.lang.Throwable th) {
            str = null;
        }
        java.lang.String current = str;
        if (kotlin.jvm.internal.Intrinsics.areEqual(current, host)) {
            return;
        }
        boolean changed = com.kamispeed.container.Reflect.INSTANCE.set(context, "mBasePackageName", host);
        boolean changed2 = com.kamispeed.container.Reflect.INSTANCE.set(context, "mOpPackageName", host) || changed;
        java.lang.Object state = com.kamispeed.container.Reflect.INSTANCE.get(context, "mAttributionSourceState");
        if (state != null) {
            com.kamispeed.container.Reflect.INSTANCE.set(state, "packageName", host);
            com.kamispeed.container.Reflect.INSTANCE.set(context, "mAttributionSource", null);
            changed2 = true;
        }
        android.util.Log.i(TAG, "context package " + current + " -> " + host + " (" + context.getClass().getSimpleName() + ", fields=" + changed2 + ")");
    }

    public final void scrub(@org.jetbrains.annotations.Nullable java.lang.String guestPackage2, @org.jetbrains.annotations.Nullable android.content.Context context) {
        java.lang.Object obj;
        java.lang.String host = hostPackage;
        if (host == null || guestPackage2 == null || context == null || kotlin.jvm.internal.Intrinsics.areEqual(guestPackage2, host)) {
            return;
        }
        java.util.ArrayList hits = new java.util.ArrayList();
        scrubObject(context, guestPackage2, host, "ContextImpl", hits, 0);
        java.lang.Class cls = context.getClass();
        while (true) {
            java.lang.Class c = cls;
            if (c == null || kotlin.jvm.internal.Intrinsics.areEqual(c, java.lang.Object.class)) {
                break;
            }
            java.lang.reflect.Field[] declaredFields = c.getDeclaredFields();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(declaredFields, "getDeclaredFields(...)");
            for (java.lang.reflect.Field f : declaredFields) {
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    java.lang.String typeName = f.getType().getName();
                    kotlin.jvm.internal.Intrinsics.checkNotNull(typeName);
                    if (!typeName.contains("Params") && !typeName.contains("Attribution")) {
                    }
                    try {
                        f.setAccessible(true);
                        obj = f.get(context);
                    } catch (java.lang.Throwable th) {
                        obj = null;
                    }
                    java.lang.Object value = obj;
                    if (value != null) {
                        scrubObject(value, guestPackage2, host, c.getSimpleName() + "." + f.getName(), hits, 1);
                    }
                }
            }
            cls = c.getSuperclass();
        }
        if (!hits.isEmpty()) {
            android.util.Log.i(TAG, "scrub " + guestPackage2 + " -> " + host + " : " + java.lang.String.join(", ", hits));
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Unexpected missing predecessor for block: B:14:0x0050
        	at jadx.core.dex.visitors.blocks.BlockSplitter.addTempConnectionsForExcHandlers(BlockSplitter.java:275)
        	at jadx.core.dex.visitors.blocks.BlockSplitter.visit(BlockSplitter.java:68)
        */
    private final void scrubObject(java.lang.Object target, java.lang.String guest, java.lang.String host, java.lang.String label, java.util.List<java.lang.String> hits, int depth) {
        /*
            Method dump skipped, instructions count: 363
            To view this dump add '--comments-level debug' option
        */
        java.lang.Class<?> c = target.getClass();
        while (c != null && c != java.lang.Object.class) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() != java.lang.String.class) continue;
                try {
                    f.setAccessible(true);
                    if (kotlin.jvm.internal.Intrinsics.areEqual(f.get(target), guest)) {
                        f.set(target, host);
                        hits.add(label + "." + f.getName());
                    }
                } catch (java.lang.Throwable th) {
                }
            }
            c = c.getSuperclass();
        }
        if (depth >= 3) return;
        java.lang.Class<?> d = target.getClass();
        while (d != null && d != java.lang.Object.class) {
            for (java.lang.reflect.Field f : d.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() == java.lang.String.class) continue;
                java.lang.String tn = f.getType().getName();
                if (!tn.contains("Params") && !tn.contains("Attribution")) continue;
                java.lang.Object v;
                try {
                    f.setAccessible(true);
                    v = f.get(target);
                } catch (java.lang.Throwable th) {
                    v = null;
                }
                if (v != null) scrubObject(v, guest, host, label + "." + f.getName(), hits, depth + 1);
            }
            d = d.getSuperclass();
        }
    }

    public final void wrapActivity(@org.jetbrains.annotations.NotNull android.app.Activity activity) {
        android.content.Context base;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(activity, "activity");
        java.lang.String host = hostPackage;
        if (host == null || (base = activity.getBaseContext()) == null || (base instanceof com.kamispeed.container.HostContextWrapper)) {
            return;
        }
        java.lang.String guest = guestPackageOf(activity, base);
        scrub(guest, base);
        java.util.ArrayList hits = new java.util.ArrayList();
        forcePackageFields(base, host, "ContextImpl", hits, 0);
        if (!hits.isEmpty()) {
            android.util.Log.i(TAG, "force fields: " + java.lang.String.join(", ", hits));
        }
        scheduleLoadedApkRename(base);
        if (com.kamispeed.container.Reflect.INSTANCE.set(activity, "mBase", new com.kamispeed.container.HostContextWrapper(base, host, guest))) {
            android.util.Log.i(TAG, "wrapped activity " + activity.getClass().getSimpleName() + " opPkg -> " + host);
        } else {
            android.util.Log.w(TAG, "wrap activity " + activity.getClass().getSimpleName() + " FAILED");
        }
    }

    /**
     * 把访客 Activity 所在 task 的最近任务卡片设置成**游戏自己的名称+图标**。
     * 容器里的游戏 Activity 都装在宿主 Stub 里，系统默认把 task 的 label/icon
     * 标成宿主的（都叫 KamiSpeed），最近任务里主面板和游戏看起来就是同一个应用。
     * 用 TaskDescription 覆盖成访客应用自己的名字和图标，两者就能分开显示。
     */
    public final void applyGuestTaskDescription(@org.jetbrains.annotations.NotNull android.app.Activity activity) {
        try {
            android.content.Context host = hostContext;
            if (host == null) return;
            android.content.pm.ApplicationInfo ai = activity.getApplicationInfo();
            if (ai == null) return;
            java.lang.String guestPkg = ai.packageName;
            if (guestPkg == null || guestPkg.equals(hostPackage)) return;
            java.lang.String label = ai.loadLabel(host.getPackageManager()).toString();
            android.app.ActivityManager.TaskDescription td;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                td = new android.app.ActivityManager.TaskDescription.Builder().setLabel(label).build();
            } else {
                td = new android.app.ActivityManager.TaskDescription(label);
            }
            activity.setTaskDescription(td);
            android.util.Log.i(TAG, "task desc -> " + label + " (" + guestPkg + ")");
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "applyGuestTaskDescription failed: " + t.getMessage());
        }
    }

    public final void wrapApplication(@org.jetbrains.annotations.Nullable android.app.Application application) {        android.content.Context base;
        java.lang.String host = hostPackage;
        if (host == null || application == null || (base = application.getBaseContext()) == null || (base instanceof com.kamispeed.container.HostContextWrapper)) {
            return;
        }
        try {
            java.lang.String guest = base.getPackageName();
            scrub(guest, base);
            java.util.ArrayList hits = new java.util.ArrayList();
            forcePackageFields(base, host, "ContextImpl", hits, 0);
            if (!hits.isEmpty()) {
                android.util.Log.i(TAG, "force fields(app): " + java.lang.String.join(", ", hits));
            }
            scheduleLoadedApkRename(base);
            com.kamispeed.container.Reflect reflect = com.kamispeed.container.Reflect.INSTANCE;
            kotlin.jvm.internal.Intrinsics.checkNotNull(guest);
            if (reflect.set(application, "mBase", new com.kamispeed.container.HostContextWrapper(base, host, guest))) {
                android.util.Log.i(TAG, "wrapped application " + application.getClass().getSimpleName() + " opPkg -> " + host);
            }
        } catch (java.lang.Throwable th) {
        }
    }

    private final java.lang.String guestPackageOf(android.app.Activity activity, android.content.Context base) {
        java.lang.String str;
        java.lang.String str2;
        try {
            java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(activity, "mActivityInfo");
            android.content.pm.ActivityInfo activityInfo = obj instanceof android.content.pm.ActivityInfo ? (android.content.pm.ActivityInfo) obj : null;
            str = activityInfo != null ? activityInfo.packageName : null;
        } catch (java.lang.Throwable th) {
            str = null;
        }
        java.lang.String fromInfo = str;
        java.lang.String host = hostPackage;
        java.lang.String str3 = fromInfo;
        if (!(str3 == null || str3.length() == 0) && !kotlin.jvm.internal.Intrinsics.areEqual(fromInfo, host)) {
            guestPackage = fromInfo;
            return fromInfo;
        }
        java.lang.String it = guestPackage;
        if (it != null) {
            return it;
        }
        try {
            java.lang.String packageName = base.getPackageName();
            kotlin.jvm.internal.Intrinsics.checkNotNull(packageName);
            str2 = packageName;
        } catch (java.lang.Throwable th2) {
            java.lang.String str4 = host;
            if (str4 == null) {
                str4 = "android";
            }
            str2 = str4;
        }
        return str2;
    }

    private final void scheduleLoadedApkRename(android.content.Context context) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            scheduleLoadedApkRename$lambda$1(context);
        });
    }

    private static final void scheduleLoadedApkRename$lambda$1(android.content.Context $context) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter($context, "$context");
        INSTANCE.pointLoadedApkToHost($context);
    }

    private final void pointLoadedApkToHost(android.content.Context context) {
        java.lang.Object apk;
        java.lang.String host = hostPackage;
        if (host != null && (apk = com.kamispeed.container.Reflect.INSTANCE.get(context, "mPackageInfo")) != null && !kotlin.jvm.internal.Intrinsics.areEqual(com.kamispeed.container.Reflect.INSTANCE.get(apk, "mPackageName"), host) && com.kamispeed.container.Reflect.INSTANCE.set(apk, "mPackageName", host)) {
            android.util.Log.i(TAG, "LoadedApk.mPackageName -> " + host);
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Unexpected missing predecessor for block: B:19:0x0078
        	at jadx.core.dex.visitors.blocks.BlockSplitter.addTempConnectionsForExcHandlers(BlockSplitter.java:275)
        	at jadx.core.dex.visitors.blocks.BlockSplitter.visit(BlockSplitter.java:68)
        */
    private final void forcePackageFields(java.lang.Object target, java.lang.String host, java.lang.String label, java.util.List<java.lang.String> hits, int depth) {
        /*
            Method dump skipped, instructions count: 300
            To view this dump add '--comments-level debug' option
        */
        if (depth > 4) return;
        java.lang.Class<?> c = target.getClass();
        while (c != null && c != java.lang.Object.class) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() == java.lang.String.class) {
                    if (!f.getName().toLowerCase(java.util.Locale.ROOT).contains("ackage")) continue;
                    try {
                        f.setAccessible(true);
                        if (!kotlin.jvm.internal.Intrinsics.areEqual(f.get(target), host)) {
                            f.set(target, host);
                            hits.add(label + "." + f.getName());
                        }
                    } catch (java.lang.Throwable th) {
                    }
                } else {
                    java.lang.String tn = f.getType().getName();
                    if (!tn.contains("Attribution") && !tn.contains("Params")) continue;
                    java.lang.Object v;
                    try {
                        f.setAccessible(true);
                        v = f.get(target);
                    } catch (java.lang.Throwable th) {
                        v = null;
                    }
                    if (v != null) forcePackageFields(v, host, label + "." + f.getName(), hits, depth + 1);
                }
            }
            c = c.getSuperclass();
        }
    }

    public final void diag(@org.jetbrains.annotations.NotNull android.app.Activity activity) {
        java.lang.String str;
        java.lang.String str2;
        java.lang.String str3;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(activity, "activity");
        java.lang.String host = hostPackage;
        if (host == null) {
            return;
        }
        java.lang.String simpleName = activity.getClass().getSimpleName();
        try {
            java.lang.String opPackageName = activity.getOpPackageName();
            if (opPackageName == null) {
                opPackageName = "null";
            }
            str = opPackageName;
        } catch (java.lang.Throwable t$iv) {
            str = "ERR:" + t$iv.getClass().getSimpleName();
        }
        java.lang.String str4 = str;
        try {
            java.lang.String opPackageName2 = activity.getBaseContext().getOpPackageName();
            if (opPackageName2 == null) {
                opPackageName2 = "null";
            }
            str2 = opPackageName2;
        } catch (java.lang.Throwable t$iv2) {
            str2 = "ERR:" + t$iv2.getClass().getSimpleName();
        }
        java.lang.String str5 = str2;
        try {
            java.lang.String packageName = activity.getBaseContext().getPackageName();
            if (packageName == null) {
                packageName = "null";
            }
            str3 = packageName;
        } catch (java.lang.Throwable t$iv3) {
            str3 = "ERR:" + t$iv3.getClass().getSimpleName();
        }
        android.util.Log.i(TAG, "diag " + simpleName + ": opSelf=" + str4 + " baseOp=" + str5 + " basePkgName=" + str3 + " host=" + host);
        android.content.Context baseContext = activity.getBaseContext();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(baseContext, "getBaseContext(...)");
        dumpFields("baseCtx", baseContext);
        dumpFields("activity", activity);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Unexpected missing predecessor for block: B:12:0x0059
        	at jadx.core.dex.visitors.blocks.BlockSplitter.addTempConnectionsForExcHandlers(BlockSplitter.java:275)
        	at jadx.core.dex.visitors.blocks.BlockSplitter.visit(BlockSplitter.java:68)
        */
    public final void dumpAllFields(@org.jetbrains.annotations.NotNull java.lang.String tag, @org.jetbrains.annotations.NotNull java.lang.Object target) {
        /*
            r13 = this;
            r0 = r14
            java.lang.String r1 = "tag"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r0 = r15
            java.lang.String r1 = "target"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            java.util.ArrayList r0 = new java.util.ArrayList
            r1 = r0
            r1.<init>()
            r16 = r0
            r0 = r15
            java.lang.Class r0 = r0.getClass()
            r17 = r0
        L1c:
            r0 = r17
            if (r0 == 0) goto Lbc
            r0 = r17
            java.lang.Class<java.lang.Object> r1 = java.lang.Object.class
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r0, r1)
            if (r0 != 0) goto Lbc
            r0 = r17
            java.lang.reflect.Field[] r0 = r0.getDeclaredFields()
            r1 = r0
            java.lang.String r2 = "getDeclaredFields(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r1, r2)
            r18 = r0
            r0 = 0
            r19 = r0
            r0 = r18
            int r0 = r0.length
            r20 = r0
        L40:
            r0 = r19
            r1 = r20
            if (r0 >= r1) goto Lb2
            r0 = r18
            r1 = r19
            r0 = r0[r1]
            r21 = r0
            r0 = r21
            int r0 = r0.getModifiers()
            boolean r0 = java.lang.reflect.Modifier.isStatic(r0)
            if (r0 != 0) goto Lac
        L5a:
            r0 = r21
            r1 = 1
            r0.setAccessible(r1)     // Catch: java.lang.Throwable -> L83
            r0 = r21
            r1 = r15
            java.lang.Object r0 = r0.get(r1)     // Catch: java.lang.Throwable -> L83
            r1 = r0
            if (r1 == 0) goto L7a
            java.lang.String r0 = r0.toString()     // Catch: java.lang.Throwable -> L83
            r1 = r0
            if (r1 == 0) goto L7a
            r1 = 40
            java.lang.String r0 = kotlin.text.StringsKt.take(r0, r1)     // Catch: java.lang.Throwable -> L83
            r1 = r0
            if (r1 != 0) goto L7e
        L7a:
        L7b:
            java.lang.String r0 = "null"
        L7e:
            r23 = r0
            goto L8a
        L83:
            r24 = move-exception
            java.lang.String r0 = "ERR"
            r23 = r0
        L8a:
            r0 = r23
            r22 = r0
            r0 = r16
            r1 = r17
            java.lang.String r1 = r1.getSimpleName()
            r2 = r21
            java.lang.String r2 = r2.getName()
            r3 = r21
            java.lang.Class r3 = r3.getType()
            java.lang.String r3 = r3.getSimpleName()
            r4 = r22
            java.lang.String r1 = r1 + "." + r2 + ":" + r3 + "=" + r4
            boolean r0 = r0.add(r1)
        Lac:
            int r19 = r19 + 1
            goto L40
        Lb2:
            r0 = r17
            java.lang.Class r0 = r0.getSuperclass()
            r17 = r0
            goto L1c
        Lbc:
            java.lang.String r0 = "SM-ContextPackage"
            r1 = r14
            r2 = r16
            int r2 = r2.size()
            r3 = r16
            java.lang.Iterable r3 = (java.lang.Iterable) r3
            java.lang.String r4 = " | "
            java.lang.CharSequence r4 = (java.lang.CharSequence) r4
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            r10 = 62
            r11 = 0
            java.lang.String r3 = kotlin.collections.CollectionsKt.joinToString$default(r3, r4, r5, r6, r7, r8, r9, r10, r11)
            java.lang.String r1 = "allfields(" + r1 + ")[" + r2 + "]: " + r3
            int r0 = android.util.Log.i(r0, r1)
            return
        */
        java.util.ArrayList<java.lang.String> out = new java.util.ArrayList<>();
        java.lang.Class<?> c = target.getClass();
        while (c != null && c != java.lang.Object.class) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                java.lang.String value;
                try {
                    f.setAccessible(true);
                    java.lang.Object o = f.get(target);
                    java.lang.String s = o != null ? o.toString() : "null";
                    if (s.length() > 40) s = s.substring(0, 40);
                    value = s;
                } catch (java.lang.Throwable th) {
                    value = "ERR";
                }
                out.add(c.getSimpleName() + "." + f.getName() + ":" + f.getType().getSimpleName() + "=" + value);
            }
            c = c.getSuperclass();
        }
        android.util.Log.i(TAG, "allfields(" + tag + ")[" + out.size() + "]: " + java.lang.String.join(" | ", out));
    }

    private final java.lang.String safe(kotlin.jvm.functions.Function0<java.lang.String> function0) {
        java.lang.String str;
        try {
            java.lang.String str2 = (java.lang.String) function0.invoke();
            if (str2 == null) {
                str2 = "null";
            }
            str = str2;
        } catch (java.lang.Throwable t) {
            str = "ERR:" + t.getClass().getSimpleName();
        }
        return str;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(11:9|(2:28|23)|13|14|(2:16|(1:18))|24|20|21|22|23|7) */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x008e, code lost:
    
        if (r0 == null) goto L22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x009a, code lost:
    
        r24 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x009c, code lost:
    
        r23 = "ERR:" + r24.getClass().getSimpleName();
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final void dumpFields(java.lang.String tag, java.lang.Object target) {
        /*
            Method dump skipped, instructions count: 263
            To view this dump add '--comments-level debug' option
        */
        java.util.ArrayList<java.lang.String> out = new java.util.ArrayList<>();
        java.lang.Class<?> c = target.getClass();
        while (c != null && c != java.lang.Object.class) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                java.lang.String n = f.getName();
                if (!n.contains("ackage") && !n.contains("ttribution")) continue;
                java.lang.String value;
                try {
                    f.setAccessible(true);
                    java.lang.Object o = f.get(target);
                    java.lang.String s = o != null ? o.toString() : "null";
                    if (s.length() > 48) s = s.substring(0, 48);
                    value = s;
                } catch (java.lang.Throwable th) {
                    value = "ERR:" + th.getClass().getSimpleName();
                }
                out.add(c.getSimpleName() + "." + n + "=" + value + "[" + java.lang.reflect.Modifier.toString(f.getModifiers()) + "]");
            }
            c = c.getSuperclass();
        }
        android.util.Log.i(TAG, "fields(" + tag + "): " + java.lang.String.join(" | ", out));
        dumpAllFields(tag + "-all", target);
    }
}
