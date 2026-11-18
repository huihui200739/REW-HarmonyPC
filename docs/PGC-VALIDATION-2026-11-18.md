# PGC 验证记录 · REW · 2026-11-18

| 项 | 值 |
|---|---|
| 代码基线 | 本仓库 `main` 分支（适配层）+ BiSheng JDK 17 @ `e366af4e348dbbc9b987ed23982565de398dd0e3` |
| 构建时间 | 2026-11-18 |
| 平台 | OpenHarmony 6.1.1.125 / API 24（emulator 6.1.0.125），arm64，deviceType 2in1 |
| 应用 | `com.example.rewharmony`（debug，未签名 HAP） |
| runtime | 73 MB（12 个 jlink 模块），20 个 .so 全 AArch64、零 GLIBC 引用 |
| Java | OpenJDK 17.0.17-internal（BiSheng 基线交叉编译） |

## 验证范围

| # | 项目 | 方法 | 结果 |
|---|---|---|---|
| 1 | libjvm.so 交叉编译 | ELF/符号/依赖检查 | ✅ AArch64、导出 `JNI_CreateJavaVM`、无 `GLIBC_*` |
| 2 | java.base runtime | jlink + 全 .so 校验 | ✅ 41 MB、10 个 .so |
| 3 | java.desktop native | 编译产出 | ✅ 8 个库 |
| 4 | JVM 在 HAP 内启动 | 设备内 JNI | ✅ `java.version=17.0.17-internal` |
| 5 | REW 主类加载 | `FindClass("roomeqwizard/RoomEQ_Wizard")` | ✅ |
| 6 | REW main 执行 | 设备日志 | ✅ 走通 L&F / EDT / 组件树 / `Window.pack()` |
| 7 | REW 稳定运行 | 进程存活 + stderr | ✅ 无异常 |
| 8 | **Java2D 渲染** | 离屏画图 + ImageIO | ✅ 见 [证据图](img/java2d-render-2026-11-18.png) |
| 9 | 界面上屏 | — | ⬜ 未达成（P4） |
| 10 | 音频 | — | ⬜ 未达成（P5） |

## Java2D 渲染证据

`docs/img/java2d-render-2026-11-18.png`（400×200）：白底 + 黑色横线 + 文字
**"REW on HarmonyOS - Java2D OK"**，字形经 FreeType 渲染，无锯齿。

生成路径：HAP 内 JNI → `BufferedImage` → `Graphics2D` → `fillRect/drawString` → `ImageIO.write`。

## 已知边界

- 界面尚未上屏（FramePeer 仍是 dummy peer，只建组件树不建窗口）。
- 仅 `-Xint` 解释执行，JIT 未验证。
- Java Sound 未实现（OHAudio 后端待做）。
- 未在真机验证，仅模拟器。

## 诚实结论

**T0 达成**：JVM 可用、REW 能启动并稳定运行。
**T1 部分**：图形管线（Java2D）已通，但界面未上屏，因此"能用的 REW"尚未成立。
