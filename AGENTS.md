# REW 鸿蒙 PC 适配 —— 工作约定

## 硬事实

- 上游 REW V5.31.3，install4j 打包，**Java 8** 运行时，闭源（EULA）。仓库内不放二进制。
- `rewtools/rew` 是 Go 项目，与 REW 无关，别再引用。
- 目标平台 OpenHarmony 6.1.1.125 / API 24 / arm64 / 2in1。

## 工作方式

1. 任何结论先落证据：命令、输出、时间、SHA256，写进 `docs/`。
2. 改动分层：`jvm/`（JDK 构建与补丁）、`platform/`（图形/音频后端）、`app/`（ArkTS 宿主）互不混写。
3. 每次构建产物路径、耗时、失败点记录在 `reports/`。
4. 设备侧动作一律走 `tools/portkit/portkit.py device exec`（单模拟器串行锁），不直接裸调 hdc。
5. 不做"能启动就算适配"的结论：T1 以下不算可用。

## 提交流程

沿用 `~/Documents/鸿蒙pc适配/鸿蒙PC移植-提交手册.md`：镜像 → 组织仓 issue → push → 签名 HAP → 侧载站 → 飞书登记。
