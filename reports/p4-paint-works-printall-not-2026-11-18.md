# P4 决定性诊断：paint() 能画出内容（nonMag 3 万+），printAll() 不能 · 2026-11-18

## 实验

对每个 Frame 做两件事：
1. 用主图 `printAll(g)`（原流程）→ 输出仍纯白；
2. 另建品红底图，对 contentPane 调 **`paint(pg)`**，统计非品红像素。

## 结果

    clip=null nonMag=0        <- Frame 1（SharedOwnerFrame，空，正常）
    clip=null nonMag=31831    <- Frame 2（JFrame）
    clip=null nonMag=32747    <- Frame 3（REW 主窗口）

**`paint()` 画出了 3 万多个像素**——组件渲染完全正常。

## 结论

| 方法 | 结果 |
|---|---|
| `printAll(g)` | ❌ 空白（打印路径在我们的离屏 Graphics 上被跳过） |
| `paint(g)` | ✅ 正常渲染 |

`printAll` 走的是 `Printable`/`printComp` 路径，会检查 `Graphics` 是否为 `PrintGraphics`，
不满足就跳过——这解释了为什么之前所有 `printAll` 尝试都是空白。

## 下一轮（一行改动）

把 `f.printAll(g)` 改回 `f.paint(g)`（对 contentPane 调），保留品红底做对比，
就能拿到 REW 的真实界面图像。
