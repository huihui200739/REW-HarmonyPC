# P3 起步：修复 createGraphics 死循环，遇到 SurfaceData 二次初始化 · 2026-11-18

## 修掉的死循环

    BufferedImage.createGraphics -> OhosGraphicsEnvironment.createGraphics -> img.createGraphics() -> ...

我最初的实现委托给 img.createGraphics()，而 BufferedImage 内部又回到 GraphicsEnvironment，形成无限递归
（StackOverflowError）。

改为标准路径：

    SurfaceData sd = SurfaceData.getPrimarySurfaceData(img);
    return new SunGraphics2D(sd, Color.white, Color.black, defaultFont);

## 新问题

    java.lang.InternalError: Attempting to set SurfaceData ops twice
      at sun.awt.image.BufImgSurfaceData.initRaster(Native Method)
      at sun.awt.image.BufImgSurfaceData.createDataIC
      at sun.awt.image.BufImgSurfaceData.createData
      at sun.awt.image.BufImgSurfaceManager.<init>
      at sun.awt.image.SurfaceManager.getManager

说明 SurfaceData 被创建两次（可能因为 img.createGraphics 与我们的 createGraphics 都在建 SurfaceManager）。

## 下一轮

1. 检查 BufferedImage 的构造（我们用的是 (int,int,int) 三参构造，会创建默认 SurfaceManager）；
2. 对比 SunGraphicsEnvironment 的标准实现，确认我们缺了哪些前置条件；
3. 或改用 “先 getPrimarySurfaceData 再由 SunGraphics2D 接管” 的顺序，避免重复 createData。

## 意义

这一轮开始进入**真实渲染管线**（不再是占位 Peer）。虽然还没画出像素，
但已经能触达 SunGraphics2D / SurfaceData 这一层——这是 Java2D 软件渲染的核心。
