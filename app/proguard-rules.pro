# 反射 / JNI 保留
-keep class com.speedmaster.speedhack.Speed { *; }
-keep class com.speedmaster.container.StubActivity* { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
