# P2 突破：ohos.awt 后端已被 JDK 采用，卡在内部接口契约 · 2026-11-18

## 关键修复链

1. sun.awt.PlatformGraphicsInfo.createGE()/createToolkit() 在 Linux 上硬编码 X11GraphicsEnvironment/XToolkit，
   打补丁让它读 java.awt.graphicsenv / awt.toolkit 系统属性；
2. 属性加载用 Class.forName(nm) 时走的是 java.desktop 的 bootstrap classloader，看不到应用 classpath，
   报 ClassNotFoundException: ohos/awt/OhosGraphicsEnvironment；
   改用 ClassLoader.getSystemClassLoader() 后成功加载。

## 结果

不再出现 X11GraphicsEnvironment，也不再加载 libawt_xawt 的 initXRender。
当前错误变成：

    java.lang.ClassCastException: class ohos.awt.OhosToolkit cannot be cast
      to class sun.awt.KeyboardFocusManagerPeerProvider
        at java.awt.KeyboardFocusManager.initPeer
        at java.awt.KeyboardFocusManager.<init>
        at java.awt.DefaultKeyboardFocusManager.<init>

意味着 **JDK 已经接受我们的 Toolkit**，只是它要求 Toolkit 额外实现
内部接口 sun.awt.KeyboardFocusManagerPeerProvider（返回 KeyboardFocusManagerPeer）。

## 这说明什么

AWT 的内部契约是一串接口：Toolkit → KeyboardFocusManagerPeerProvider →
KeyboardFocusManagerPeer → 输入事件分发。每补一层，Swing 初始化就能再往前走一段。
这条路可行，但需要按 JDK 内部的接口逐个实现，工作量可控但琐碎。

## 下一步

1. 让 OhosToolkit 实现 sun.awt.KeyboardFocusManagerPeerProvider（需 --add-exports 编译），
   返回一个最小的 KeyboardFocusManagerPeer（空实现，后续接 OHOS 输入事件）；
2. 继续跑，记录下一个 ClassCastException / AbstractMethodError；
3. 顺手 mkdir home/REW，消掉 FileHandler 的 SEVERE 噪声。
