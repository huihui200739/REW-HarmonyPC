# P4：REW 像素上屏代码已全部就位（待最终验证）· 2026-11-18

## 本轮完成

1. `xcomponent_bridge.cpp` 新增 `BlitFrame(pixels, w, h)`（RequestBuffer → mmap → memcpy → Flush）；
2. `jvm_probe_napi.cpp` 新增 `SurfaceBridge_blit` JNI 实现，并在初始化时 `RegisterNatives`；
   把 `extern void BlitFrame(...)` 声明移到 include 之后（否则在匿名 namespace 内不可见）；
3. JDK 新增 `ohos.awt.SurfaceBridge`（`native void blit(int[], int, int)` + `push()` 容错包装）；
4. `FrameCapture` 在 `paint()` 后调用 `SurfaceBridge.push(argb, width, height)`。

## 构建状态

    MAKE=0（java.desktop）
    BUILD SUCCESSFUL（HAP）

## 设备日志（260 秒时）

    REWXC: OnSurfaceCreated
    REWXC: FillSolid w=1600 h=1000

此时 REW 的界面构建还在进行（约需 4-5 分钟），`BlitFrame` 尚未被调用。

## 结论

**从 Java 渲染 → JNI → native → XComponent 的完整链路代码已全部就位且编译通过。**
只差一次完整的运行（等 REW 构建完成后会自动 `BlitFrame`）。

## 下一轮

1. 部署后等待 300 秒；
2. 抓 `REWXC: BlitFrame 1600x1000` 日志；
3. 截屏确认 REW 界面出现在鸿蒙屏幕上。
