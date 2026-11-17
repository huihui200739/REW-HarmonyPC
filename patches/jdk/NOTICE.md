# 补丁来源

本目录三个补丁来自社区项目 **ohos_eclipse-ide**（`OpenHarmonyPCDeveloper`，
MIT License, Copyright (c) 2026 aycxd0528），用于把 BiSheng JDK 17 交叉编译到
OpenHarmony；原样保留以维持与上游基线的可复现性。

| 补丁 | 作用 |
|---|---|
| 0001-separate-macos-build-sysroot.patch | 分离 macOS 构建机 SDK 与 OHOS 目标 sysroot |
| 0002-add-minimal-ohos-configure-mode.patch | 新增 `--enable-ohos-port` 最小配置模式 |
| 0003-adapt-hotspot-to-ohos-libc.patch | HotSpot 适配 OHOS libc（utmpx/dlinfo、阻止 glibc `.symver`） |

我们自己的增量补丁（java.desktop / AWT 后端）另起编号 01xx- 追加在本目录。
