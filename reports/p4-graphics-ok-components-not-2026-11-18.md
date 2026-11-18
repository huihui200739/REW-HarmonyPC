# P4 关键分离：离屏 Graphics 能画，Swing 组件不画 · 2026-11-18

## 实验

在 FrameCapture 里先 `g.setColor(WHITE); g.fillRect(0,0,1600,1000)` 再 `paintAll`。

结果：

- PNG 1600x1000 / 7080 字节 = **纯白**；
- 组件内容**依然一笔没有**。

## 结论（重要）

| 环节 | 结论 |
|---|---|
| 离屏 BufferedImage + SunGraphics2D | ✅ **能画**（白底成功） |
| Swing 组件的 paintAll | ❌ **不出内容** |

所以问题不在 Java2D 管线，而在 **Swing 的绘制分发**。

## 最可能的原因

1. **双缓冲**：`RepaintManager` 在离屏 Graphics 上走 `paintDoubleBuffered`，可能直接跳过；
2. **可见性传播**：`Container.paint` 遍历子组件时会 `if (child.isVisible()) child.paint(g)`，
   而 REW 的 Frame `vis=false`（我们的 Peer 没翻转该标志）；
3. **FlatLaf painter** 依赖 `GraphicsConfiguration` 能力（离屏 GC 与屏幕 GC 不同）。

## 下一轮（按优先级）

1. **递归强制 `setVisible(true)`** 整棵组件树后再 paint（最可能见效）；
2. 试 `printAll(g)` 而不是 `paintAll(g)`（打印路径绕过双缓冲）；
3. 若仍空，把 FlatLaf 临时切到 Metal L&F 验证是否为 L&F painter 问题。
