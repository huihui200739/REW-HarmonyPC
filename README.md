# REW (Room EQ Wizard) · 鸿蒙 PC 适配

把 [Room EQ Wizard](https://www.roomeqwizard.com/)（REW，房间声学测量与分析软件）完整跑到 HarmonyOS PC 上：
**不改界面、不重写功能**，而是在 OHOS 上补齐它需要的运行时（JVM + AWT/Swing + Java Sound），让官方原版 jar 直接启动。

| 项 | 值 |
|---|---|
| 上游 | REW V5.31.3（2024-07-25，John Mulcahy / roomeqwizard.com），install4j 打包 |
| 许可证 | 上游为免费但**闭源**软件，遵循其 EULA（[eula.html](https://www.roomeqwizard.com/eula.html)）；本仓库只含适配层代码，**不分发 REW 二进制** |
| 运行时依赖 | 官方说明：Linux 版需 **Java 8** 运行时（`REW_linux_no_jre_5_31_3.sh`） |
| 目标平台 | OpenHarmony 6.1.1.125 / API 24，arm64，deviceType 2in1 |
| 适配路线 | 运行时移植（JVM → AWT/Java2D → Java Sound → 平台层），非重写 |
| 状态 | P0 事实核查完成，P1 起步 |

## 为什么不是"搬源码"

排查结论（详见 [docs/FACTS-2026-11-18.md](docs/FACTS-2026-11-18.md)）：

- REW 没有公开源码仓库。GitHub 上同名的 `rewtools/rew` 是 Go 写的构建胶囊工具，**与 Room EQ Wizard 无关**。
- 官方只发布 Win/macOS/Linux 安装包（install4j 生成），本体是 Java 应用。
- 因此"界面和图标与原版一致"只有一条路：**让原版 jar 在 OHOS 上跑起来**。任何重写方案都做不到 UI 一致。

## 技术路线（四层）

```
┌──────────────────────────────────────────────┐
│ REW 原版 jar（不动）                          │  app/  (安装后抽取，不入库)
├──────────────────────────────────────────────┤
│ Java 8 类库 + Swing/Java2D + Java Sound      │  jvm/
├──────────────────────────────────────────────┤
│ OHOS 移植层：Java2D→NativeWindow/Skia         │  platform/
│              Java Sound→OHAudio                │
│              文件对话框/剪贴板/打印/DPI/字体    │
├──────────────────────────────────────────────┤
│ ArkTS 宿主 HAP（窗口、生命周期、权限）          │  app/
└──────────────────────────────────────────────┘
```

## 能力分级（T0/T1/T2）

| 级别 | 内容 | 状态 |
|---|---|---|
| **T0 基础** | OpenJDK 8 在 OHOS arm64 启动 `java -version`；REW jar 进入主循环并创建首帧 | 未开始 |
| **T1 主要** | 主界面完整渲染（菜单/工具栏/图表区）；`.mdat` 读写；音频设备枚举；频率响应曲线绘制 | 未开始 |
| **T2 增强** | 扫频测量全链路（输出+输入）；EQ 计算与导出；打印/PDF；多显示器 DPI；快捷��；Pro 多输入 | 未开始 |

严格口径：T1 以下不算"能用的 REW"。

## 阶段计划

| 阶段 | 内容 | 出口标准 |
|---|---|---|
| P0 | 事实核查、安装包结构分析、依赖清单 | jar/so/配置清单入库文档 |
| P1 | OpenJDK 8 交叉编译到 OHOS arm64（headless） | `java -version` 在模拟器里跑通 |
| P2 | AWT/Swing 图形后端（Java2D → OHOS NativeWindow） | REW 主窗口出图 |
| P3 | Java Sound SPI（→ OHAudio） | 设备枚举 + 扫频回环 |
| P4 | 平台层（文件/剪贴板/打印/DPI/字体） | T2 功能项通过 |
| P5 | 打包 HAP、签名、侧载站投稿、社区组织仓申请 | 提交完成 |

## 目录

| 路径 | 说明 |
|---|---|
| `docs/` | 事实核查、路线、验证记录 |
| `jvm/` | OpenJDK 8 for OHOS 的构建脚本与补丁 |
| `platform/` | Java2D / Java Sound / 平台层移植代码 |
| `app/` | ArkTS 宿主工程（HAP） |
| `patches/` | 对 OpenJDK / 平台层的补丁，`scripts/verify.sh` 校验哈希 |
| `reports/` | 设备侧验证产物 |

## 构建与验证

```bash
scripts/verify.sh            # 补丁哈希 + 依赖核查 + 构建入口检查
scripts/verify.sh --device   # 追加设备侧冒烟（需模拟器在线，走 portkit 设备锁）
```

## 已知边界

- 本仓库**不提供** REW 安装包或抽取出的 jar；使用前请自行从官网获取并遵守 EULA。
- OpenJDK 8 目前没有官方 OHOS 目标，需自建交叉编译链（musl/OHOS libc、Skia 后端）。
- Pro 功能依赖官方授权，仅做本地运行验证。
