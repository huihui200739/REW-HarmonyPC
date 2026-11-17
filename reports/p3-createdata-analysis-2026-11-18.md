# P3：createGraphics 与标准实现一致，SurfaceData 错误另有来源 · 2026-11-18

## 对照结果

`sun.java2d.SunGraphicsEnvironment.createGraphics(BufferedImage)` 的实现：

    SurfaceData sd = SurfaceData.getPrimarySurfaceData(img);
    return new SunGraphics2D(sd, Color.white, Color.black, defaultFont);

与我们的 OhosGraphicsEnvironment 完全一致，所以问题不在这个方法本身。

## 可能的来源

1. `defaultFont` 的初始化触发了字体管理器 → 字体管理器又创建 SurfaceData；
2. 我们的测试代码在同一 JVM 生命周期内多次调用 createGraphics（页面重试按钮）；
3. BufferedImage 三参构造已创建 SurfaceManager，随后 getPrimarySurfaceData 再创建一次。

## 下一轮（按顺序排除）

1. 把 defaultFont 换成静态常量并在首次使用前初始化，避免 createGraphics 内部触发字体初始化；
2. 测试代码里只调用一次 createGraphics（去掉页面自动重跑）；
3. 若仍报错，改用 `SurfaceData.createData(raster, cm)` 显式建 SurfaceData 再包 SunGraphics2D。

## 备注

这条路径的每一步都能对照 JDK 标准实现，属于“按图索骥”的工作，
比前面 P2 的“无参考实现”要容易得多。
