# P4：组件树递归诊断已生效（日志被截断）· 2026-11-18

## 输出（hilog 单行截断）

    FrameCapture result=main started; frames=3 visible=0; {javax.swing.SwingUtilities$SharedOwnerFrame 1600x1000 vis=false valid=true; kids=0

## 已确认

1. 递归 dumpTree 正常工作；
2. 第一个 Frame 是 `SwingUtilities$SharedOwnerFrame`（Swing 内部隐藏窗口），1600x1000、kids=0 属正常；
3. `valid=true`。

## 问题

hilog 单行有长度上限，组件树信息太长被截断，看不到真正的 REW 主窗口（第三个 Frame）的子树。

## 下一轮

把组件树**写成文件**（`rew-tree.txt`）而不是打日志，然后 hdc file recv 拉下来看：

    Files.writeString(Path.of(outPath + ".tree.txt"), tree);

这样不受日志长度限制，能完整看到 rootPane → contentPane → 各面板的尺寸链。
