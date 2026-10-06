package com.kamispeed.container;

/* compiled from: ActivityThreadHook.kt */
/* loaded from: classes.jar:com/speedmaster/container/ActivityThreadHook.class */
public final class ActivityThreadHook {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.ActivityThreadHook INSTANCE = new com.kamispeed.container.ActivityThreadHook();

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-ATHook";
    private static final int LAUNCH_ACTIVITY = 100;
    private static final int NEW_INTENT = 112;
    private static final int EXECUTE_TRANSACTION = 159;
    private static boolean installed;

    private ActivityThreadHook() {
    }

    public final boolean install() {
        boolean z;
        java.lang.Object at = null;
        if (installed) {
            return true;
        }
        try {
            java.lang.Class atClass = java.lang.Class.forName("android.app.ActivityThread");
            com.kamispeed.container.Reflect reflect = com.kamispeed.container.Reflect.INSTANCE;
            kotlin.jvm.internal.Intrinsics.checkNotNull(atClass);
            at = reflect.callStatic(atClass, "currentActivityThread", new java.lang.Object[0]);
        } catch (java.lang.Throwable t) {
            android.util.Log.e(TAG, "install failed", t);
            com.kamispeed.container.Diag.INSTANCE.fail("hook-activitythread", (java.lang.String) null, t);
            z = false;
        }
        if (at != null) {
            java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(at, "mH");
            android.os.Handler handler = obj instanceof android.os.Handler ? (android.os.Handler) obj : null;
            if (handler != null) {
                android.os.Handler mH = handler;
                java.lang.Object obj2 = com.kamispeed.container.Reflect.INSTANCE.get(mH, "mCallback");
                android.os.Handler.Callback original = obj2 instanceof android.os.Handler.Callback ? (android.os.Handler.Callback) obj2 : null;
                android.os.Handler.Callback callback = (v1) -> {
                    return install$lambda$0(original, v1);
                };
                com.kamispeed.container.Reflect.INSTANCE.set(mH, "mCallback", callback);
                installInstrumentation(at);
                installed = true;
                android.util.Log.i(TAG, "hook installed (api=" + android.os.Build.VERSION.SDK_INT + ")");
                z = true;
                return z;
            }
            throw new java.lang.IllegalStateException("ActivityThread.mH is null");
        }
        throw new java.lang.IllegalStateException("ActivityThread.currentActivityThread() == null");
    }

