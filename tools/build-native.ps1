<#
  直接用 NDK 的 clang 编译 libspeedhack.so（不经过 CMake / Ninja）。

  什么时候用：
    正常开发请在 Android Studio 里直接构建（走 speedhack/src/main/cpp/CMakeLists.txt）。
    只有在 CMake/Ninja 不可用的环境（例如受限沙箱、ninja 无法创建子进程）里，
    才用本脚本产出预编译 .so，然后：
        gradle -PnativePrebuilt=true :app:assembleDebug

  用法：
    pwsh tools/build-native.ps1 -Ndk <NDK 路径>
#>
param(
    [string]$Ndk = $env:ANDROID_NDK_HOME,
    [string]$Api = "24",
    [string[]]$Abis = @("arm64-v8a", "armeabi-v7a")
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Ndk)) {
    $sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { $env:ANDROID_SDK_ROOT }
    if (-not $sdk) { throw "请通过 -Ndk 指定 NDK 路径，或设置 ANDROID_NDK_HOME / ANDROID_HOME" }
    $Ndk = (Get-ChildItem "$sdk\ndk" -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
}

$clang = Join-Path $Ndk "toolchains\llvm\prebuilt\windows-x86_64\bin\clang.exe"
if (-not (Test-Path $clang)) { throw "找不到 clang: $clang" }

$root = Split-Path -Parent $PSScriptRoot
$src  = Join-Path $root "speedhack\src\main\cpp"
$out  = Join-Path $root "speedhack\src\main\jniLibs"

# 与 CMakeLists.txt 保持一致
$cflags = @(
    "-O2", "-fPIC", "-fvisibility=hidden", "-fno-exceptions", "-fno-unwind-tables",
    "-ffunction-sections", "-fdata-sections", "-Wall", "-Wextra",
    "-Wno-unused-parameter", "-DANDROID"
)
$ldflags = @("-shared", "-Wl,--gc-sections", "-Wl,-z,max-page-size=16384")
$libs    = @("-llog", "-ldl")

$targets = @{
    "arm64-v8a"   = "aarch64-linux-android$Api"
    "armeabi-v7a" = "armv7a-linux-androideabi$Api"
}

foreach ($abi in $Abis) {
    if (-not $targets.ContainsKey($abi)) { throw "不支持的 ABI: $abi" }
    $target = $targets[$abi]
    $dest   = Join-Path $out $abi
    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    $so     = Join-Path $dest "libspeedhack.so"

    $objs = @()
    foreach ($c in @("elf_util.c", "speedhack.c")) {
        $obj = Join-Path $env:TEMP ("sh_" + $abi + "_" + [IO.Path]::GetFileNameWithoutExtension($c) + ".o")
        Write-Host "  [$abi] CC  $c"
        & $clang "--target=$target" @cflags -c (Join-Path $src $c) -o $obj
        if ($LASTEXITCODE -ne 0) { throw "编译失败: $c ($abi)" }
        $objs += $obj
    }

    Write-Host "  [$abi] LD  libspeedhack.so"
    & $clang "--target=$target" @ldflags -o $so @objs @libs
    if ($LASTEXITCODE -ne 0) { throw "链接失败 ($abi)" }
    $objs | ForEach-Object { Remove-Item -Force $_ -ErrorAction SilentlyContinue }

    $size = (Get-Item $so).Length
    Write-Host ("  [$abi] OK  {0}  ({1:N0} bytes)" -f $so, $size)
}

Write-Host ""
Write-Host "预编译完成。构建 APK："
Write-Host "  gradle -PnativePrebuilt=true :app:assembleDebug"
