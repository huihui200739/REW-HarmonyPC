# JVM 路线评估 · 2026-11-18

## 1. 约束

| 约束 | 来源 | 影响 |
|---|---|---|
| REW 官方要求 **Java 8** | 官网下载页 "Linux installer (requires a Java 8 runtime)" | 首选 JDK 8；若 REW 能在 17/21 上跑，工作量大幅下降 |
| OHOS 无官方 JVM | 平台现状 | 必须自建交叉编译链 |
| OHOS 用户空间是 musl，非 glibc | Harmonybrew 文档 | HotSpot 的 os_linux 目标要打 musl + 沙箱补丁 |
| 应用沙箱 / 代码签名 / W^X | 同上 | JIT 需要可执行内存；可能被迫走 interpreter 模式（`-Xint`） |

## 2. 已有社区基础（可复用）

**Harmonybrew/homebrew-core** 维护了 OpenJDK 的 OHOS 适配补丁：

| 项 | 内容 |
|---|---|
| 补丁目录 | `Patches/openjdk/`、`Patches/openjdk@17/`、`@21/`、`@25/` |
| 基础分支 | bishengjdk-17 等 |
| 核心手法 | autoconf 里 `TOOLCHAIN_DETECT_TARGET_OHOS` 检测 `__OHOS__` 宏，产出 `OPENJDK_TARGET_IS_OHOS`，再在 `flags-cflags.m4`、`lib-std.m4`、`toolchain.m4` 分支 |
| 覆盖内容 | 构建系统检测、OS 沙箱适配、POSIX API 降级、`UseCompressedClassPointers` 调整（21） |

结论：**JDK 17/21 的 OHOS 移植已有可复用补丁；JDK 8 没有现成 OHOS 补丁。**

## 3. 决策树

```
REW jar 能否在 JDK 21 上启动？
├─ 能 → 走 Harmonybrew openjdk@21 补丁，JDK 8 交叉编译完全不必要   ← 优先验证
└─ 不能（模块系统/移除 API/反射失败）
   └─ 自建 OpenJDK 8 的 OHOS 补丁（autoconf 老版本、无 __OHOS__ 支持，需要手写）
```

验证动作（本机即可，不需要 OHOS）：
1. macOS 上解包 REW 的 install4j 安装包，拿到 jar 与 launcher 配置；
2. 分别用 JDK 8 / 21 `java -jar` 启动，记录退出码与异常；
3. 把结果写进 `docs/REW-JAR-RUNTIME-PROBE.md`。

## 4. 图形后端

REW 是 Java 桌面应用（install4j + Swing/Java2D）。OHOS 上没有 X11/Wayland，需要提供 AWT 后端：

| 方案 | 说明 |
|---|---|
| **Caciocavallo** | OpenJDK 的"可插拔 AWT 后端"参考实现，已有 X11 / EGL / Wayland 后端；把它的 EGL 后端换成 OHOS NativeWindow + Skia 是现实起点 |
| 自研 | 直接实现 `sun.awt` 的 GraphicsEnvironment/GraphicsDevice/Graphics2D 管线（工作量大，但可控） |
| Virtual BufferedImage | 只做离屏渲染 + 上传纹理到 ArkUI（最快出图，代价是输入/光标要自己接） |

PoC 顺序：先 Virtual 后端把首帧画出来（证明链路），再换 NativeWindow 直绘（证明性能）。

## 5. 音频后端

REW 的核心功能是测量，必须有音频 I/O：

- Java Sound 的 `AudioSystem` SPI（`javax.sound.sampled.spi.MixerProvider`）→ 实现一个 OHOS MixerProvider；
- 底层对接 OHAudio：`OH_AudioRenderer` / `OH_AudioCapturer`（Native API，N-API 或 C 桥接进 Java 用 JNI）；
- 采样率/位深/通道布局要按 REW 默认（48k/24bit/mono 输入）对齐。

## 6. 风险清单

| # | 风险 | 影响 | 缓解 |
|---|---|---|---|
| 1 | OHOS 沙箱禁止 JIT 可执行内存 | 只能解释执行，REW 测量算力可能不够 | 先测 `-Xint` 性能；必要时热点函数改 native |
| 2 | Swing 里大量依赖 X11/字体/光标 | 后端实现面积大 | 先用无头渲染跑通，再补输入 |
| 3 | Java 8 → 21 的 API 迁移问题 | REW 启动失败 | 先实测；失败再评估 JDK 8 自建 |
| 4 | 音频权限与路由（麦克风） | 测量拿不到输入 | 提前申请 ohos.permission.MICROPHONE 并测延迟 |
| 5 | 上游闭源，无法改 REW 代码 | 只能改运行时 | 所有适配必须在 JVM/平台层完成 |
