# P3：宿主对照通过，问题锁定在 SurfaceData_InitOps 的 pData 检查 · 2026-11-18

## 对照实验（重要）

在宿主 macOS JDK 17 上跑同样代码：

    BufferedImage img = new BufferedImage(400,200,TYPE_INT_RGB);
    SurfaceData sd = SurfaceData.getPrimarySurfaceData(img);
    Graphics2D g = new SunGraphics2D(sd, WHITE, BLACK, font);
    g.fillRect(...); g.drawString(...);

结果：`surfaceData=sun.awt.image.BufImgSurfaceData@...` / `render OK`。

**调用方式完全正确**，问题只在 OHOS 运行时。

## 源码定位

`Java_sun_awt_image_BufImgSurfaceData_initRaster` 第一句：

    BufImgSDOps *bisdo = (BufImgSDOps*)SurfaceData_InitOps(env, bisd, sizeof(BufImgSDOps));

`SurfaceData_InitOps` 会读取 Java 对象的 pData 字段，若非 NULL 就抛
"Attempting to set SurfaceData ops twice"。也就是说 **pData 读到非零值**。

## 已排除

- `Java_sun_java2d_SurfaceData_initIDs` 定义在 libawt.so，且不在 stub 清单（由我们手动触发的 JNI_OnLoad 注册）；
- BufferedImage 构造不建 SurfaceManager；
- 不是重复调用（加了标志位）。

## 下一步（最可能的两个方向）

1. **JNI 字段 ID 错乱**：SurfaceData_InitOps 用的 sDataID 若未正确初始化，读到的可能是别的字段值；
   验证：在 native 侧打印 GetFieldID 结果与读到的 pData 值；
2. **OHOS 的 malloc 不清零**：SurfaceData 对象若走 native 侧分配且未 memset，pData 可能是脏值；
   验证：用 -Xcheck:jni 或打印对象地址。

## 备注

宿主对照实验是非常有效的手段——它把"我的代码错"和"平台差异"彻底分开了。
