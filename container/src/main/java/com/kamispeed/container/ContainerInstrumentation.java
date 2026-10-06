package com.kamispeed.container;

/* compiled from: ContainerInstrumentation.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nContainerInstrumentation.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ContainerInstrumentation.kt\ncom/speedmaster/container/ContainerInstrumentation\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,65:1\n13309#2,2:66\n*S KotlinDebug\n*F\n+ 1 ContainerInstrumentation.kt\ncom/speedmaster/container/ContainerInstrumentation\n*L\n26#1:66,2\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/ContainerInstrumentation.class */
public final class ContainerInstrumentation extends android.app.Instrumentation {

    @org.jetbrains.annotations.NotNull
    private final android.app.Instrumentation base;

    public ContainerInstrumentation(@org.jetbrains.annotations.NotNull android.app.Instrumentation base) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(base, "base");
        this.base = base;
        for (java.lang.String str : new java.lang.String[]{"mThread", "mInstrContext", "mAppContext", "mComponent", "mWatcher", "mUiAutomationConnection", "mActivityMonitors"}) {
            java.lang.Object value = com.kamispeed.container.Reflect.INSTANCE.get(this.base, str);
            if (value != null) {
                com.kamispeed.container.Reflect.INSTANCE.set(this, str, value);
            }
        }
    }

    @Override // android.app.Instrumentation
    @org.jetbrains.annotations.NotNull
    public android.app.Application newApplication(@org.jetbrains.annotations.Nullable java.lang.ClassLoader cl, @org.jetbrains.annotations.Nullable java.lang.String className, @org.jetbrains.annotations.Nullable android.content.Context context) {
        android.app.Application app;
        try {
            app = this.base.newApplication(cl, className, context);
        } catch (java.lang.Throwable t) {
            throw new java.lang.RuntimeException(t);
        }
        com.kamispeed.container.ContextPackage.INSTANCE.wrapApplication(app);
        kotlin.jvm.internal.Intrinsics.checkNotNull(app);
        return app;
    }

    @Override // android.app.Instrumentation
    public void callActivityOnCreate(@org.jetbrains.annotations.NotNull android.app.Activity activity, @org.jetbrains.annotations.Nullable android.os.Bundle icicle) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(activity, "activity");
        com.kamispeed.container.ContextPackage.INSTANCE.wrapActivity(activity);
        this.base.callActivityOnCreate(activity, icicle);
    }

    @Override // android.app.Instrumentation
    public void callActivityOnResume(@org.jetbrains.annotations.NotNull android.app.Activity activity) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(activity, "activity");
        this.base.callActivityOnResume(activity);
        try {
            android.view.Window window = activity.getWindow();
            if (window != null) {
                android.view.View decorView = window.getDecorView();
                if (decorView != null) {
                    android.view.ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
                    if (viewTreeObserver != null) {
                        viewTreeObserver.addOnWindowFocusChangeListener((v1) -> {
                            callActivityOnResume$lambda$1(activity, v1);
                        });
                    }
                }
            }
            com.kamispeed.container.ContainerLifecycle.INSTANCE.notifyWindowFocus(activity, activity.hasWindowFocus());
        } catch (java.lang.Throwable th) {
        }
    }

    private static final void callActivityOnResume$lambda$1(android.app.Activity $activity, boolean hasFocus) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter($activity, "$activity");
        com.kamispeed.container.ContainerLifecycle.INSTANCE.notifyWindowFocus($activity, hasFocus);
    }
}
