# P3 突破：Java2D 在鸿蒙上渲染成功 · 2026-11-18

## 结果

    setrgb-test: OK
    java2d-render-test: OK /data/storage/el2/base/haps/entry/files/java2d-test.png

拉取的 PNG（400x200）内容：白底、一条黑色横线、一行文字 **REW on HarmonyOS - Java2D OK**。

证据图：docs/img/java2d-render-2026-11-18.png

## 这意味着什么

整条 Java2D 软件渲染管线在 OHOS 上跑通了：

| 环节 | 状态 |
|---|---|
| BufferedImage 创建 | ✅ |
| SurfaceData / SetOps | ✅（side table 方案） |
| SunGraphics2D | ✅ |
| fillRect / drawString | ✅ |
| 字体渲染（FreeType + 字形） | ✅ 文字清晰无锯齿 |
| ImageIO PNG 编码 | ✅ |

## 决定性修复

`SurfaceData_SetOps` 读取 Java 侧 `pData` 字段时得到 0x1（错位读到相邻的 `valid`），
改为 **native side table**（`{jobject, ops}` 数组 + 互斥 + IsSameObject 查表）后彻底解决。

## 走过的坑（值得记入移植手册）

1. 源码替换静默失败（SetOps 里已有诊断 fprintf，文本不匹配）；
2. 四层构建缓存（.o → .so → .jmod → runtime），任一未刷新都表现为“改了没生效”；
3. JNI 签名写错（setRGB `(IIII)V` 应为 `(III)V`）直接崩；
4. `reinterpret_cast<OnLoadFn>(nullptr)` 编译报错，需整体删除相关代码。

## 下一步（P4）

Java2D 能画图了，但画的是离屏 BufferedImage。要让它上屏：

1. FramePeer 真正创建 OHOS 窗口（XComponent）；
2. 把 REW 的 Frame.paint 到 BufferedImage，再把像素送到 Surface；
3. 输入事件从 XComponent 回灌到 AWT EventQueue。
