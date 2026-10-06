package com.kamispeed.container;

/* compiled from: Diag.kt */
@kotlin.jvm.internal.SourceDebugExtension({"SMAP\nDiag.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Diag.kt\ncom/speedmaster/container/Diag\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,76:1\n1855#2,2:77\n1855#2,2:79\n*S KotlinDebug\n*F\n+ 1 Diag.kt\ncom/speedmaster/container/Diag\n*L\n65#1:77,2\n70#1:79,2\n*E\n"})
/* loaded from: classes.jar:com/speedmaster/container/Diag.class */
public final class Diag {

    @org.jetbrains.annotations.NotNull
    public static final com.kamispeed.container.Diag INSTANCE = new com.kamispeed.container.Diag();

    @org.jetbrains.annotations.Nullable
    private static volatile java.lang.String lastError;

    @org.jetbrains.annotations.Nullable
    private static volatile java.lang.String lastStage;

    @org.jetbrains.annotations.Nullable
    private static volatile java.lang.String lastPackage;

    @org.jetbrains.annotations.Nullable
    private static volatile java.lang.String lastReflectError;

    private Diag() {
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastError() {
        return lastError;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastStage() {
        return lastStage;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastPackage() {
        return lastPackage;
    }

    public final void clear() {
        lastError = null;
        lastStage = null;
        lastPackage = null;
        lastReflectError = null;
    }

    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastReflectError() {
        return lastReflectError;
    }

    public final void noteReflect(@org.jetbrains.annotations.NotNull java.lang.String target, @org.jetbrains.annotations.NotNull java.lang.Throwable t) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(target, "target");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(t, "t");
        lastReflectError = target + " -> " + t.getClass().getName() + ": " + t.getMessage();
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String report() {
        java.lang.String err = lastError;
        if (err == null) {
            return "未知原因（Diag 未记录到异常）";
        }
        java.lang.String refl = lastReflectError;
        java.lang.String str = refl;
        return str == null || str.length() == 0 ? err : err + "\n--- 期间被吞掉的反射异常 ---\n" + refl;
    }

    public final void fail(@org.jetbrains.annotations.NotNull java.lang.String stage, @org.jetbrains.annotations.Nullable java.lang.String packageName, @org.jetbrains.annotations.NotNull java.lang.Throwable t) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(stage, "stage");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(t, "t");
        lastStage = stage;
        lastPackage = packageName;
        java.lang.String str = packageName;
        if (str == null) {
            str = "-";
        }
        lastError = "[" + stage + "] " + str + "\n" + describe(t);
    }

    public final void fail(@org.jetbrains.annotations.NotNull java.lang.String stage, @org.jetbrains.annotations.Nullable java.lang.String packageName, @org.jetbrains.annotations.NotNull java.lang.String message) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(stage, "stage");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(message, "message");
        lastStage = stage;
        lastPackage = packageName;
        java.lang.String str = packageName;
        if (str == null) {
            str = "-";
        }
        lastError = "[" + stage + "] " + str + "\n" + message;
    }

    @org.jetbrains.annotations.NotNull
    public final java.lang.String describe(@org.jetbrains.annotations.NotNull java.lang.Throwable t) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(t, "t");
        java.lang.StringBuilder $this$describe_u24lambda_u242 = new java.lang.StringBuilder();
        $this$describe_u24lambda_u242.append(t.getClass().getName());
        $this$describe_u24lambda_u242.append(": ");
        $this$describe_u24lambda_u242.append(t.getMessage());
        $this$describe_u24lambda_u242.append('\n');
        java.lang.StackTraceElement[] stackTrace = t.getStackTrace();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(stackTrace, "getStackTrace(...)");
        java.lang.Iterable $this$forEach$iv = kotlin.collections.ArraysKt.take(stackTrace, 12);
        for (java.lang.Object element$iv : $this$forEach$iv) {
            java.lang.StackTraceElement it = (java.lang.StackTraceElement) element$iv;
            $this$describe_u24lambda_u242.append("    at ").append(it).append('\n');
        }
        java.lang.Throwable cause = t.getCause();
        for (int depth = 0; cause != null && depth < 3 && cause != t; depth++) {
            $this$describe_u24lambda_u242.append("Caused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append('\n');
            java.lang.StackTraceElement[] stackTrace2 = cause.getStackTrace();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(stackTrace2, "getStackTrace(...)");
            java.lang.Iterable $this$forEach$iv2 = kotlin.collections.ArraysKt.take(stackTrace2, 6);
            for (java.lang.Object element$iv2 : $this$forEach$iv2) {
                java.lang.StackTraceElement it2 = (java.lang.StackTraceElement) element$iv2;
                $this$describe_u24lambda_u242.append("    at ").append(it2).append('\n');
            }
            cause = cause.getCause();
        }
        java.lang.String sb = $this$describe_u24lambda_u242.toString();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(sb, "toString(...)");
        return sb;
    }
}
