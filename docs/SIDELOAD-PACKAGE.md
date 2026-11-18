# 侧载提交包清单（可直接上传）· 2026-11-18

## 文件

| 项 | 值 |
|---|---|
| 路径 | `/tmp/rew-sign/rew-signed.hap` |
| 大小 | 71 MB |
| SHA-256 | `700a4a45b4da92943d3e450d087f753d154eea48bbbe095817c339f56a9f5052` |
| 签名 | OpenHarmony 官方调试证书（`openharmony application profile debug`）|
| 安装验证 | ✅ `install bundle successfully`（HarmonyOS PC 模拟器 API 24）|
| bundleName | `com.example.rewharmony` |

## 投稿表单建议填法

| 字段 | 建议值 |
|---|---|
| 应用名称 | Room EQ Wizard (REW) |
| 版本 | 5.31.3 |
| 包名 | com.example.rewharmony |
| 分类 | 音频 / 专业工具 |
| 设备类型 | 2in1 |
| 系统要求 | OpenHarmony API 24 (6.1.1) / arm64 |
| 许可证 | Apache-2.0 |
| 源码仓库 | https://atomgit.com/2501_93573368/ohos_REW |
| 描述 | REW 房间声学测量软件的鸿蒙 PC 移植：OpenJDK 17 + java.desktop + Java2D 运行时适配，原版界面完整运行。 |

## 已知限制（建议在描述里写明）

- 音频后端已实现（OHAudio 播放 + 采集），但 REW 需交互触发才会打开音频线，物理声卡未实测；
- 触摸/键鼠交互已接入，模拟器侧的右键注入仍受限；
- 仅解释执行（`-Xint`）；
- 仅模拟器验证，未上真机。

## 为什么用调试证书

OpenHarmony 允许应用用 SDK 自带的调试证书签名做本地/社区侧载分发；
生产分发需要 AppGallery 的正式证书。本项目的定位是**社区侧载验证**，调试证书足够。
