# REW (Room EQ Wizard) · 鸿蒙 PC 适配

把 [Room EQ Wizard](https://www.roomeqwizard.com/)（REW，房间声学测量与分析软件）跑到 HarmonyOS PC 上：
**不改界面、不重写功能**，而是在 OHOS 上补齐它需要的运行时（JVM + AWT/Swing + Java Sound），让官方原版 jar 直接启动。

| 项 | 值 |
|---|---|
| 上游 | REW V5.31.3（2024-07-25，John Mulcahy / roomeqwizard.com），install4j 打包 |
| 许可证 | 上游免费但**闭源**（[EULA](https://www.roomeqwizard.com/eula.html)）；本仓库只含适配层代码，**不分发 REW 二进制** |
| 运行时 | 官方要求 Java 8；本项目实测 **OpenJDK 17** 可加载（class major 52），走 JDK 17 |
| 目标平台 | OpenHarmony 6.1.1.125 / API 24，arm64，deviceType 2in1 |
| 适配路线 | 运行时移植（JVM → AWT/Java2D → Java Sound），非重写 |
| 当前状态 | **P1/P2 完成，P3(Java2D) 渲染打通，P4(上屏) 进行中** |

## 当前进展（有实测证据）

| 能力 | 状态 | 证据 |
|---|---|---|
| OpenJDK 17 交叉编译到 OHOS | ✅ | `libjvm.so` 20MB / AArch64 / 导出 `JNI_CreateJavaVM` / 零 GLIBC 符号 |
| java.base 最小 runtime | ✅ | 41MB，10 个 .so 全 AArch64 |
| java.desktop 全量 native | ✅ | libawt / libawt_headless / libfontmanager / libfreetype / libjawt / liblcms / libmlib_image / libjavajpeg |
| JVM 在 HAP 内启动 | ✅ | `java.version=17.0.17-internal` |
| **REW 主类加载 + main 执行** | ✅ | 走通 L&F 装配、EDT、组件树、`Window.pack()` |
| **REW 稳定运行** | ✅ | 进程存活、stderr 无异常 |
| **Java2D 渲染出图** | ✅ | [证据图](docs/img/java2d-render-2026-11-18.png)：fillRect + drawString + FreeType 字形 |
| REW 界面上屏 | 🔄 | 需 FramePeer 接 OHOS 窗口（P4） |
| 输入事件 | ⬜ | 未开始 |
| Java Sound | ⬜ | 未开始（需 OHAudio 后端） |

## 能力分级（T0/T1/T2）

| 级别 | 内容 | 状态 |
|---|---|---|
| **T0 基础** | JVM 在 OHOS arm64 启动 `java -version`；REW jar 进入主循环 | ✅ **达成** |
| **T1 主要** | REW 主界面渲染（Java2D 管线可用）；`.mdat` 读写；音频设备枚举 | 🔄 渲染管线通，界面未上屏 |
| **T2 增强** | 扫频测量全链路；EQ 计算导出；打印/DPI/输入法 | ⬜ 未开始 |

严格口径：T1 以下不算"能用的 REW"。

## 为什么不是"搬源码"

详见 [docs/FACTS-2026-11-18.md](docs/FACTS-2026-11-18.md)：
REW 无公开源码（GitHub 同名 `rewtools/rew` 是无关的 Go 项目），只能让原版 jar 跑起来。

## 技术路线（四层）

```
REW 原版 jar（不动）
  ↓
OpenJDK 17（BiSheng 基线）+ java.desktop + java.base
  ↓
OHOS 移植层：ohos.awt（Toolkit/GraphicsEnvironment/Peers/CMM）+ SurfaceData side table
  ↓
ArkTS 宿主 HAP（XComponent 窗口、生命周期、权限）
```

## 复现

```bash
jvm/fetch-jdk.sh && jvm/apply-jdk-patches.sh && jvm/configure-jdk.sh
jvm/build-jvm.sh --jobs 6
jvm/build-runtime.sh
platform/build-awt-backend.sh   # 可选：外部 jar 形式的后端（当前已下沉进 java.desktop）
scripts/verify.sh               # 补丁哈希 + 依赖清单 + 构建入口检查
```

## 关键实现说明

| 问题 | 解法 |
|---|---|
| OHOS linker 只在 `DT_NEEDED` 闭包内解析符号 | `patchelf --add-needed` 补依赖（libjava→libjdkshim、libfontmanager→libawt_headless） |
| `java.awt.peer` 未导出，外部 jar 无法实现 ComponentFactory | **把后端下沉进 java.desktop**（`src/java.desktop/share/classes/ohos/`） |
| `PlatformGraphicsInfo` 硬编码 X11 | 补丁改为读 `java.awt.graphicsenv` / `awt.toolkit`，并用 system classloader 加载 |
| `SurfaceData_SetOps` 读 `pData` 字段得到 0x1 | 改为 **native side table**（对象→ops 映射） |
| lcms native 缺依赖 | 自建 **identity CMM**（纯 Java，profile 直通） |

## 目录

| 路径 | 说明 |
|---|---|
| `docs/` | 事实核查、路线、各阶段验证记录、证据图 |
| `jvm/` | OpenJDK 构建脚本、补丁应用、runtime 生成 |
| `platform/` | AWT 后端源码（与下沉进 JDK 的副本保持同步） |
| `patches/jdk/` | 对 BiSheng JDK 17 的补丁（0001-0008） |
| `app/` | ArkTS 宿主工程（JVM 探针 + runtime 部署） |
| `reports/` | 各轮验证记录（按日期） |
