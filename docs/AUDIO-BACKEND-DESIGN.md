# Java Sound 音频后端设计（OHAudio）

> 当前状态：**枚举层已实现（纯 Java 桩），真实音频 I/O 未实现。**
> 本文给出接入 OHAudio 的完整路径。

## 现状

| 组件 | 实现 | 状态 |
|---|---|---|
| `ohos.sound.OhosMixerProvider` | 纯 Java，SPI 注册，提供 2 个虚拟 Mixer | ✅ |
| `ohos.sound.OhosSourceDataLine` | 纯 Java，`write()` 丢弃数据 | ✅ |
| `AudioSystem.getMixerInfo()` | 返回 `OHOS Audio Out` / `OHOS Audio In` | ✅ |
| 真实播放 / 采集 | — | ❌ |

## 为什么不用 JDK 自带的 libjsound

`src/java.desktop/share/native/libjsound/` 存在（DirectAudioDevice.c 等），但：

1. 我们的构建开了 `--enable-headless-only`，补丁里跳过了 jsound 的编译；
2. Linux 版 jsound 依赖 **ALSA**（`libasound`），OHOS 没有；
3. 即使编出来，`DirectAudioDevice` 也找不到 ALSA 设备。

所以必须自己实现 native 层。

## 设计：Java Sound → JNI → OHAudio

```
REW (javax.sound.sampled)
  ↓
ohos.sound.OhosSourceDataLine.write(byte[], off, len)
  ↓  JNI
Java_ohos_sound_OhosSourceDataLine_nativeWrite(env, cls, byteArray, off, len)
  ↓
OH_AudioStreamBuilder (OHAudio)
  ↓
OH_AudioRenderer_WriteData / callback
  ↓
系统音频输出
```

### 关键 API（OHAudio，OHOS 4.0+）

```c
OH_AudioStreamBuilder_Create(&builder, AUDIOSTREAM_TYPE_RENDERER);
OH_AudioStreamBuilder_SetSamplingRate(builder, 48000);
OH_AudioStreamBuilder_SetChannelCount(builder, 2);
OH_AudioStreamBuilder_SetSampleFormat(builder, AUDIOSTREAM_SAMPLE_S16LE);
OH_AudioStreamBuilder_SetEncodingType(builder, AUDIOSTREAM_ENCODING_TYPE_RAW);
OH_AudioStreamBuilder_SetRendererCallback(builder, callback, userData);
OH_AudioStreamBuilder_SetRendererWriteDataCallback(builder, writeCb, userData);
OH_AudioStreamBuilder_GenerateRenderer(builder, &renderer);
OH_AudioRenderer_Start(renderer);
```

写入回调里从 Java 侧的环形缓冲区取数据：

```c
static OH_AudioData_Callback_Result WriteCb(OH_AudioRenderer* r, void* userData,
                                            void* audioData, int32_t audioDataSize) {
    RingBuffer* rb = (RingBuffer*) userData;
    rb_read(rb, audioData, audioDataSize);
    return AUDIO_DATA_CALLBACK_RESULT_VALID;
}
```

### Java 侧改造

```java
public final class OhosSourceDataLine implements SourceDataLine {
    private static native long nativeOpen(int sampleRate, int channels, int bits);
    private static native int  nativeWrite(long handle, byte[] data, int off, int len);
    private static native void nativeClose(long handle);

    @Override public int write(byte[] b, int off, int len) {
        return handle != 0 ? nativeWrite(handle, b, off, len) : len;
    }
}
```

## 构建改动

| 项 | 改动 |
|---|---|
| `CMakeLists.txt` | `target_link_libraries(rewjvm PRIVATE ohaudio)` |
| 头文件 | `#include <ohaudio/native_audiostreambuilder.h>` |
| 权限 | `ohos.permission.MICROPHONE`（采集侧，播放不需要）|

## 工作量预估

| 步骤 | 轮次 |
|---|---|
| 播放路径（SourceDataLine + 环形缓冲） | 2-3 |
| 采集路径（TargetDataLine） | 2-3 |
| 设备热插拔 / 采样率协商 | 1-2 |

共约 **5-8 轮**，且需要能验证音频输出的环境（当前模拟器的音频通路未验证）。

## 为什么现在不做

1. REW 的核心价值是**测量分析**，音频 I/O 是它的输入输出通道；没有真实音频，测量链路跑不起来；
2. 但当前模拟器**无法验证音频**（听不到、抓不到）；
3. 按“先能看见，再能听见”的顺序，界面已达成，音频应作为下一阶段。
