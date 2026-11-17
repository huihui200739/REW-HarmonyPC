# P2：UI 组件阶段 — Peer 工厂受阻于 java.awt.peer 未导出 · 2026-11-18

## 目标

REW 报 AWTError: UI components are unsupported by ohos.awt.OhosToolkit，
需要实现 Toolkit.createFrame/createButton/... 返回 Peer，才能让组件树建立。

## 尝试

用 java.lang.reflect.Proxy 动态生成 Peer（所有方法返回默认值），
覆盖 Frame/Window/Dialog/Button/Label/TextField/TextArea/Panel/Canvas/ScrollPane/Scrollbar/
Checkbox/Choice/List/MenuBar/Menu/PopupMenu/MenuItem/CheckboxMenuItem/FileDialog 共 20 个工厂方法。

## 结果：编译失败

    OhosToolkit.java:30: 错误: 方法不会覆盖或实现超类型的方法
    找不到符号: java.awt.peer.FramePeer

即 java.desktop 模块没有导出 java.awt.peer 包，javac 的 --add-exports
（java.desktop/java.awt.peer=ALL-UNNAMED）在本环境下未生效：
- 带 --release 17 时会报“不允许在使用 --release 时从系统模块导出程序包”；
- 去掉 --release 改成 -source/-target 后仍未生效。

## 下一轮方案

1. 编译时把 JDK 的 java.desktop.jmod 加入 --module-path 并 --add-modules java.desktop，
   再用 --add-exports 指向该模块；
2. 或改用 JDK 源码里的 headless Toolkit 作为基类（sun.awt.HeadlessToolkit），
   它已经实现了大部分 createXxx，只需覆盖少数几个；
3. 或打补丁在 java.desktop 内部提供桥接类，暴露需要的方法。

## 进展定位

这不是设计问题，是**编译期模块导出策略**问题。设计（Proxy 最小 Peer）是对的，
只需要把编译配置调整到能引用 java.awt.peer。
