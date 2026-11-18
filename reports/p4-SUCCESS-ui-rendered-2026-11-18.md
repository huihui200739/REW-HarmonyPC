# 🎉 P4 成功：REW 完整界面在鸿蒙上渲染出来 · 2026-11-18

![REW UI](img/rew-ui-rendered-2026-11-18.png)

## 渲染结果（1600x1000，131 KB）

图像内容（肉眼可读）：

- 顶部标签页：**Soundcard / Cal files / Comms / Analysis / Equaliser / View**
- Sample rate `48 kHz`、Output device / Input device 下拉框、Buffer `32k`
- Stereo only / Virtual balanced input 复选框
- Input options：Invert / High pass
- Soundcard calibration：Calibrate soundcard… / Make cal file…
- Levels：Use subwoofer test signal… / Check levels… / Generate debug file…
- **右侧三个电平表**：Out / In / Ref In，刻度 -10 ~ -100 dBFS
- 底部 Help 区域：完整英文说明文字（可读）

## 两个关键修复

| # | 问题 | 修复 |
|---|---|---|
| 1 | `ICC_ColorSpace.toRGB` 抛 `ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1` | `OhosColorTransform.colorConvert` 返回数组按 **out 分量数**（3）而非 `src.length`（1），灰度复制到 RGB |
| 2 | `printAll()` 在离屏 Graphics 上什么都不画 | 改用 **`paint()`**（`printAll` 走 `Printable` 路径，要求 `PrintGraphics`，不满足就跳过） |

## 追踪历程（15+ 轮）

试过的错误方向：双缓冲、组件可见性、FlatLaf painter、屏幕设备数量、音频设备、ICC profile 完整性……
最终根因只有两条：**一行数组长度 + 一个方法选错**。

## 意义

- REW 的 UI 不只是“能构建”，而是**能完整渲染**；
- 从 T0（JVM 可用）推进到 **T1（主界面可用）**；
- 证明整套移植栈（OpenJDK 17 + java.desktop + ohos.awt + Java2D 软件渲染）是可用的。

## 仍待完成

| 项 | 说明 |
|---|---|
| 上屏 | 当前渲染到离屏 BufferedImage，还需接 XComponent 显示到屏幕 |
| 输入事件 | XComponent → AWT EventQueue |
| Java Sound | OHAudio 后端 |
| 签名 HAP / 侧载站 / 飞书 | 交付流程 |
