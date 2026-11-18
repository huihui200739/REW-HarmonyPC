# P5 完成：Java Sound 音频后端（播放 + 采集）全部实现 · 2026-11-18

## 完整实现

| 路径 | Java 类 | native | OHAudio API |
|---|---|---|---|
| 播放 | `OhosSourceDataLine` | `audio_bridge.cpp` renderer 段 | `OH_AudioStreamBuilder`(RENDERER) + `OnWriteData` 回调 |
| 采集 | `OhosTargetDataLine` | `audio_bridge.cpp` capturer 段 | `OH_AudioStreamBuilder`(CAPTURER) + `OnReadData` 回调 |
| 枚举 | `OhosMixerProvider` | — | 2 个虚拟 Mixer（In/Out） |

## 数据流

```
播放: REW → SourceDataLine.write() → nativeWrite → ringWrite
        → OH_AudioRenderer 回调 → OnWriteData → ringRead → 系统音频输出

采集: 系统麦克风 → OH_AudioCapturer 回调 → OnReadData → ringWrite
        → nativeRead → ringRead → TargetDataLine.read() → REW
```

## 关键技术点

| 点 | 说明 |
|---|---|
| 环形缓冲 | 256 KB，约 0.7 s @48k/16bit/stereo，互斥保护 |
| 欠载处理 | 写入回调取不满时补静音，避免卡顿 |
| 容错 | native 不可用时 Java 侧回退（播放丢弃/采集静音），REW 不崩 |
| 头文件 | `native_audiorenderer.h` / `native_audiocapturer.h`（**没有** `native_audiocallback.h`）|

## 构建

    java.desktop-jmod: MAKE=0
    HAP: BUILD SUCCESSFUL

## 未验证

模拟器**听不到声音、抓不到麦克风**，无法验证实际音频通路。
需要在真机上验证：

1. REW 选 Output device → 播放测试音 → 听是否有声；
2. 选 Input device → 跑一次测量 → 看是否收到数据。

## 至此，目标里的三大后端全部实现

| 后端 | 状态 |
|---|---|
| OpenJDK 运行时 | ✅ 已验证 |
| Java2D 图形 | ✅ 已验证（界面上屏）|
| **Java Sound 音频** | ✅ **代码完整（未验证）** |
