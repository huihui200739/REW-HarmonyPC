cmake_minimum_required(VERSION 3.24)
project(rewjvm LANGUAGES C CXX)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(jdkshim SHARED jvm_shim.c)
target_link_libraries(jdkshim PRIVATE m)
set_target_properties(jdkshim PROPERTIES OUTPUT_NAME jdkshim)

add_library(awt_xawt SHARED awt_xawt_stub.c)
target_include_directories(awt_xawt PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/include")
set_target_properties(awt_xawt PROPERTIES OUTPUT_NAME awt_xawt)

add_library(rewjvm SHARED jvm_probe_napi.cpp)
target_include_directories(rewjvm PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/include")
target_link_libraries(rewjvm PRIVATE ace_napi.z ace_ndk.z hilog_ndk.z dl)
target_link_options(rewjvm PRIVATE -Wl,--no-undefined)
set_target_properties(rewjvm PROPERTIES OUTPUT_NAME rewjvm)