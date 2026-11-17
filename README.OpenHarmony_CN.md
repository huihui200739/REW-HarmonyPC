# REW 鸿蒙 PC 适配说明

## 环境

| 项 | 版本 |
|---|---|
| DevEco Studio | 6.x |
| HarmonyOS SDK | OpenHarmony 6.1.1.125 / API 24（arm64） |
| 目标设备 | PC（deviceType = 2in1）|
| 上游 | REW V5.31.3（需 Java 8 运行时，官方闭源） |

## 先决条件

1. 自行从官网下载 REW（本仓库不分发二进制）：
   `https://www.roomeqwizard.com/installers/REW_linux_no_jre_5_31_3.sh`
2. 本机具备 OpenHarmony SDK 与 hdc（DevEco 自带）。

## 获取代码

```bash
git clone https://github.com/huihui200739/REW-HarmonyPC.git
cd REW-HarmonyPC
```

## 验证

```bash
scripts/verify.sh              # 补丁哈希 + 依赖清单 + 构建入口检查
scripts/verify.sh --device     # 追加设备侧冒烟（单模拟器串行锁）
```

## 目录

| 路径 | 说明 |
|---|---|
| `docs/` | 事实核查（FACTS）、JVM 路线（JVM-ROUTE）、验证记录（PGC-VALIDATION-*） |
| `jvm/` | OpenJDK for OHOS 构建脚本与补丁 |
| `platform/` | Java2D / Java Sound / 平台层移植 |
| `app/` | ArkTS 宿主工程（HAP） |
| `patches/` | 补丁（哈希由 `source.lock.json` 校验） |

## 阶段状态

| 阶段 | 状态 |
|---|---|
| P0 事实核查 | 完成（见 `docs/FACTS-2026-11-18.md`） |
| P1 OpenJDK for OHOS | 进行中 |
| P2 Java2D/Swing 后端 | 未开始 |
| P3 Java Sound → OHAudio | 未开始 |
| P4 平台层（文件/DPI/打印） | 未开始 |
| P5 HAP 打包与提交 | 未开始 |

## 问题排查

| 现象 | 处理 |
|---|---|
| 找不到 REW jar | 本仓库不含二进制，需自行从官网安装包解出 |
| `java -version` 在设备上失败 | 检查 `libjvm.so` 的 ABI 与 `/system/etc/ld-musl` 查找路径 |
| 主窗口黑屏 | Java2D 后端未接上 NativeWindow，先用离屏渲染验证链路 |
| 无法录音 | 检查 HAP 的 `ohos.permission.MICROPHONE` 与系统音频路由 |
