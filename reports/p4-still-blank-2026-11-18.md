# P4：强制可见 + printAll 仍未出内容 · 2026-11-18

## 本轮尝试

1. `makeTreeVisible()` 递归把整棵组件树 `setVisible(true)`；
2. `paintAll` 改为 `printAll`（打印路径，绕过 RepaintManager 双缓冲）。

结果：PNG 仍是 7080 字节 = **纯白**（与上轮白底版同样大小），组件一笔未画。

## 说明什么

可见性与双缓冲都不是原因。剩下的可能：

1. **FlatLaf 的 painter** 依赖 `GraphicsConfiguration` 的真实能力（离屏 GC 与屏幕 GC 不同），
   可能在 `SunGraphics2D` 上判定为“不可绘制”；
2. REW 的 contentPane 子树在 depth>3 深处，可能**根本没有实际内容组件**（界面还在异步构建中）；
3. 组件树的 layout 虽然 valid，但 `paint` 需要的 `RepaintManager` 状态未初始化。

## 下一轮（换方向）

1. 把 dumpTree 深度提到 10，确认 contentPane 子树到底有多少实际控件；
2. 若子树为空 → 说明 REW 的界面是**延迟构建**（等事件循环跑起来才 add），需要更长的等待或触发；
3. 若子树非空 → 临时切换到 Metal L&F 重跑一次，验证是否 FlatLaf 特有问题。

## 阶段判断

P4 已经连续多轮在原地打转（Frame 尺寸→组件树→绘制分发）。
建议下一轮先做深度树确认“到底有没有内容”，再决定继续还是转入交付收尾。
