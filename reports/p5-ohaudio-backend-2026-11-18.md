# P5：OHAudio 真实音频后端已实现并编译通过 · 2026-11-18

## 本轮完成

新增 `app/entry/src/main/cpp/audio_bridge.cpp`（约 180 行）：

| 组件 | 实现 |
|---|---|
| `RingBuffer` | 256 KB 环形缓冲（约 0.7 s @48k/16bit/stereo），带互斥 |
| `OnWriteData` | OHAudio 写入回调，从环形缓冲取数据，不足补静音 |
| `OnError` | 错误回调（hilog）|
| `nativeOpen(sr, ch, bits)` | 创建 `OH_AudioStreamBuilder` → Renderer → Start |
| `nativeWrite(handle, data, off, len)` | 写入环形缓冲 |
| `nativeClose(handle)` | Stop → Release → 释放缓冲 |

Java 侧 `OhosSourceDataLine` 接入：

    open()  → nativeOpen(格式采样率/聲道/位深)
    write() → nativeWrite(handle, b, off, len)
    close() → nativeClose(handle)

失败时自动回退到“丢弃数据”模式（不抛异常，REW 不会崩）。

## 构建

    CMakeLists: + audio_bridge.cpp, 链接 ohaudio
    java.desktop-jmod: MAKE=0
    HAP: BUILD SUCCESSFUL

## 踩的坑

| 坑 | 说明 |
|---|---|
| `native_audiocallback.h` 不存在 | 实际是 `native_audiorenderer.h` |
| hvigor 签名密码校验 | `signingConfigs` 里的明文密码 <32 位会 FAIL，构建时须清空（签名用 `scripts/sign-hap.sh` 后处理）|

## 未验证

**模拟器上听不到声音**，无法验证真实播放。代码路径已完整：

    OhosSourceDataLine.write → nativeWrite → ringWrite
      → OH_AudioRenderer 回调 → OnWriteData → ringRead → 系统音频

需要在有音频输出的设备（真机）上验证。
