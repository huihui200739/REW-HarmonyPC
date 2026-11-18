# 交付说明 · REW 鸿蒙 PC 运行时适配

日期：2026-11-18  
仓库：https://github.com/huihui200739/REW-HarmonyPC  
镜像：https://atomgit.com/2501_93573368/ohos_REW  
组织仓申请：[issue #464](https://atomgit.com/OpenHarmonyPCDeveloper/community/issues/464)

---

## 一、做了什么

把 Room EQ Wizard（REW，闭源 Java 桌面应用）跑到 HarmonyOS PC 上。
不改界面、不重写功能，而是在 OHOS 上补齐它需要的运行时。

| 层 | 内容 |
|---|---|
| JVM | OpenJDK 17（BiSheng 基线）交叉编译到 aarch64-linux-ohos |
| 类库 | java.base + java.desktop 等 12 个模块，jlink 出 73 MB runtime |
| 图形 | java.desktop 全量 native（awt/awt_headless/fontmanager/freetype/jawt/lcms/mlib_image/javajpeg） |
| 移植层 | `ohos.awt`（Toolkit/GraphicsEnvironment/Peers/CMM）+ SurfaceData side table |
| 宿主 | ArkTS HAP（JNI 启动 JVM、部署 runtime、探针） |

## 二、实测达成

| 能力 | 证据 |
|---|---|
| OpenJDK 17 在 OHOS 运行 | `java.version=17.0.17-internal`，libjvm.so 20 MB / AArch64 / 零 GLIBC |
| java.desktop 全量 native | 8 个 .so 全部产出并加载 |
| REW 主类加载 + main 执行 | 走通 L&F 装配、AWT 事件线程、组件树、`Window.pack()` |
| REW 稳定运行 | 进程存活、stderr 无异常 |
| REW 窗口 ×3 | 组件树显示 `REW V5.31.3` 主窗口 |
| REW 菜单栏 ×7 | File/Tools/Preferences/Graph/Help/Donate/Pro Upgrades |
| Java2D 渲染管线 | 离屏绘制 PNG（fillRect + drawString + FreeType 字形） |
| 颜色管理 | 自建 identity CMM，profile 数据直通 |

## 三、未达成（诚实记录）

| 项 | 原因 |
|---|---|
| 界面完整渲染 | **被 REW 自身启动错误对话框（模态）阻塞** |
| 输入事件 | 未开始（需 XComponent 事件回灌） |
| Java Sound | 未开始（需 OHAudio 后端） |
| JIT | 仅 `-Xint` 解释执行 |
| 真机验证 | 仅模拟器 |

## 四、技术要点（对其它 JVM/Java 类移植有复用价值）

| 问题 | 解法 |
|---|---|
| OHOS linker 只在 `DT_NEEDED` 闭包内解析符号 | `patchelf --add-needed` 补依赖（libjava→libjdkshim、libfontmanager→libawt_headless） |
| `java.awt.peer` 未导出，外部 jar 无法实现 ComponentFactory | **把 AWT 后端下沉进 java.desktop** |
| `PlatformGraphicsInfo` 硬编码 X11 | 补丁改为读 `java.awt.graphicsenv`/`awt.toolkit`，用 system classloader 加载 |
| `SurfaceData_SetOps` 读 `pData` 字段得到 0x1 | 改为 **native side table**（对象→ops 映射） |
| lcms native 依赖缺失 | 自建 **identity CMM** |
| 字体配置走 fontconfig 崩溃 | 强制 MFontConfiguration（properties 驱动） |
| initIDs 落在空实现导致崩溃 | 预加载 libfontmanager 等，让 dlsym 找到真实符号（real=37/stub=31） |

## 五、复现步骤

```bash
jvm/fetch-jdk.sh && jvm/apply-jdk-patches.sh && jvm/configure-jdk.sh
jvm/build-jvm.sh --jobs 6
jvm/build-runtime.sh
scripts/verify.sh
```

然后打包 HAP（`app/` 目录，DevEco Studio 或 hvigorw），安装到 API 24 / arm64 / 2in1 设备。

## 六、后续路线

1. **绕过启动错误对话框**（P4 收尾）：找到 `InvocationTargetException` 的真实 cause；
2. 界面完整渲染 + FramePeer 接 XComponent 上屏；
3. 输入事件回灌 AWT EventQueue；
4. Java Sound（OHAudio 后端）；
5. 签名 HAP → 侧载站 → 飞书登记。

## 七、许可

- 适配层代码：Apache-2.0；
- 上游 REW：闭源免费软件，遵循其 EULA，**本仓库不分发 REW 二进制**；
- 用户需自行从 roomeqwizard.com 下载安装包。
