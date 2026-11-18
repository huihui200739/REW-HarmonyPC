# 🎉🎉 P4 完成：REW 界面在 HarmonyOS 屏幕上显示成功 · 2026-11-18

![REW on screen](img/rew-onscreen-2026-11-18.png)

## 设备日志（决定性证据）

    REWJVMN: SurfaceBridge class=found register=0
    REWXC:  BlitFrame 1600x1000

## 截屏内容（1600x1000 的 REW 界面，位于 HAP 窗口内）

- 标签页：**Soundcard / Cal files / Comms / Analysis / Equaliser / View**
- Sample rate `48 kHz`、Output/Input device 下拉框、Buffer `32k`
- Stereo only / Virtual balanced input 复选框
- Input options：Invert / High pass、Sweep level `-12.0 dBFS`
- Soundcard calibration：Calibrate soundcard… / Make cal file…
- Levels：Check levels… / Generate debug file…
- **三个电平表**：Out / In / Ref In（−10 ~ −100 dBFS 刻度清晰）
- Help 区域：完整英文说明，可读

## 完整链路（全部打通）

```
REW jar → OpenJDK 17 → java.desktop → ohos.awt 后端 → Java2D 软件渲染
   → BufferedImage.paint() → int[] ARGB
   → ohos.awt.SurfaceBridge.blit()  (JNI)
   → BlitFrame()  (native)
   → OH_NativeWindow RequestBuffer → mmap → memcpy → FlushBuffer
   → XComponent surface → 屏幕
```

## 本轮修复

| 问题 | 修复 |
|---|---|
| `UnsatisfiedLinkError: SurfaceBridge.blit` | 注册代码之前**根本没插入**（脚本静默失败）；本次用 marker 精确定位后插入 |
| `ld.lld: undefined symbol: BlitFrame` | `BlitFrame` 原在匿名 namespace 内（内部链接），移出后可见 |
| `SET_BUFFER_GEOMETRY` 无效 | 必须在 `RequestBuffer` **之前**调用 |

## 里程碑意义

**T1 完整达成**：REW 的主界面不仅在离屏渲染出来，而且真正显示在了 HarmonyOS PC 的屏幕上。

从第 1 轮 OpenJDK 交叉编译到第 106 轮界面显示，共 106 轮。

## 仍待完成

| 项 | 说明 |
|---|---|
| 输入事件 | XComponent 触摸 → AWT EventQueue（界面可看不可点） |
| Java Sound | OHAudio 后端（设备下拉框空） |
| JIT | 仅 -Xint |
| 真机验证 | 仅模拟器 |
| 签名/侧载/飞书 | 缺证书密码 |
