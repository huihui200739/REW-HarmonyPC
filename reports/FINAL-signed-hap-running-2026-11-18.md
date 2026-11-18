# 🎉 最终验证：签名 HAP 运行，REW 界面完整显示 · 2026-11-18

![signed HAP running](img/rew-signed-hap-running-2026-11-18.png)

## 本轮

1. 安装 `rew-signed.hap`（OpenHarmony 官方调试证书签名，71 MB）；
2. `aa start -a EntryAbility -b com.example.rewharmony`；
3. 等待约 5 分钟（REW 构建 UI + 渲染）；
4. 截屏。

## 设备日志

    REWJVMN: SurfaceBridge class=found register=0
    REWXC:  BlitFrame 1600x1000

## 截屏内容（清晰无遮挡）

- 标签页：Soundcard / Cal files / Comms / Analysis / Equaliser / View
- Sample rate `48 kHz`、Output/Input device、Buffer `32k`
- Input options、Sweep level `-12.0 dBFS`、Treat 32-bit data as 24-bit（勾选）
- Soundcard calibration、Levels、Help 全文
- **三个电平表**：Out / In / Ref In（-10 ~ -100 dBFS 刻度清晰）

## 交付链完整闭环

```
源码（GitHub + AtomGit）
  → jvm/build-*.sh          OpenJDK 17 交叉编译
  → jvm/build-runtime.sh    jlink 73 MB runtime
  → hvigorw assembleHap     未签名 HAP（71 MB）
  → scripts/sign-hap.sh     OpenHarmony 调试证书签名
  → hdc install             install bundle successfully ✅
  → aa start                界面完整显示 ✅
```

## 至此，T1 完整达成

| 分级 | 内容 | 状态 |
|---|---|---|
| T0 | JVM 在 OHOS 可用 | ✅ |
| **T1** | **REW 原版界面完整显示（签名 HAP）** | ✅ **本轮最终验证** |
| T1+ | 触摸交互 | 代码完整，模拟器环境无法验证 |
| T2 | 音频枚举 | ✅ 桩实现 |
| T2 | 音频 I/O | ⬜ |
