# P4 收敛：REW 被自身启动错误对话框阻塞（确定结论）· 2026-11-18

## 完整采集（stack3.txt，11271 字节）

    WINDOW SwingUtilities$SharedOwnerFrame
    WINDOW roomeqwizard.JB
    WINDOW javax.swing.JFrame
    WINDOW roomeqwizard.g            -> 菜单栏：File / Tools / Preferences / Graph / Help / Donate / Pro Upgrades
    WINDOW roomeqwizard.iE           -> Multi-input capture upgrade（Pro 推广，模态）
    WINDOW BasicErrorPaneUI$JXErrorDialog title=Error during startup
                                     -> java.lang.reflect.InvocationTargetException occurred during startup
                                     -> Close | Report Error | Details >>

## 确定结论

**REW 在 OHOS 上能启动、能建窗口、能建菜单栏、能弹对话框；
但启动过程中抛了 `InvocationTargetException`，被 REW 自己的 JXErrorPane 捕获并弹模态框，
导致主界面构建停在那里。**

## 为什么没法继续前进

1. 该对话框是**模态**的，从外部线程关闭会与 `SecondaryLoop` 双向等待（已验证两次都会卡死）；
2. 错误 cause 藏在 Details（HTML/JTextArea），未展开时采集不到完整堆栈；
3. 要修复根因必须先看到 cause，要看到 cause 必须先展开 Details——**鸡生蛋**。

## 已达成（有证据）

| 项 | 证据 |
|---|---|
| OpenJDK 17 在 OHOS 运行 | java.version=17.0.17-internal |
| java.desktop 全量 native | 8 个库 |
| REW 主类加载 + main 执行 | FrameCapture: main started |
| REW 窗口 3 个（含主窗口 REW V5.31.3） | 组件树 |
| REW 菜单栏 7 项 | 组件树 + 文本 |
| REW 的两个对话框 | 组件树 + 文本 |
| Java2D 渲染管线 | PNG 证据 |

## 下一步建议

P4 到此为止（13 轮），转入交付收尾：
1. HAP 签名（DevEco 登录华为账号）；
2. 侧载站投稿；
3. 飞书任务清单登记；
4. 把“界面被启动错误对话框阻塞”作为已知限制写入 README/PGC。
