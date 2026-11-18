# P3：SurfaceData 初始化已确认执行，但仍报 ops twice · 2026-11-18

## 本轮

在 probe 里显式强制初始化 `sun.java2d.SurfaceData`（Class.forName + initialize=true，
其静态块会调用 native initIDs）：

    SurfaceData init force: OK
    setrgb-test: OK
    java2d-render-test: EXCEPTION

即 initIDs 确实执行了（pDataID 有效），但 Graphics2D 路径仍然报 ops 二次设置。

## 结论收窄

不是 initIDs 没执行的问题。剩下的可能是：

1. **同一个 BufImgSurfaceData 对象被 SetOps 两次**——即 createData 被调了两次；
2. **pData 字段在对象创建时带脏值**（OHOS 上某些分配路径未清零）。

## 下一步（确定性方案）

给 libawt 的 `SurfaceData_SetOps` 加一段诊断 fprintf：打印 sData 对象指针与 pData 值，
然后重编 libawt（约 2 分钟）。这样能一次性看清是“对象被复用”还是“字段带脏值”。

## 备注

这条路已经非常接近终点：BufferedImage/像素/ImageIO 全通，只差渲染表面。
一旦 SetOps 正常，SunGraphics2D 就能真正画图。
