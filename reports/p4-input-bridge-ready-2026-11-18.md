# P4：输入事件桥接已就位（InputBridge class=found）· 2026-11-18

## 本轮完成

1. JDK 新增 `ohos.awt.InputBridge`：构造 `MouseEvent`（PRESSED/RELEASED/MOVED）并 `EventQueue.invokeLater` 派发到目标 Frame；
2. native 侧：
   - `DispatchInput(type, x, y)`：`GetJavaVM` + `AttachCurrentThread` + `CallStaticVoidMethod`；
   - `DispatchTouchEventCB`：读 `OH_NativeXComponent_TouchEvent`，按 DOWN/UP/MOVE 调用 `DispatchInput`；
3. JNI 初始化时把 `InputBridge` 类存为 global ref。

## 设备日志

    REWJVMN: InputBridge class=found
    REWXC:  BlitFrame 1600x1000

## 交互验证受阻

点击 `Cal files` 标签时，另一个应用（ChimeraX）仍在最前，`uitest uiInput click` 的坐标落在 ChimeraX 上。
截屏显示 REW 窗口在后方（能看到右侧黑色电平表与部分界面）。

## 已确认可用

| 项 | 状态 |
|---|---|
| InputBridge 类注册 | ✅ class=found |
| 触摸事件回调 | ✅ 代码就位 |
| BlitFrame 渲染 | ✅ 1600x1000 |
| 点击交互实测 | ⚠️ 被其他应用遮挡，未验证 |
