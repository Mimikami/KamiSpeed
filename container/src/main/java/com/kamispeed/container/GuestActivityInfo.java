package com.kamispeed.container;

/* compiled from: LaunchTargets.kt */
/* loaded from: classes.jar:com/speedmaster/container/GuestActivityInfo.class */
public final class GuestActivityInfo {

    @org.jetbrains.annotations.NotNull
    private final java.lang.String className;

    @org.jetbrains.annotations.NotNull
    private final java.lang.String label;
    private final boolean exported;
    private final boolean enabled;
    private final boolean isLauncher;
    private final boolean looksLikeSdkGate;

    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return this.className;
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return this.label;
    }

    public final boolean component3() {
        return this.exported;
    }

    public final boolean component4() {
        return this.enabled;
    }

    public final boolean component5() {
        return this.isLauncher;
    }

    public final boolean component6() {
        return this.looksLikeSdkGate;
    }

    @org.jetbrains.annotations.NotNull
    public final com.kamispeed.container.GuestActivityInfo copy(@org.jetbrains.annotations.NotNull java.lang.String className, @org.jetbrains.annotations.NotNull java.lang.String label, boolean exported, boolean enabled, boolean isLauncher, boolean looksLikeSdkGate) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(className, "className");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(label, "label");
        return new com.kamispeed.container.GuestActivityInfo(className, label, exported, enabled, isLauncher, looksLikeSdkGate);
    }

    public static /* synthetic */ com.kamispeed.container.GuestActivityInfo copy$default(com.kamispeed.container.GuestActivityInfo guestActivityInfo, java.lang.String str, java.lang.String str2, boolean z, boolean z2, boolean z3, boolean z4, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = guestActivityInfo.className;
        }
        if ((i & 2) != 0) {
            str2 = guestActivityInfo.label;
        }
        if ((i & 4) != 0) {
            z = guestActivityInfo.exported;
        }
        if ((i & 8) != 0) {
            z2 = guestActivityInfo.enabled;
        }
        if ((i & 16) != 0) {
            z3 = guestActivityInfo.isLauncher;
        }
        if ((i & 32) != 0) {
            z4 = guestActivityInfo.looksLikeSdkGate;
        }
        return guestActivityInfo.copy(str, str2, z, z2, z3, z4);
    }

    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return "GuestActivityInfo(className=" + this.className + ", label=" + this.label + ", exported=" + this.exported + ", enabled=" + this.enabled + ", isLauncher=" + this.isLauncher + ", looksLikeSdkGate=" + this.looksLikeSdkGate + ")";
    }

    public int hashCode() {
        int result = this.className.hashCode();
        return (((((((((result * 31) + this.label.hashCode()) * 31) + java.lang.Boolean.hashCode(this.exported)) * 31) + java.lang.Boolean.hashCode(this.enabled)) * 31) + java.lang.Boolean.hashCode(this.isLauncher)) * 31) + java.lang.Boolean.hashCode(this.looksLikeSdkGate);
    }

    public boolean equals(@org.jetbrains.annotations.Nullable java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.kamispeed.container.GuestActivityInfo)) {
            return false;
        }
        com.kamispeed.container.GuestActivityInfo guestActivityInfo = (com.kamispeed.container.GuestActivityInfo) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.className, guestActivityInfo.className) && kotlin.jvm.internal.Intrinsics.areEqual(this.label, guestActivityInfo.label) && this.exported == guestActivityInfo.exported && this.enabled == guestActivityInfo.enabled && this.isLauncher == guestActivityInfo.isLauncher && this.looksLikeSdkGate == guestActivityInfo.looksLikeSdkGate;
    }

    public GuestActivityInfo(@org.jetbrains.annotations.NotNull java.lang.String className, @org.jetbrains.annotations.NotNull java.lang.String label, boolean exported, boolean enabled, boolean isLauncher, boolean looksLikeSdkGate) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(className, "className");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(label, "label");
        this.className = className;
        this.label = label;
        this.exported = exported;
        this.enabled = enabled;
        this.isLauncher = isLauncher;
        this.looksLikeSdkGate = looksLikeSdkGate;
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String getClassName() {
        return this.className;
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLabel() {
        return this.label;
    }

    public final boolean getExported() {
        return this.exported;
    }

    public final boolean getEnabled() {
        return this.enabled;
    }

    public final boolean isLauncher() {
        return this.isLauncher;
    }

    public final boolean getLooksLikeSdkGate() {
        return this.looksLikeSdkGate;
    }
}
