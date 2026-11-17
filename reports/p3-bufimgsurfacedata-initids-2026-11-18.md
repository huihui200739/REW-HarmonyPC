# P3 诊断：BufImgSurfaceManager 在构造期就建 SurfaceData · 2026-11-18

## 源码事实

    public BufImgSurfaceManager(BufferedImage bImg) {
        this.bImg = bImg;
        this.sdDefault = BufImgSurfaceData.createData(bImg);   // 构造即创建
    }
    public SurfaceData getPrimarySurfaceData() { return sdDefault; }

且 `BufferedImage` 自身不持有 SurfaceManager（懒创建）。

所以正常路径下：第一次 getManager 创建 manager + createData，
后续调用复用同一个 manager —— 不应该出现二次 createData。

## 冲突的可能来源（收窄后）

1. 我们的测试代码里 BufferedImage 被创建了两次（页面重跑），而 JVM 只创建一次，
   第二次的对象复用了同一 raster（可能性低）；
2. `BufImgSurfaceData.createData(bImg)` 的 native 实现在 OHOS 上把 ops 写到了共享位置；
3. 我们自己注册的 initIDs 空实现破坏了 `BufImgSurfaceData` 的字段初始化（它也有 initIDs！）。

## 最可疑的是第 3 条

`sun.awt.image.BufImgSurfaceData` 有 native initIDs，负责缓存 raster/colorModel 的字段 ID。
如果它落在我们注册的 31 个 stub 里，字段 ID 为 NULL，createData 时就会写错位置 ——
恰好能解释 "set ops twice"。

## 下一轮

1. 打印 stub 清单，确认 sun/awt/image/BufImgSurfaceData 是否在其中；
2. 若是，把该类的符号从 libawt 里 dlsym 出来真实注册（或直接在安装期 dlopen 前置）。
