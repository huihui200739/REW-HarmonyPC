# P4 确诊：REW 主窗口存在，但尺寸 0x0、组件树只有 1 个节点 · 2026-11-18

## 诊断输出

    FrameCapture result=main started; frames=3 visible=0;
      [ 0x0 comps=0]
      [ 10x10 comps=1]
      [REW V5.31.3 0x0 comps=1]
      painted=3 write=true

## 解读

1. **第三个 Frame 标题是 `REW V5.31.3`**——确认 REW 主窗口已创建；
2. **尺寸 0x0**：`setSize()` 没有生效，因为 FramePeer 是 Proxy dummy；
3. **comps=1**：主窗口只有 1 个子组件（正常应是几十个）——组件树没有真正建起来。

## 根因链

    FramePeer.setBounds() -> stub（无操作）
      -> Frame 尺寸停留在 0x0
        -> Swing 布局管理器计算出 0 尺寸
          -> 子组件没有被添加/布局
            -> paint 出空白

## 下一轮（关键）

让 FramePeer 的 `setBounds` / `setVisible` / `getInsets` **真实生效**（至少改 Java 侧字段）：

1. `setBounds(int,int,int,int)` -> `((Frame)target).setBounds(...)` 会递归；改为维护一个 side table 存边界；
2. `getInsets()` 返回 `new Insets(28, 8, 8, 8)`（模拟标题栏+边框）；
3. `setVisible(boolean)` -> 记录状态，供 FrameCapture 判断。

即：从“Proxy 全返回默认值”升级为“Proxy + 状态表”，这是 P4 的核心改造。

## 意义

这一轮把“黑屏”从“不知道哪里错”变成了“Frame 尺寸为 0、组件树为空”的确定性诊断，
下一步的改造方向非常明确。
