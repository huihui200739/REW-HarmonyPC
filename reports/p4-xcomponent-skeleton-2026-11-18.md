# P4 上屏：XComponent 骨架已写入，编译待修 2 个 API 签名 · 2026-11-18

## 本轮完成

1. 新增 `app/entry/src/main/cpp/xcomponent_bridge.cpp`：
   - `OH_NativeXComponent_Callback`（Created/Changed/Destroyed/Touch 四个回调）；
   - `FillSolid()`：RequestBuffer → mmap → 填充 → FlushBuffer；
   - `RegisterXComponent()`：从 exports 取 `OH_NATIVE_XCOMPONENT_OBJ` 并 `napi_unwrap` 注册；
2. `CMakeLists.txt`：加入 `xcomponent_bridge.cpp`，链接 `native_window`；
3. `jvm_probe_napi.cpp` 的 `Init` 里调用 `RegisterXComponent`；
4. `Index.ets`：用 `XComponent({ id: 'rewSurface', type: 'surface', libraryname: 'rewjvm' })` 替换 `Image`。

## 编译错误（待修）

    xcomponent_bridge.cpp:36: unknown type name 'NativeBufferFlushMode'
    xcomponent_bridge.cpp:36: no matching function for call to 'OH_NativeWindow_NativeWindowFlushBuffer'

`OH_NativeWindow_NativeWindowFlushBuffer` 的第 4 个参数类型需要按当前 SDK 头文件确认
（可能是 `Region` 或 `uint64_t`，取决于 API 版本）。

## 下一轮

1. `grep -A3 'NativeWindowFlushBuffer' $(NDK)/sysroot/usr/include/native_window/external_window.h` 确认签名；
2. 修正后重新构建，验证 XComponent 显示纯色（绿色）；
3. 再接入 REW 像素。
