# P3：SurfaceData API 探索记录 · 2026-11-18

## 试过

1. `SurfaceData.getPrimarySurfaceData(img)` + `new SunGraphics2D(...)`（与标准实现一致）
   → `InternalError: Attempting to set SurfaceData ops twice`；
2. 自检只跑一次（加静态标志）→ 错误不变，排除重复调用；
3. `SurfaceData.createData(WritableRaster, ColorModel)` → 编译失败，该重载不存在。

## 下一步（需要查 SurfaceData 的实际 API）

`SurfaceData` 的工厂方法签名需要对照源码确认，可能的候选：
- `BufImgSurfaceData.createData(BufferedImage)`；
- `SurfaceData.createData(SunGraphics2D, ...)`；
- 通过 `SurfaceManager.getManager(img)` 拿到已有 manager 再取 primary surface。

方向：不要新建 SurfaceData，而是**复用 BufferedImage 已创建的 SurfaceManager**，
这样根本不会出现“ops 二次设置”。
