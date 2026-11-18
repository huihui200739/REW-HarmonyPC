# P5 音频：最小 MixerProvider 已实现（验证被干扰）· 2026-11-18

## 本轮完成

新增 `ohos.sound.OhosMixerProvider`（`javax.sound.sampled.spi.MixerProvider` 子类）：

| 组件 | 内容 |
|---|---|
| Mixer.Info ×2 | `OHOS Audio Out` / `OHOS Audio In`（vendor=OHOS） |
| Mixer 实现 | 完整实现 `Mixer` 接口；`getLine` 抛 `LineUnavailableException`（音频 I/O 未实现） |
| SPI 注册 | `META-INF/services/javax.sound.sampled.spi.MixerProvider` → `ohos.sound.OhosMixerProvider` |

`MAKE=0`，runtime 与 HAP 重建成功。

## 目的

让 `AudioSystem.getMixerInfo()` 返回非空，REW 的 Output device / Input device 下拉框能显示设备名
（当前为空，因为 Java Sound 完全没实现）。

## 验证受阻

ChimeraX 第三次回到前台遮挡窗口，无法截屏确认下拉框是否已有内容。

## 说明

这是**枚举层的桩实现**：设备名有、能选中，但打开音频线会抛异常。
真正的音频 I/O（OHAudio 后端）仍需单独实现。

## 下一轮

1. 想办法让 ChimeraX 不再抢前台（`aa force-stop` 后它会被系统重启）；
2. 或在无人干扰时截屏确认下拉框内容；
3. 之后才谈 OHAudio 真实音频 I/O。
