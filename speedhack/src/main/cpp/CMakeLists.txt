cmake_minimum_required(VERSION 3.22.1)
project(speedhack C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

add_library(speedhack SHARED
        elf_util.c
        speedhack.c)

# liblog / libdl 直接用名字链接即可，现代 NDK 里 dl 系列符号已在 libc 中
target_link_libraries(speedhack log dl)

target_compile_options(speedhack PRIVATE
        -Wall
        -Wextra
        -Wno-unused-parameter
        -O2
        -fvisibility=hidden
        -fno-exceptions
        -ffunction-sections
        -fdata-sections)

target_link_options(speedhack PRIVATE -Wl,--gc-sections)
