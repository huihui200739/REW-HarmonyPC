# P4：setVisible(false) 关闭模态对话框仍然卡死 · 2026-11-18

## 现象

    hilog: FrameCapture method=found
    （无 result 行）
    rew-window.png 不存在

与 doClick 版本同样卡住。

## 分析

模态对话框在 Swing 里通过 `SecondaryLoop` 让 EDT 进入嵌套事件循环。
我们从 FrameCapture 线程调用 `invokeLater` + `sleep`，事件虽会被模态 pump 处理，
但 `Dialog.dispose()` 会等待 modal loop 退出，而 modal loop 又在等 EDT 空闲——
形成**双向等待**。

## 务实的结论

程序化关闭 REW 的模态对话框在这套 stub peer 下不可靠。更稳的三条路：

1. **不关闭，直接接受现状**——抓到的图就是“带对话框的 REW”，
   已经能证明 REW 跑起来了（对话框本身就是 REW 的界面元素）；
2. **在 REW 启动前就消掉对话框的触发条件**（例如让升级检查失败、跳过许可证提示）；
3. **用 Robot 模拟点击**（需要真实 peer 的 input 注入，当前不具备）。

## 建议

P4 已经消耗 12 轮。当前能确定交付的事实：
- JVM 在 OHOS 跑起来（T0）；
- REW 主类加载并执行到 UI 构建（T0）；
- Java2D 渲染管线可用（有 PNG 证据）；
- REW 的窗口/菜单栏/对话框都在（有组件树与文本证据）。

建议把“界面完整渲染”列为已知限制，转入交付收尾（HAP 签名、侧载站、飞书登记）。
