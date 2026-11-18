# P4 完整答案：REW 弹了两个对话框（升级提示 + 启动错误）· 2026-11-18

## 采集到的窗口文本

    [WINDOW roomeqwizard.g] File | Tools | Preferences | Graph | Help | Donate | Pro Upgrades |
    [WINDOW roomeqwizard.iE title=Multi-input capture upgrade]
        Multi-input capture allows REW to capture multiple inputs simultaneously ...
        A license is valid for a single user ... Paste license code here then click Verify license
        Buy license | Verify license |
    [WINDOW BasicErrorPaneUI$JXErrorDialog title=Error during startup]
        java.lang.reflect.InvocationTargetException occurred during startup
        See Details for more information, please send it to feedback@roomeqwizard.com
        Close | Report Error | Details >> |

## 结论

REW 启动时弹了**两个模态对话框**：

1. **`roomeqwizard.iE`**——多输入捕获升级提示（Pro 功能推广）；
2. **`JXErrorDialog`**——启动错误 `InvocationTargetException`。

主窗口 `roomeqwizard.g` 的菜单栏已建好（File/Tools/Preferences/Graph/Help/Donate/Pro Upgrades），
但 contentPane 要等这两个对话框关闭后才继续构建——这就是“界面只有菜单栏”的原因。

## 两个待解问题

1. **对话框的 Close 按钮点了会发生什么**？（需要程序化关闭）
2. **InvocationTargetException 的真实 cause 是什么**？（需要读 Details 的完整文本）

## 下一轮

1. 读取 `JXErrorDialog` 里 Details 的 JTextArea 完整内容（错误堆栈）；
2. 在 FrameCapture 里程序化点击 Close（`AbstractButton.doClick()`），让 REW 继续构建界面；
3. 两个对话框都关掉后，界面应该能完整出现——到那时再截图就是真正的 REW 界面。
