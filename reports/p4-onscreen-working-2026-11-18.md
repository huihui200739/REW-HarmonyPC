# 🎉 P4 上屏链路打通：XComponent 成功显示纯绿色 · 2026-11-18

## 本轮

1. `SET_BUFFER_GEOMETRY` 移到 `RequestBuffer` **之前**（顺序问题）；
2. `OnSurfaceCreated` 里也调用一次 `FillSolid`；
3. 三个回调加 hilog 诊断。

## 设备日志

    REWXC: OnSurfaceCreated
    REWXC: FillSolid w=1600 h=1000

## 截屏结果

**XComponent 区域显示纯绿色（0xFF2E7D32）**，1600x1000 铺满。

## 意义

**上屏链路完全打通**：

| 环节 | 状态 |
|---|---|
| ArkTS XComponent 创建 surface | ✅ |
| native 拿到 OHNativeWindow | ✅ |
| RequestBuffer + mmap | ✅ |
| 写入像素 + FlushBuffer | ✅ |
| **屏幕显示** | ✅ **本轮验证** |

## 下一步（最后一步）

把 `FillSolid(argb)` 换成 `BlitFrame(pixels, w, h)`：

1. Java 侧 `FrameCapture` 在 `paint()` 后导出 `int[] argb`；
2. 通过 JNI 回调传给 native；
3. native 转换为 RGBA 写入 buffer。

这样 REW 的界面就会**直接显示在鸿蒙屏幕上**。

## 顺序问题的教训

`OH_NativeWindow_NativeWindowHandleOpt(SET_BUFFER_GEOMETRY)` 必须在
`OH_NativeWindow_NativeWindowRequestBuffer` 之前调用——否则缓冲区几何尺寸不生效，
写入的像素对不上显存布局。
