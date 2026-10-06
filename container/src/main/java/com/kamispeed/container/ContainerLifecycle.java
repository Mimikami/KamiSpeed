package com.kamispeed.container;

/* compiled from: ContainerLifecycle.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nContainerLifecycle.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ContainerLifecycle.kt\ncom/speedmaster/container/ContainerLifecycle\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,37:1\n1855#2,2:38\n*S KotlinDebug\n*F\n+ 1 ContainerLifecycle.kt\ncom/speedmaster/container/ContainerLifecycle\n*L\n34#1:38,2\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/ContainerLifecycle.class */
public final class ContainerLifecycle {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.ContainerLifecycle INSTANCE = new com.kamispeed.container.ContainerLifecycle();

    @org.jetbrains.annotations.NotNull
    private static final java.util.concurrent.CopyOnWriteArrayList<com.kamispeed.container.ContainerLifecycle.FocusListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    /* compiled from: ContainerLifecycle.kt */
    /* loaded from: classes.jar:com/speedmaster/container/ContainerLifecycle$FocusListener.class */
    public interface FocusListener {
        void onGuestWindowFocus(@org.jetbrains.annotations.NotNull android.app.Activity activity, boolean z);
    }

    private ContainerLifecycle() {
    }

    public final void addListener(@org.jetbrains.annotations.NotNull com.kamispeed.container.ContainerLifecycle.FocusListener listener) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(listener, "listener");
        listeners.add(listener);
    }

    public final void removeListener(@org.jetbrains.annotations.NotNull com.kamispeed.container.ContainerLifecycle.FocusListener listener) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(listener, "listener");
        listeners.remove(listener);
    }

    public final void notifyWindowFocus(@org.jetbrains.annotations.NotNull android.app.Activity activity, boolean hasFocus) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(activity, "activity");
        java.lang.Iterable $this$forEach$iv = listeners;
        for (java.lang.Object element$iv : $this$forEach$iv) {
            com.kamispeed.container.ContainerLifecycle.FocusListener it = (com.kamispeed.container.ContainerLifecycle.FocusListener) element$iv;
            it.onGuestWindowFocus(activity, hasFocus);
        }
    }
}
