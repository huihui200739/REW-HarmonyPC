# P4 起步：FrameCapture 已接入，执行未生效 · 2026-11-18

## 本轮完成

1. 写 `ohos.awt.FrameCapture`（下沉进 java.desktop）：
   - 在后台线程调用目标应用的 main()；
   - 等待指定时间；
   - 在 EDT 上把所有可见 Frame paint 到离屏 BufferedImage；
   - ImageIO 写 PNG；
   - 返回诊断字符串（frames/visible/painted/write）。
2. 在 native 侧接入：加载 FrameCapture，调用 runAndCapture("roomeqwizard.RoomEQ_Wizard", …, 20000, 1600, 1000)。
3. `MAKE=0`、HAP 构建成功、设备安装成功。

## 结果

设备日志里只有 `java2d-render-test: OK`，**没有 FrameCapture 行**，也没有 rew-window.png。
说明那段代码没有被执行到。

## 可能原因

1. native 里的 `FindClass("ohos/awt/FrameCapture")` 失败（类名或路径不对）；
2. 代码插入位置在 `goto after_main` 之前，但 label 或控制流有问题；
3. 编译时该分支被优化/跳过。

## 下一轮

1. 在 native 里打印 FindClass 结果；
2. 把 goto/label 改成正常的 if-else 结构；
3. 重建后确认 FrameCapture 日志出现。
