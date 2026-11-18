# P4：尺寸已修好（1600x1000），comps=1 其实是 rootPane · 2026-11-18

## 等待 60 秒后的诊断

    frames=3 visible=0;
      [ 1600x1000 comps=0 valid=true]
      [ 1600x1000 comps=1 valid=false]
      [REW V5.31.3 1600x1000 comps=1 valid=true]
    painted=3 write=true

## 进展

- 尺寸问题**已解决**：setSize 后所有 Frame 都是 1600x1000；
- `valid=true`（REW 主窗口布局已生效）；
- `comps=1` 是 **正常的**——JFrame 的直接子组件就是 rootPane（JRootPane），
  真正的界面在 rootPane → contentPane → 各面板之下。

## 仍为空白的可能原因

1. `paintAll` 理论上递归，但 Swing 的双缓冲机制（`RepaintManager`）可能在离屏 Graphics 上跳过绘制；
2. 子组件尺寸仍为 0（rootPane 布局未把尺寸传给 contentPane）；
3. REW 用了自定义 L&F（FlatLaf），其 painter 可能依赖真实 peer 的图形配置。

## 下一轮

1. 递归打印组件树的类名/尺寸/是否可见（2-3 层即可），定位哪一层断了；
2. 若 rootPane 有尺寸但 contentPane 为 0，说明布局没传播，需要手动 `rootPane.layout()`；
3. 或改用 `printAll(g)`（走打印路径，绕过双缓冲）。

## 意义

窗口尺寸、valid 状态都已正常，剩下的纯粹是“绘制内容为什么空”的问题，
范围已经收窄到 Swing 绘制链的最后一环。
