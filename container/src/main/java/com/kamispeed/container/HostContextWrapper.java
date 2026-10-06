package com.kamispeed.container;

/* compiled from: HostContextWrapper.kt */
/* loaded from: classes.jar:com/speedmaster/container/HostContextWrapper.class */
public final class HostContextWrapper extends android.content.ContextWrapper {

    @org.jetbrains.annotations.NotNull
    private final java.lang.String hostPackageName;

    @org.jetbrains.annotations.NotNull
    private final java.lang.String guestPackageName;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public HostContextWrapper(@org.jetbrains.annotations.NotNull android.content.Context base, @org.jetbrains.annotations.NotNull java.lang.String hostPackageName, @org.jetbrains.annotations.NotNull java.lang.String guestPackageName) {
        super(base);
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(base, "base");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hostPackageName, "hostPackageName");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(guestPackageName, "guestPackageName");
        this.hostPackageName = hostPackageName;
        this.guestPackageName = guestPackageName;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.NotNull
    public java.lang.String getOpPackageName() {
        return this.hostPackageName;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.NotNull
    public java.lang.String getPackageName() {
        return this.guestPackageName;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.NotNull
    public android.content.ContentResolver getContentResolver() {
        android.content.ContentResolver hostResolver = com.kamispeed.container.ContextPackage.INSTANCE.getHostResolver();
        if (hostResolver != null) {
            return hostResolver;
        }
        android.content.ContentResolver contentResolver = super.getContentResolver();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(contentResolver, "getContentResolver(...)");
        return contentResolver;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.Nullable
    public java.lang.String getAttributionTag() {
        return null;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.NotNull
    public android.content.Context createAttributionContext(@org.jetbrains.annotations.Nullable java.lang.String attributeTag) {
        android.content.Context createAttributionContext = super.createAttributionContext(attributeTag);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(createAttributionContext, "createAttributionContext(...)");
        return new com.kamispeed.container.HostContextWrapper(createAttributionContext, this.hostPackageName, this.guestPackageName);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.Nullable
    public android.content.Intent registerReceiver(@org.jetbrains.annotations.Nullable android.content.BroadcastReceiver receiver, @org.jetbrains.annotations.Nullable android.content.IntentFilter filter) {
        com.kamispeed.container.FakeReceiver.INSTANCE.recordReceiver(receiver, filter, this);
        return super.registerReceiver(receiver, filter);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.Nullable
    public android.content.Intent registerReceiver(@org.jetbrains.annotations.Nullable android.content.BroadcastReceiver receiver, @org.jetbrains.annotations.Nullable android.content.IntentFilter filter, @org.jetbrains.annotations.Nullable java.lang.String broadcastPermission, @org.jetbrains.annotations.Nullable android.os.Handler scheduler) {
        com.kamispeed.container.FakeReceiver.INSTANCE.recordReceiver(receiver, filter, this);
        return super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.Nullable
    public android.content.Intent registerReceiver(@org.jetbrains.annotations.Nullable android.content.BroadcastReceiver receiver, @org.jetbrains.annotations.Nullable android.content.IntentFilter filter, int flags) {
        com.kamispeed.container.FakeReceiver.INSTANCE.recordReceiver(receiver, filter, this);
        return super.registerReceiver(receiver, filter, flags);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @org.jetbrains.annotations.Nullable
    public android.content.Intent registerReceiver(@org.jetbrains.annotations.Nullable android.content.BroadcastReceiver receiver, @org.jetbrains.annotations.Nullable android.content.IntentFilter filter, @org.jetbrains.annotations.Nullable java.lang.String broadcastPermission, @org.jetbrains.annotations.Nullable android.os.Handler scheduler, int flags) {
        com.kamispeed.container.FakeReceiver.INSTANCE.recordReceiver(receiver, filter, this);
        return super.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
    }
}
