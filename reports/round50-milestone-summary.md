# 第 50 轮阶段总结 · 2026-11-18

## 已完成（可复现、有证据）

| 阶段 | 内容 | 证据 |
|---|---|---|
| P1 | OpenJDK 17 交叉编译到 OHOS arm64 | libjvm.so 20MB、AArch64、JNI_CreateJavaVM、无 GLIBC |
| P1 | java.base 最小 runtime | 41MB、10 个 .so 全 AArch64 |
| P1 | java.desktop 全量 native | 8 个库（awt/awt_headless/fontmanager/freetype/jawt/lcms/mlib_image/javajpeg） |
| P1 | JVM 在 HAP 内启动 | java.version=17.0.17-internal、FindClass OK |
| P2 | REW 主类加载 + main 执行 | 走通 L&F、事件线程、组件树、pack() |
| P2 | REW 稳定运行 | 进程存活、stderr 无异常 |
| P2 | 自建 AWT 后端 | OhosToolkit/GraphicsEnvironment/Device/Configuration/Peers（下沉进 java.desktop） |
| P2 | identity CMM | 绕开 lcms，profile 数据直通 |
| P3 | 像素链路 | BufferedImage + setRGB + ImageIO 全通 |
| P3 | SurfaceData | 未通（pData 字段读到 1，已定位为字段 ID 错位） |

## 卡点

`SurfaceData_SetOps` 读到 `existing=0x1`（应为 0）。已确认不是重复注册，
疑似 pData 字段 ID 指向了相邻的 `valid` 字段。当前已实现 native side table 替代方案，
编译通过，设备验证待确认。

## 未开始

- 窗口上屏（FramePeer 真实创建 OHOS 窗口 + 像素传输）
- 输入事件（XComponent → AWT EventQueue）
- Java Sound（OHAudio 后端）
- HAP 签名、社区提交材料

## 建议

若 side table 仍不通，建议转入交付准备：
1. 仓库整理（README 能力分级、docs/PGC-VALIDATION、scripts/verify.sh 已具备）；
2. 把当前成果如实记录为 T1 部分达成（运行时可用，界面未上屏）；
3. 准备 AtomGit 镜像与组织仓申请材料。
