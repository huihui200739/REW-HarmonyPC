# P2 关键纠正：需要实现 sun.awt.ComponentFactory 接口，而不是覆写 Toolkit.createXxx · 2026-11-18

## 定位

`AWTError: UI components are unsupported by: <toolkit>` 的抛出点：

    java/awt/Component.java:1268
    java/awt/MenuComponent.java:205

判断条件是 `Toolkit.getDefaultToolkit() instanceof sun.awt.ComponentFactory`。
也就是说：**Toolkit 必须实现 sun.awt.ComponentFactory 接口**，而不是覆写 createXxx 方法。

## 这解释了为什么之前的主意不对

我一直在 Toolkit 里覆写 createFrame/createButton...，但那些方法在 OHOS 的 Toolkit 基类里
并不存在（或者说不被用于组件创建判断）；真正的门是 ComponentFactory 这个内部接口。

ComponentFactory 的方法清单（约 25 个 create*，覆盖 Button/Canvas/Checkbox/
CheckboxMenuItem/Choice/Desktop/Dialog/FileDialog/Font/Frame/Label/Lightweight/List/
MenuBar/MenuItem/Menu/Panel/PopupMenu/Robot/ScrollPane/Scrollbar/Taskbar/TextArea/
TextField/Window 等）。

## 好消息

sun.awt 包已经可以用（--add-exports java.desktop/sun.awt=ALL-UNNAMED 之前为
KeyboardFocusManagerPeerProvider 加过，编译和运行都通）。

## 下一轮

1. 用脚本从 ComponentFactory.java 提取方法签名，生成 OhosToolkit 的实现（每个方法
   返回 Peers.make(XxxPeer.class)）；
2. 编译、打包、上机验证；
3. 预期这一步之后 REW 能建立组件树（虽然还不会真正绘制）。
