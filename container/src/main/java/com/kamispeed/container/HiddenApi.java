package com.kamispeed.container;

/* compiled from: HiddenApi.kt */
/* loaded from: classes.jar:com/speedmaster/container/HiddenApi.class */
public final class HiddenApi {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.HiddenApi INSTANCE = new com.kamispeed.container.HiddenApi();

    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SM-HiddenApi";
    private static boolean applied;

    private HiddenApi() {
    }

    public final boolean bypass() {
        boolean z;
        if (applied) {
            return true;
        }
        try {
            java.lang.reflect.Method forName = java.lang.Class.class.getDeclaredMethod("forName", java.lang.String.class);
            java.lang.Class classArrayClass = new java.lang.Class[0].getClass();
            java.lang.reflect.Method getDeclaredMethod = java.lang.Class.class.getDeclaredMethod("getDeclaredMethod", java.lang.String.class, classArrayClass);
            java.lang.Object invoke = forName.invoke(null, "dalvik.system.VMRuntime");
            kotlin.jvm.internal.Intrinsics.checkNotNull(invoke, "null cannot be cast to non-null type java.lang.Class<*>");
            java.lang.Class vmRuntimeClass = (java.lang.Class) invoke;
            java.lang.Object invoke2 = getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
            kotlin.jvm.internal.Intrinsics.checkNotNull(invoke2, "null cannot be cast to non-null type java.lang.reflect.Method");
            java.lang.reflect.Method getRuntime = (java.lang.reflect.Method) invoke2;
            java.lang.Object invoke3 = getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new java.lang.Class[]{java.lang.String.class});
            kotlin.jvm.internal.Intrinsics.checkNotNull(invoke3, "null cannot be cast to non-null type java.lang.reflect.Method");
            java.lang.reflect.Method setHiddenApiExemptions = (java.lang.reflect.Method) invoke3;
            java.lang.Object vmRuntime = getRuntime.invoke(null, new java.lang.Object[0]);
            setHiddenApiExemptions.invoke(vmRuntime, (java.lang.Object) new java.lang.String[]{"L"});
            applied = true;
            android.util.Log.i(TAG, "hidden api exemptions applied");
            z = true;
        } catch (java.lang.Throwable t) {
            android.util.Log.w(TAG, "bypass failed: " + t.getMessage());
            z = false;
        }
        return z;
    }
}
