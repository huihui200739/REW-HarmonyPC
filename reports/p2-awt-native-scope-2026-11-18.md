# 关键发现：AWT 组件的 native 实现属于 libawt_xawt，headless 构建里没有 · 2026-11-18

## 证据

1. libawt.so 导出 89 个 Java_* 符号，但没有 Java_java_awt_Component_initIDs；
   libawt_headless.so 导出 51 个，同样没有 Component 相关。
2. 源码定位：该函数在 src/java.desktop/unix/native/libawt_xawt/xawt/XToolkit.c:157。
3. 也就是说：Swing/AWT 组件的原生字段注册（initIDs）只在 xawt 里，
   而 xawt 需要 X11，OHOS 上不可能编出来。

## 这解释了当前现象

     UnsatisfiedLinkError: 'void java.awt.Component.initIDs()'
     UnsatisfiedLinkError: 'void sun.awt.X11GraphicsEnvironment.initDisplay(boolean)'

我们给的 libawt_xawt.so 空壳只解决“库找不到”，解决不了“符号不存在”。

## 路线判断

要让 REW 的 Swing 界面真正跑起来，必须实现一个**不依赖 X11 的 AWT native 层**，
至少包含：

| 模块 | 内容 |
|---|---|
| 字段注册 | Component/MenuComponent/Button/... 的 initIDs（XToolkit.c 里那批） |
| 图形 | Graphics/Graphics2D 的 native（可直接对接 Java2D 的 software 管线） |
| 事件 | Toolkit 的事件队列与输入分发（先空实现，只保证不崩） |
| 字体 | libfontmanager 已编出，需要接字体发现路径 |

这是一项以月计的工作，不是补几个符号能解决的。

## 下一轮的最小增量

1. 把 XToolkit.c 里的 initIDs 函数抽出来，编译进 libawt_xawt.so（替换空壳），
   这些函数只做字段 ID 注册，不需要 X11 连接；
2. 观察 REW 能推进到哪一步（下一个 UnsatisfiedLinkError 是什么）；
3. 据此估算完整 AWT native 层的工作量，再决定是否继续这条路线。
