# P4：界面在异步构建中（ID$1 出现），main 诊断代码未生效 · 2026-11-18

## 新观察（相比上一轮）

上一次组件树：

    JFrame ... JLayeredPane kids=1 / JPanel 10x10 kids=1

这一次：

    JFrame ... JLayeredPane kids=1 / JPanel 1600x1000 kids=1
      roomeqwizard.ID$1 1600x1000 vis=true valid=true; kids=0

**出现了 `roomeqwizard.ID$1`**（REW 的内部组件），且尺寸从 10x10 变成 1600x1000。

说明：**REW 的界面是在事件循环里异步构建的**，60 秒等待期间还在陆续 add 组件。
主窗口 `roomeqwizard.g` 仍然只有菜单栏（XE 的 7 个 JMenu），contentPane 依旧空。

## 本轮未拿到的东西

想加的两项诊断（`MAIN-ERROR` 段 + `getAllStackTraces` 堆栈）没有出现在 tree 文件里，
说明插入的代码没有被执行到（可能在 `detail` 的 append 之后又覆盖，或插入位置在 return 之后）。

## 下一轮

1. 修好诊断插入位置（把堆栈写进 **单独的** stack.txt，不要混进 detail）；
2. 同时把等待时间提到 120 秒，看界面是否继续构建（异步是否有终点）；
3. 若能确认 main 卡在哪一行，就能对症下药。

## 正面信号

`roomeqwizard.ID$1` 的出现证明 **UI 构建没有死**，只是慢或被阻塞在某一步。
这与“绘制问题”的旧假设完全不同。
