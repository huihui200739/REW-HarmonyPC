# P3：排除 initIDs 假设，SurfaceData 冲突另有来源 · 2026-11-18

## 审计结果

31 个 stub 类的完整清单（设备日志）：

    java/awt/{AWTEvent,Button,Checkbox,Component,Container,Cursor,Dialog,Event,
              FileDialog,Frame,Insets,KeyboardFocusManager,Menu,MenuComponent,
              MenuItem,ScrollPane,Scrollbar,TextArea,TextField,TrayIcon,Window}
    java/awt/event/{InputEvent,KeyEvent}
    sun/awt/X11/{GtkFileDialogPeer,XToolkit,XWindow,X11GraphicsConfig,X11InputMethodBase}
    sun/awt/image/JPEGImageDecoder
    sun/java2d/xr/{XRBackendNative,XRSurfaceData}

**BufImgSurfaceData 不在其中**，所以“initIDs 被空实现破坏”这个假设排除。

## 观察

java.awt.* 这批 initIDs 的符号确实不在 libawt/libawt_headless（之前确认过），
它们的 native 实现属于 libawt_xawt（我们没有真实实现，只有空壳）。
这解释了为什么它们一直是 stub —— 但我们用空实现绕过，目前没引发问题。

## 下一步

SurfaceData 冲突要换方向查：
1. 在 native 里 hook `BufImgSurfaceData_initRaster`（或加日志），看它被调用了几次、
   两次调用的调用栈分别来自哪里；
2. 检查 `SurfaceManager.getManager` 与 `SunGraphics2D` 构造是否各触发一次 createData；
3. 若确实如此，可在 createGraphics 里缓存 SurfaceData（按 raster 弱引用），第二次直接复用。
