# P1：把 JVM 跑上 OHOS —— 路线、可复用资产、我们的增量

## 1. 结论先行

- **不必从零啃 OpenJDK**：社区已有可复现的 OHOS JVM 构建链，我们直接复用其参数与补丁。
- **真正的空白是 `java.desktop`（AWT/Swing）**：参考实现明确停在 `java.base`（headless），
  并在构建 `java.desktop/libawt` 时因 sysroot 缺 X11 失败。REW 是 Swing + Java2D 重度用户，
  这一块正是我们要做的。
- 因此 P1 拆成两段：**P1-a 复现 JVM 与 java.base runtime（照抄参考路线）**、
  **P1-b 给 java.desktop 造 OHOS 后端（原创工作量）**。

## 2. 参考实现（MIT，可直接借鉴）

| 项 | 值 |
|---|---|
| 仓库 | `https://atomgit.com/OpenHarmonyPCDeveloper/ohos_eclipse-ide`（分支 `feature/gate5-device-verification`） |
| 许可证 | MIT（Copyright 2026 aycxd0528） |
| 文档 | `docs/JVM_PORTING.md` |
| 源码基线 | BiSheng JDK 17，`gitee.com/openeuler/bishengjdk-17` @ `e366af4e348dbbc9b987ed23982565de398dd0e3` |
| JDK 许可证 | GPL-2.0-only WITH Classpath-exception-2.0 |

已验证通过的环节：

1. `configure` 在 macOS AArch64 主机完成（目标 `aarch64-unknown-linux-musl`，实际 `aarch64-linux-ohos12.0.0`）；
2. `hotspot-server-libs` 完整编译链接，`libjvm.so` 为 AArch64 ELF、导出 `JNI_CreateJavaVM`、无 `GLIBC_*` 符号；
3. HAP 打包（`entry/libs/arm64-v8a`）与最小 runtime image（`jlink --add-modules java.base`，约 41 MB → `rawfile/jre-runtime.zip` 约 13 MB）；
4. 设备侧安装链与验证脚本。

## 3. 关键构建参数

```bash
BOOT_JDK=<host JDK 17>
SYSROOT=$OHOS_NDK_ROOT/sysroot
OHOS_TARGET=aarch64-linux-ohos12.0.0
OPENJDK_TARGET=aarch64-unknown-linux-musl

CC  = <NDK>/clang   --target=$OHOS_TARGET
CXX = <NDK>/clang++ --target=$OHOS_TARGET

configure \
  --openjdk-target=$OPENJDK_TARGET \
  --with-toolchain-type=clang \
  --with-sysroot=$SYSROOT \
  --with-boot-jdk=$BOOT_JDK \
  --with-build-jdk=$BOOT_JDK \
  --with-jvm-variants=server \
  --with-debug-level=release \
  --with-stdc++lib=dynamic \
  --enable-headless-only \
  --enable-ohos-port \
  --disable-warnings-as-errors \
  --with-native-debug-symbols=none \
  --with-conf-name=ohos-aarch64
```

三个补丁（共 297 行）：

| 补丁 | 作用 |
|---|---|
| `0001-separate-macos-build-sysroot.patch` | 分离 macOS 构建机 SDK 与 OHOS 目标 sysroot |
| `0002-add-minimal-ohos-configure-mode.patch` | 新增 `--enable-ohos-port` 最小配置模式 |
| `0003-adapt-hotspot-to-ohos-libc.patch` | HotSpot 适配 OHOS libc（utmpx/dlinfo 等、阻止 glibc `.symver`） |

## 4. 我们的增量：java.desktop / AWT / Swing

REW 的 UI 是 Swing + FlatLaf + Java2D，还用到 `java.awt.Color`、字体、图标（SVG 经 jsvg）、JavaHelp。

| 子任务 | 内容 | 出口标准 |
|---|---|---|
| P1-b1 | 去掉 headless 限制，让 `java.desktop` 在 sysroot 缺 X11/Fontconfig/CUPS 时**编译通过** | `libawt.so`、`libawt_headless.so`、`libfontmanager.so` 产出 |
| P1-b2 | 实现 **OHOS AWT 后端**：`GraphicsEnvironment`/`GraphicsDevice` → NativeWindow + Skia | 离屏画 BufferedImage 并回读像素 |
| P1-b3 | 接到 ArkUI Surface（XComponent）显示、输入事件回灌 | REW 主窗口在模拟器里出图 |
| P1-b4 | 字体发现路径（Noto/思源） | 中文界面不缺字 |

技术选择：
- 不接 X11、也不模拟 X11（避免额外进程与性能损失）；
- 先做**离屏渲染 + 纹理上传**（最快出图），再评估 NativeWindow 直绘；
- 输入法/光标/DPI 放 P4。

## 5. 阶段分解与验收

| 步骤 | 动作 | 验证 |
|---|---|---|
| P1-a1 | 拉 BiSheng JDK 17 @ e366af4e | `git rev-parse HEAD` 匹配 |
| P1-a2 | 应用 3 个补丁 + configure | `configure` 无错退出 |
| P1-a3 | 构建 hotspot-server-libs | `libjvm.so` AArch64、`JNI_CreateJavaVM`、无 GLIBC 符号 |
| P1-a4 | 最小 runtime（java.base）+ HAP | 设备上 `java -version` 通过 |
| P1-b1 | java.desktop 编译通过 | 三个 .so 产出 |
| P1-b2 | 离屏 Graphics2D | 像素回读正确 |
| P1-b3 | REW 主窗口上屏 | 截图与原版对比 |

## 6. 风险

| 风险 | 说明 | 应对 |
|---|---|---|
| `java.desktop` 依赖 X11 头文件 | 编译期就要 `X11/Xlib.h` | stub 头 + 编译期屏蔽，运行期走自研后端 |
| JIT 在 OHOS 沙箱 | 可执行内存可能被禁 | 先 `-Xint` 验证功能，再测性能 |
| JNA/libusb native | 需要 arm64 OHOS 版 | libjffi 基于 libffi 移植；usb4java 后置 |
| 上游补丁绑定 JDK 17 | REW 声明要 Java 8 | 已实测 class 版本 52 可在 21 加载，17 亦可 |
