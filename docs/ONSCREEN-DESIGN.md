# 上屏方案设计（XComponent + OH_NativeWindow）

> 本文档给出把 REW 的离屏渲染结果显示到 HarmonyOS 屏幕的完整技术方案。
> 当前状态：离屏渲染已成功（`docs/img/rew-ui-rendered-2026-11-18.png`），上屏未实现。

## 为什么不用 Image 组件

已实测三条路线全部失败：

| 方案 | 结果 |
|---|---|
| `Image('file://' + filesDir + '/rew-window.png')` | 空白 |
| `Image(PixelMap)`（`image.createImageSource(buf).createPixelMap()`） | 空白 |
| 默认图片视图 + probe 完成后自动加载 | 空白 |

视图切换本身是生效的（按钮状态变化），说明问题在 `Image` 组件与图片数据的配合。

## 推荐方案：XComponent + 直接写图形缓冲区

### 1. ArkTS 侧

```typescript
XComponent({ id: 'rewSurface', type: 'surface', libraryname: 'rewjvm' })
  .width('100%')
  .height('100%')
  .onLoad(() => { /* surface 就绪 */ })
```

`libraryname` 必须与 native 模块名一致（当前是 `rewjvm`）。

### 2. native 侧

在 `jvm_probe_napi.cpp` 的 `Init` 里注册 XComponent 回调：

```cpp
static OH_NativeXComponent_Callback g_cb = {
    .OnSurfaceCreated = OnSurfaceCreated,
    .OnSurfaceChanged = OnSurfaceChanged,
    .OnSurfaceDestroyed = OnSurfaceDestroyed,
    .DispatchTouchEvent = DispatchTouchEvent,
};

napi_value Init(napi_env env, napi_value exports) {
    napi_value exportInstance = nullptr;
    napi_get_named_property(env, exports, OH_NATIVE_XCOMPONENT_OBJ, &exportInstance);
    OH_NativeXComponent* component = nullptr;
    napi_unwrap(env, exportInstance, reinterpret_cast<void**>(&component));
    OH_NativeXComponent_RegisterCallback(component, &g_cb);
    return exports;
}
```

### 3. 像素传输（核心）

```cpp
static OHNativeWindow* g_window = nullptr;

void OnSurfaceCreated(OH_NativeXComponent* component, void* window) {
    g_window = static_cast<OHNativeWindow*>(window);
}

void BlitFrame(const void* pixels, int width, int height) {
    if (!g_window) return;
    OHNativeWindowBuffer* buffer = nullptr;
    int fenceFd = -1;
    OH_NativeWindow_NativeWindowRequestBuffer(g_window, &buffer, &fenceFd);
    OH_NativeWindow_NativeWindowHandleOpt(g_window, SET_BUFFER_GEOMETRY, width, height);
    BufferHandle* handle = OH_NativeWindow_GetBufferHandleFromNative(buffer);
    void* mapped = mmap(handle->virAddr, handle->size, PROT_READ | PROT_WRITE,
                        MAP_SHARED, handle->fd, 0);
    memcpy(mapped, pixels, width * height * 4);
    OH_NativeWindow_NativeWindowFlushBuffer(g_window, buffer, fenceFd,
                                           NATIVEBUFFER_FLUSH_MODE_IMMEDIATE);
}
```

### 4. 与 Java 渲染对接

在 `FrameCapture` 里，`paint()` 之后不再只 `ImageIO.write`，而是额外导出 `int[]`：

```java
int[] argb = img.getRGB(0, 0, width, height, null, 0, width);
// 转成 RGBA 后调用 native BlitFrame
```

### 5. 输入事件（后续）

`DispatchTouchEvent` 里拿到触摸点，构造 AWT `MouseEvent` 注入 `EventQueue`：

```cpp
// 在 Java 侧暴露静态方法 ohos.awt.Peers.dispatchMouse(int type, int x, int y)
```

## 依赖与构建改动

| 项 | 改动 |
|---|---|
| `CMakeLists.txt` | `target_link_libraries(rewjvm PRIVATE native_window hilog_ndk.z)` |
| `module.json5` | 无需额外权限 |
| ArkTS | 用 `XComponent` 替换 `Image` 视图 |

## 预估工作量

| 步骤 | 轮次 |
|---|---|
| XComponent 骨架 + 纯色测试 | 1-2 |
| 像素传输（BlitFrame） | 1-2 |
| Java 侧对接（导出像素） | 1 |
| 输入事件 | 2-3 |

共约 **5-8 轮**。