    private static final boolean install$lambda$0(android.os.Handler.Callback $original, android.os.Message msg) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(msg, "msg");
        return INSTANCE.onMessage(msg, $original);
    }

    private final void installInstrumentation(java.lang.Object at) {
        java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(at, "mInstrumentation");
        android.app.Instrumentation instrumentation = obj instanceof android.app.Instrumentation ? (android.app.Instrumentation) obj : null;
        if (instrumentation == null) {
            return;
        }
        android.app.Instrumentation current = instrumentation;
        if (current instanceof com.kamispeed.container.ContainerInstrumentation) {
            return;
        }
        com.kamispeed.container.Reflect.INSTANCE.set(at, "mInstrumentation", new com.kamispeed.container.ContainerInstrumentation(current));
        android.util.Log.i(TAG, "instrumentation replaced: " + current.getClass().getName());
    }

    private final boolean onMessage(android.os.Message msg, android.os.Handler.Callback original) {
        try {
            switch (msg.what) {
                case LAUNCH_ACTIVITY /* 100 */:
                case NEW_INTENT /* 112 */:
                    patchRecord(msg.obj);
                    break;
                case EXECUTE_TRANSACTION /* 159 */:
                    patchTransaction(msg.obj);
                    break;
            }
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "handle msg " + msg.what + " failed: " + t.getMessage());
            com.kamispeed.container.Diag.INSTANCE.fail("patch-message/" + msg.what, (java.lang.String) null, t);
        }
        if (original != null) {
            return original.handleMessage(msg);
        }
        return false;
    }

    private final void patchRecord(java.lang.Object record) {
        android.content.Intent rawIntent;
        com.kamispeed.container.StubRecord rec;
        android.content.Intent guestIntent;
        if (record == null) {
            return;
        }
        java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(record, "intent");
        android.content.Intent intent = obj instanceof android.content.Intent ? (android.content.Intent) obj : null;
        if (intent == null || (rec = com.kamispeed.container.StubRegistry.INSTANCE.recordOf((rawIntent = intent))) == null || (guestIntent = com.kamispeed.container.StubRegistry.INSTANCE.toGuestIntent(rawIntent)) == null) {
            return;
        }
        com.kamispeed.container.Reflect.INSTANCE.set(record, "intent", guestIntent);
        android.content.pm.ActivityInfo info = rec.getGuest().activityInfo(rec.getActivityClass());
        if (info != null) {
            com.kamispeed.container.Reflect.INSTANCE.set(record, "activityInfo", info);
            android.util.Log.i(TAG, "patched -> " + rec.getGuest().getPackageName() + "/" + rec.getActivityClass());
        } else {
            com.kamispeed.container.Diag.INSTANCE.fail("resolve-activityinfo", rec.getGuest().getPackageName(), "拿不到 " + rec.getActivityClass() + " 的 ActivityInfo");
        }
    }

    private final void patchTransaction(java.lang.Object transaction) {
        if (transaction == null) {
            return;
        }
        java.lang.Object call = com.kamispeed.container.Reflect.INSTANCE.call(transaction, "getCallbacks", new java.lang.Object[0]);
        java.util.List list = call instanceof java.util.List ? (java.util.List) call : null;
        if (list == null) {
            return;
        }
        java.util.List callbacks = list;
        for (java.lang.Object item : callbacks) {
            if (item != null) {
                java.lang.String name = item.getClass().getName();
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(name, "getName(...)");
                if (name.endsWith("LaunchActivityItem")) {
                    patchLaunchItem(item);
                } else {
                    java.lang.String name2 = item.getClass().getName();
                    kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(name2, "getName(...)");
                    if (name2.endsWith("NewIntentItem")) {
                        patchNewIntentItem(item);
                    } else {
                        java.lang.String name3 = item.getClass().getName();
                        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(name3, "getName(...)");
                        if (name3.endsWith("RelaunchActivityItem")) {
                            patchRelaunchItem(item);
                        }
                    }
                }
            }
        }
    }

    private final void patchLaunchItem(java.lang.Object item) {
        android.content.Intent rawIntent;
        com.kamispeed.container.StubRecord rec;
        android.content.Intent guestIntent;
        java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(item, "mIntent");
        android.content.Intent intent = obj instanceof android.content.Intent ? (android.content.Intent) obj : null;
        if (intent == null || (rec = com.kamispeed.container.StubRegistry.INSTANCE.recordOf((rawIntent = intent))) == null || (guestIntent = com.kamispeed.container.StubRegistry.INSTANCE.toGuestIntent(rawIntent)) == null) {
            return;
        }
        com.kamispeed.container.Reflect.INSTANCE.set(item, "mIntent", guestIntent);
        android.content.pm.ActivityInfo info = rec.getGuest().activityInfo(rec.getActivityClass());
        if (info != null) {
            com.kamispeed.container.Reflect.INSTANCE.set(item, "mInfo", info);
            android.util.Log.i(TAG, "patched -> " + rec.getGuest().getPackageName() + "/" + rec.getActivityClass());
        } else {
            com.kamispeed.container.Diag.INSTANCE.fail("resolve-activityinfo", rec.getGuest().getPackageName(), "拿不到 " + rec.getActivityClass() + " 的 ActivityInfo");
        }
    }

    private final void patchNewIntentItem(java.lang.Object item) {
        java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(item, "mIntents");
        java.util.List list = kotlin.jvm.internal.TypeIntrinsics.isMutableList(obj) ? (java.util.List) obj : null;
        if (list == null) {
            return;
        }
        java.util.List list2 = list;
        int size = list2.size();
        for (int i = 0; i < size; i++) {
            java.lang.Object obj2 = list2.get(i);
            android.content.Intent intent = obj2 instanceof android.content.Intent ? (android.content.Intent) obj2 : null;
            if (intent != null) {
                android.content.Intent raw = intent;
                android.content.Intent guest = com.kamispeed.container.StubRegistry.INSTANCE.toGuestIntent(raw);
                if (guest != null) {
                    list2.set(i, guest);
                }
            }
        }
    }

    private final void patchRelaunchItem(java.lang.Object item) {
        com.kamispeed.container.StubRecord rec;
        android.content.pm.ActivityInfo guestInfo;
        java.lang.Object obj = com.kamispeed.container.Reflect.INSTANCE.get(item, "mActivityInfo");
        android.content.pm.ActivityInfo activityInfo = obj instanceof android.content.pm.ActivityInfo ? (android.content.pm.ActivityInfo) obj : null;
        if (activityInfo == null) {
            return;
        }
        android.content.pm.ActivityInfo info = activityInfo;
        if (!com.kamispeed.container.StubRegistry.INSTANCE.isStubClassName(info.name) || (rec = com.kamispeed.container.StubRegistry.INSTANCE.byStubName(info.name)) == null || (guestInfo = rec.getGuest().activityInfo(rec.getActivityClass())) == null) {
            return;
        }
        com.kamispeed.container.Reflect.INSTANCE.set(item, "mActivityInfo", guestInfo);
        android.util.Log.i(TAG, "relaunch info patched -> " + rec.getGuest().getPackageName() + "/" + rec.getActivityClass());
    }
}
