package com.kamispeed.container;

/* compiled from: StubRegistry.kt */
/* loaded from: classes.jar:com/speedmaster/container/StubRecord.class */
public final class StubRecord {
    private final long token;

    @org.jetbrains.annotations.NotNull
    private final com.kamispeed.container.GuestApp guest;

    @org.jetbrains.annotations.NotNull
    private final android.content.ComponentName stub;

    @org.jetbrains.annotations.NotNull
    private final android.content.Intent targetIntent;

    @org.jetbrains.annotations.NotNull
    private final java.lang.String activityClass;
    private final boolean isLauncherActivity;
    private volatile boolean started;

    public StubRecord(long token, @org.jetbrains.annotations.NotNull com.kamispeed.container.GuestApp guest, @org.jetbrains.annotations.NotNull android.content.ComponentName stub, @org.jetbrains.annotations.NotNull android.content.Intent targetIntent, @org.jetbrains.annotations.NotNull java.lang.String activityClass, boolean isLauncherActivity, boolean started) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(guest, "guest");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(stub, "stub");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(targetIntent, "targetIntent");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(activityClass, "activityClass");
        this.token = token;
        this.guest = guest;
        this.stub = stub;
        this.targetIntent = targetIntent;
        this.activityClass = activityClass;
        this.isLauncherActivity = isLauncherActivity;
        this.started = started;
    }

    public /* synthetic */ StubRecord(long j, com.kamispeed.container.GuestApp guestApp, android.content.ComponentName componentName, android.content.Intent intent, java.lang.String str, boolean z, boolean z2, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this(j, guestApp, componentName, intent, str, z, (i & 64) != 0 ? false : z2);
    }

    public final long getToken() {
        return this.token;
    }

    @org.jetbrains.annotations.NotNull
    public final com.kamispeed.container.GuestApp getGuest() {
        return this.guest;
    }

    @org.jetbrains.annotations.NotNull
    public final android.content.ComponentName getStub() {
        return this.stub;
    }

    @org.jetbrains.annotations.NotNull
    public final android.content.Intent getTargetIntent() {
        return this.targetIntent;
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String getActivityClass() {
        return this.activityClass;
    }

    public final boolean isLauncherActivity() {
        return this.isLauncherActivity;
    }

    public final boolean getStarted() {
        return this.started;
    }

    public final void setStarted(boolean z) {
        this.started = z;
    }
}
