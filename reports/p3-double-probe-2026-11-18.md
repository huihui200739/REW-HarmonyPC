# P3：SurfaceData 二次初始化 — 怀疑是重复 probe 导致 · 2026-11-18

## 观察

    InternalError: Attempting to set SurfaceData ops twice
      at BufImgSurfaceData.initRaster (native)
      at BufImgSurfaceData.createDataIC
      at BufImgSurfaceData.createData

initRaster 在 native 里检查“ops 是否已设置”，说明同一个 Raster 被 createData 了两次。

## 关键怀疑：probe 被调用了不止一次

ArkTS 页面在 aboutToAppear 里自动调用 run()，按钮也能再触发；
而 JNI_CreateJavaVM 在同一个进程里只能成功一次——第二次 probe 会拿到同一个 JVM 环境，
并把我们的 Java2D 自检代码**再跑一遍**，于是同一个 BufferedImage/Raster 被二次初始化。

## 下一轮验证

1. 在 native 侧加静态标志，自检代码只执行一次（第二次跳过）；
2. ArkTS 侧去掉自动重跑，或加重试保护；
3. 若确认，Java2D 自检应能通过，并生成 java2d-test.png。

## 备注

这个错误与 AWT 后端实现无关，是测试脚手架的问题——先修脚手架再看真实渲染。
