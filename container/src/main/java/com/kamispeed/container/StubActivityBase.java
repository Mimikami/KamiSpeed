package com.kamispeed.container;

/* compiled from: StubActivity.kt */
/* loaded from: classes.jar:com/speedmaster/container/StubActivityBase.class */
public abstract class StubActivityBase extends android.app.Activity {
    @Override // android.app.Activity
    protected void onCreate(@org.jetbrains.annotations.Nullable android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        java.lang.String name = getClass().getName();
        android.content.Intent intent = getIntent();
        android.util.Log.w("SM-Stub", "stub activity opened: " + name + " -> " + (intent != null ? intent.getComponent() : null));
        android.widget.Toast.makeText(this, "容器还原 Activity 失败，请查看 logcat (SM-*)", 1).show();
        finish();
    }
}
