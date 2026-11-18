# P4：自动关闭对话框导致卡死（doClick 在 EDT 上阻塞）· 2026-11-18

## 现象

加入 `clickClose()`（遍历 Dialog 里的按钮并 `doClick(10)`）之后：

    hilog: FrameCapture method=found
    （没有 result 行）
    rew-window.png 不存在

即 FrameCapture 卡在 `EventQueue.invokeAndWait` 里没有返回，整个 probe 超时（PASS 0.1s 是被设备锁提前释放）。

## 原因

`AbstractButton.doClick()` 内部会 `Thread.sleep` 并触发 ActionListener，
模态对话框的 Close 监听可能又调用 `invokeAndWait`/`setVisible(false)`，
在 EDT 上形成**嵌套阻塞**（我们对模态窗口做同步操作）。

## 下一轮（改为非阻塞关闭）

1. 不要用 `doClick()`；直接 `win.setVisible(false)` + `win.dispose()`；
2. 或者用 `EventQueue.invokeLater` 异步关闭，不等待完成；
3. 关闭后再等 3 秒抓图。

## 已确认的事实（不受影响）

上一轮采集到的对话框文本已经证明：
- `roomeqwizard.iE`：多输入捕获升级提示；
- `JXErrorDialog`：`java.lang.reflect.InvocationTargetException occurred during startup`。

这两条是 P4 的确定结论，本轮的卡死只是关闭方式不当。
