# REW 安装包与运行时探测 · 2026-11-18

## 1. 安装包

| 项 | 值 |
|---|---|
| 文件 | `REW_linux_no_jre_5_31_3.sh`（install4j 自解压，unix media） |
| 大小 | 31,882,909 B |
| SHA-256 | `a9a1a4295a22049d9459528032a6eea8d162e8519c6e533101b63085fffbd426` |
| install4j 版本 | 10.0.8（build 10147） |
| applicationId | 4549-9647-2313-4375 |
| **minJavaVersion** | **1.8** |
| **maxJavaVersion** | **1.8**（安装器只接受 Java 8） |
| 压缩 | pack200Compression=true |

安装（macOS 上以 Rosetta + Temurin 8u512 静默执行）：

```bash
INSTALL4J_JAVA_HOME_OVERRIDE=<jdk8> sh REW_linux_no_jre_5_31_3.sh -q -dir <dest> -overwrite
```

## 2. 安装出来的内容（25 个 jar）

| jar | 大小 | 作用 | OHOS 依赖 |
|---|---|---|---|
| `RoomEQ_Wizard_obf.jar` | 27,182,145 | 主程序（yGuard 3.1.0 混淆） | 纯 Java + JNA |
| `JTransforms-3.1-with-dependencies.jar` | 271,667 | FFT / 频谱 | 纯 Java |
| `flatlaf-3.4.1.jar`（+extras +swingx） | 874,502 | Swing 外观（HiDPI） | 纯 Java |
| `jna-4.2.0.jar` | 1,091,541 | native 调用 | **需 libjffi（native）** |
| `usb4java-1.2.0.jar` | 45,417 | USB 麦克风 | **需 libusb（native）** |
| `comm.jar` | 28,043 | 串口 | 需 serial native（可放弃） |
| `csjsound-provider-1.0.jar` | 41,858 | Java Sound SPI 实现 | 走 javax.sound.sampled |
| `jl1.0.1.jar` / `mp3spi` / `tritonus_share` | — | MP3 解码 | 纯 Java |
| `jsvg` / `svgSalamander` | — | SVG 渲染 | 纯 Java |
| `jh.jar` | 501,098 | JavaHelp 帮助系统 | Swing |
| `parallelcolt-0.9.4.jar` | 358 | 并行集合 | 纯 Java |
| `snakeyaml` / `json` / `slf4j` / `mfl-core` / `vecmath` / `TableLayout` | — | 配置/日志/数学 | 纯 Java |

主 jar 清单：

```
Main-Class: roomeqwizard.RoomEQ_Wizard
Class-Path:  jh.jar comm.jar parallelcolt-0.8.jar vecmath.jar TableLayout.jar AppleJavaExtensions.jar
Created-By: yGuard Bytecode Obfuscator 3.1.0
```

## 3. 字节码版本

`javap -verbose` 对 `roomeqwizard/RoomEQ_Wizard.class`：**major version 52**（Java 8 编译）。
⇒ JDK 17/21 可以加载（不触发 `UnsupportedClassVersionError`）。

## 4. 启动探测（macOS / Temurin 21.0.13）

| 步骤 | 结果 |
|---|---|
| `java -cp *` 启动（缺 install4j API） | `NoClassDefFoundError: com/install4j/api/launcher/StartupNotification$Listener` |
| 补 `.install4j/i4jruntime.jar` 后 headless 启动 | 无崩溃、无异常输出（15s 内保持运行） |

结论：
- **Java 8 → 21 的类加载不构成阻断**，JDK 21（Harmonybrew 已有 OHOS 补丁）是可走的路线，JDK 8 交叉编译可以先不做。
- 仍需验证：Swing 初始化（headless 模式下被跳过）、JNA/USB 的 native 部分、音频设备枚举。

## 5. 对 OHOS 适配的直接结论

1. 主 jar 混淆且闭源 ⇒ **只能改运行时**，不能改 REW 代码。
2. 纯 Java 依赖（FlatLaf / JTransforms / jsvg / snakeyaml）在 OHOS 上不需要额外 native。
3. 需要额外移植的 native 库：
   - `libjffi`（JNA 4.2.0）→ 需按 OHOS arm64 重编，libffi 有现成 OHOS 经验；
   - `libusb`（usb4java）→ USB 麦克风支持，优先级低于板载音频；
   - 串口（comm.jar）→ 桌面 PC 场景可放弃，README 里标注为已知边界。
4. 音频走 `javax.sound.sampled` SPI（`csjsound-provider` 证明上游就是这么接的）⇒ 我们的 OHOS 侧只要实现 `MixerProvider` 即可，不必改 REW。
5. 图形是 Swing + FlatLaf + Java2D ⇒ AWT 后端是主战场。
