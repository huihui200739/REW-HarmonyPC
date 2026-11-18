# P5：音频后端编译就位，但 REW 未主动打开音频线 · 2026-11-18

## 本轮验证

部署含 OHAudio 后端的 HAP，启动等 5 分钟：

    REWXC: BlitFrame 1600x1000    ← 渲染正常
    （无 REWAUDIO 日志）           ← 音频线未被打开

## 原因

REW 只在以下动作时才打开音频线：

1. 点 **Check levels...**（会打开输出+输入线做电平检测）；
2. 点 **Generate debug file...**；
3. 真正开始一次测量。

界面静态显示时不会碰音频。所以没有 `REWAUDIO` 日志是**预期行为**，不是 bug。

## 要验证音频路径，需要

1. **点击交互可用**（当前被模拟器环境阻塞）；
2. 或加一个“启动自检”代码路径，主动打开一次音频线。

## 方案 2 更实际

在 `FrameCapture` 里加一段自检：

```java
Mixer.Info[] infos = AudioSystem.getMixerInfo();
Mixer mixer = AudioSystem.getMixer(infos[0]);
SourceDataLine line = (SourceDataLine) mixer.getLine(
    new DataLine.Info(SourceDataLine.class, new AudioFormat(48000f, 16, 2, true, false)));
line.open();
line.start();
line.write(new byte[48000 * 4], 0, 48000 * 4);  // 1 秒静音
line.close();
```

这样能验证：Mixer 枚举 → getLine → nativeOpen → Renderer 启动 全链路。

## 下一轮

把这段自检加进 probe，看是否出现 `REWAUDIO: renderer started`。
