# P4：3 个 Frame 都 paint 了，但输出全黑 · 2026-11-18

## 结果

    FrameCapture result=main started; frames=3 visible=0; painted=3 write=true

PNG 1600x1000 / 4745 字节 = **纯黑**。

## 原因分析

`painted=3` 说明 paint 调用发生了，但组件没画出内容。最可能：

1. **Frame 的尺寸是 0 或未布局**——因为没有真实 FramePeer，`setSize()` 只改 Java 字段，
   但 `validate()` 依赖 peer 的 `getInsets()` 等，我们的 stub 返回 0，可能让布局塌陷；
2. `paint()` 与 `paintAll()` 的差别：`paint` 只画自身，`paintAll` 会递归子组件；
3. REW 的 UI 在 `JB`（内部类）里构造，可能依赖真实 peer 才创建子组件。

## 下一轮

1. 在 paint 前强制 `f.setSize(1600, 1000)` 并检查 `f.getWidth()/getHeight()`；
2. 改用 `paintAll(g)` 递归绘制；
3. 打印 `f.getComponentCount()` 看子组件是否真的存在；
4. 若子组件数为 0，说明组件树没建（Peer 的 createXxx 返回 stub 导致 add 失败）。

## 意义

这一步已经从“进程能跑”走到“窗口对象存在、paint 被调用”，
剩下的是**布局与递归绘制**，属于确定的工程量，不再是未知障碍。
