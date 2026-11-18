# P4：REW 组件树完整，布局有效（1600x1000 / valid=true）· 2026-11-18

## 完整组件树（写入文件后拉取）

    {SwingUtilities$SharedOwnerFrame 1600x1000 vis=false valid=true; kids=0}
    [1600x1000 comps=0 valid=true]
    {javax.swing.JFrame 1600x1000 vis=false valid=false; kids=1
      JRootPane 1600x1000 vis=true valid=false; kids=2
        JPanel 10x10 vis=false valid=false; kids=0
        JLayeredPane 10x10 vis=true valid=false; kids=1
          JPanel 10x10 vis=true valid=false; kids=1}
    [1600x1000 comps=1 valid=false]
    {roomeqwizard.g 1600x1000 vis=false valid=true; kids=1        <- REW 主窗口
      JRootPane 1600x1000 vis=true valid=true; kids=2
        JPanel 1600x1000 vis=false valid=true; kids=0              <- contentPane
        JLayeredPane 1600x1000 vis=true valid=true; kids=2
          JPanel 1600x967 vis=true valid=true; kids=0
          roomeqwizard.XE 1600x33 vis=true valid=true; kids=7}     <- 底部工具条
    [REW V5.31.3 1600x1000 comps=1 valid=true]

## 判断

1. **REW 主窗口（`roomeqwizard.g`）尺寸 1600x1000、valid=true、布局已生效**；
2. 底部工具条 `roomeqwizard.XE` 有 7 个子组件，尺寸 1600x33——结构完整；
3. contentPane 的 JPanel 尺寸 1600x1000 但 kids=0，其内容在更深层（dumpTree 限制 depth<=3 被截断）。

## 因此“黑图”不是布局问题，而是绘制问题

树的尺寸/valid 全对，说明布局链完好。空白只能来自绘制环节：

1. `paintAll` 可能被 Swing 的双缓冲（RepaintManager）短路；
2. FlatLaf 的 `FlatPanelUI`/painter 依赖 `GraphicsConfiguration` 的真实能力；
3. 我们给的离屏 `SunGraphics2D` 的 `Composite`/`Paint` 状态可能与 FlatLaf 期望不符。

## 下一轮

1. 把 dumpTree 深度提到 8，看 contentPane 的子树到底有多少组件；
2. 试 `printAll(g)`（走打印渲染路径，绕过 RepaintManager 双缓冲）；
3. 给离屏 Graphics 设 `setColor(Color.WHITE)` 先 `fillRect` 背景——当前黑底说明连背景都没画。
