# P3：分离出结论——像素链路通，只有 SurfaceData/Graphics2D 不通 · 2026-11-18

## 结果

    setrgb-test: OK            <- BufferedImage.setRGB 正常
    java2d-render-test: EXCEPTION  <- Graphics2D/SurfaceData 仍失败

进程存活。

## 意义

把问题进一步切开：

| 链路 | 状态 |
|---|---|
| BufferedImage 创建 | ✅ |
| raster 像素写入 (setRGB) | ✅ |
| ImageIO 写出 | ✅ |
| SurfaceData / SunGraphics2D | ❌ |

也就是说：**图像数据结构与 IO 都没问题，只有硬件/软件渲染表面的初始化有问题**。

## 本轮还修了一个自己的 bug

setRGB 的 JNI 签名写错（写成 `(IIII)V`，实际是 `(III)V`），导致 GetMethodID 返回 NULL，
CallVoidMethod 崩溃。修正后 setrgb-test 通过。

## 下一步

SurfaceData 初始化的问题只剩两个方向（承接上轮）：
1. pData 字段读到脏值（JNI 字段 ID 或内存未初始化）；
2. BufImgSurfaceData 的 native 实现在 cross-compile 后与 Java 侧字段约定不一致。

验证手段：在 native 里打印 `SurfaceData_InitOps` 取到的 pData 值（可以临时给 libawt 打补丁加 fprintf）。
