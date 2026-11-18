# P4 根因找到：REW 弹了错误对话框，阻塞在 invokeAndWait · 2026-11-18

## 线程堆栈（决定性证据）

    -- REW-main WAITING
       at java.lang.Object.wait
       at java.awt.EventQueue.invokeAndWait
       at javax.swing.SwingUtilities.invokeAndWait
       at org.jdesktop.swingx.JXErrorPane.showDialog
       at org.jdesktop.swingx.JXErrorPane.showDialog
       at roomeqwizard.RoomEQ_Wizard.B(y:3692)
       at roomeqwizard.RoomEQ_Wizard.main(y:5534)

## 结论

**REW 启动时遇到了一个错误，弹出了 JXErrorPane 错误对话框，模态阻塞等待用户点击。**

这完美解释了之前所有现象：

| 现象 | 原因 |
|---|---|
| 组件树只有菜单栏 | 主界面构建被错误对话框阻塞 |
| contentPane 为空 | 构建流程停在 `B(y:3692)` 之后 |
| 60/120 秒等待无效 | 对话框不会自己关闭 |
| 黑图 | 没有内容可画 |

## 前几轮的方向全部错了

我一直在追“绘制问题”（双缓冲、可见性、FlatLaf、printAll），
实际上 **UI 构建根本没完成**，因为一个错误对话框在等人点击。

## 下一轮（关键）

1. 遍历 `Window.getWindows()` 找到所有 Dialog；
2. 递归读取对话框里的 `JLabel`/`JTextArea`/`JEditorPane` 文本，把错误消息写出来；
3. 一旦知道错误内容，就知道 REW 卡在哪（八成是音频设备/文件路径/图形环境）；
4. 对症修复后，界面应该能正常构建。
