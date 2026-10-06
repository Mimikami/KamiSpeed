plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

/**
 * 默认走 CMake 源码构建（speedhack/src/main/cpp/CMakeLists.txt）。
 *
 * 如果所在环境用不了 CMake/Ninja（例如 ninja 无法创建子进程的受限沙箱），
 * 可以按下面的方式生成预编译产物，再打开开关：
 *
 *     pwsh tools/build-native.ps1 -Ndk <NDK 路径>
 *     gradle -PnativePrebuilt=true :app:assembleDebug
 *
 * 预编译产物放在 speedhack/src/main/jniLibs/<abi>/libspeedhack.so。
 */
val usePrebuiltNative: Boolean =
    (project.findProperty("nativePrebuilt") as String?)?.toBoolean() ?: false

android {
    namespace = "com.kamispeed.speedhack"
    compileSdk = 34
    ndkVersion = "26.1.10909125"

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")

        if (!usePrebuiltNative) {
            externalNativeBuild {
                cmake {
                    abiFilters += listOf("arm64-v8a", "armeabi-v7a")
                    cFlags += listOf(
                        "-O2",
                        "-fvisibility=hidden",
                        "-fno-exceptions",
                        "-fno-unwind-tables"
                    )
                }
            }
        }
    }

    if (!usePrebuiltNative) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }

    sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
