# P3：SurfaceManager 复用仍报 ops 二次设置 · 2026-11-18

## 试过

    SurfaceManager mgr = SurfaceManager.getManager(img);
    SurfaceData sd = mgr.getPrimarySurfaceData();
    return new SunGraphics2D(sd, white, black, font);

结果仍是：

    InternalError: Attempting to set SurfaceData ops twice
      at BufImgSurfaceData.initRaster (native)
      at BufImgSurfaceData.createDataIC
      at BufImgSurfaceData.createData

## 推断

`BufferedImage` 在构造阶段就已经为它的 raster 建立了 SurfaceData；
`getManager(img)` 再次触发 createData，于是 native 侧发现 ops 已存在。

这说明冲突发生在 **BufferedImage 自身构造与我们获取 SurfaceManager 之间**，
而不是我们调用 createGraphics 的时机问题。

## 下一轮（诊断方向）

1. 在 native 里用反射读 BufferedImage 的 `surfaceManager` 字段，看构造后是否已非空；
2. 对比 JDK 正常平台上 BufferedImage 构造后的状态（可在宿主 JDK 上写个小程序验证）；
3. 若确认构造即建，则问题可能在 `BufImgSurfaceData.createData` 的 native 实现与 OHOS 的兼容性，
   需要检查 libawt 的 `BufImgSurfaceData_initRaster` 是否被调了两次（可加日志）。

## 备注

这条路径已经深入到 Java2D 的内部状态机，属于典型的“移植期调试”，
进展慢但每一步都能排除一个假设。
