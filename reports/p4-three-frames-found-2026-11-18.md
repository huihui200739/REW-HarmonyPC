# P4：REW 已创建 3 个 Frame，只差“可见”标志 · 2026-11-18

## FrameCapture 实跑结果

    FrameCapture class=found
    FrameCapture method=found
    FrameCapture result=main started; frames=3 visible=0; painted=0 write=true

= 
- REW main 在后台线程成功启动；
- **REW 创建了 3 个 Frame 对象**（主窗口 + 可能的回放/工具栏窗）；
- `visible=0`：因为我们的 FramePeer 是 Proxy dummy，`setVisible(true)` 不改变 Java 侧状态；
- `painted=0`：FrameCapture 里用 `isVisible()` 过滤，全被跳过；
- `write=true`：PNG 写出成功（4745 字节空白图）。

## 意义

**REW 的窗口层级结构是真实存在的**，组件树已建、窗口对象已建，
只是“可见性”这个布尔值没被设置。这不是渲染能力问题，是 Peer 语义问题。

## 下一轮（一行改动）

FrameCapture 不要用 `isVisible()` 过滤，直接对所有 Frame 做 `doLayout() + paint()`：

    for (Frame f : Frame.getFrames()) {
        f.doLayout();
        f.paint(g);
        painted[0]++;
    }

预期即可得到 REW 主界面的真实渲染图。
