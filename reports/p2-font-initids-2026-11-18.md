# P2 进展：焦点 peer 通过，卡在 Font.initIDs · 2026-11-18

## 本轮完成

1. OhosToolkit 实现 sun.awt.KeyboardFocusManagerPeerProvider（编译需 --add-exports java.desktop/sun.awt
   与 java.awt.peer），配 OhosKeyboardFocusManagerPeer；
   运行时参数补 --add-exports java.desktop/java.awt.peer=ALL-UNNAMED。
2. 发现并修复一个坑：应用内 ohos-awt.jar 之前只在“不存在时”复制，导致后端代码更新不生效；
   改为每次都覆盖。
3. 在 native 里手动 dlopen libawt.so 并调用其 JNI_OnLoad（ret=65538 = JNI_VERSION_1_8），
   确认注册函数确实执行了。

## 当前卡点

    Exception java.lang.UnsatisfiedLinkError: 'void java.awt.Font.initIDs()'
    Could not initialize class java.awt.Font

即 Font 的 native 注册仍未完成。JNI_OnLoad 已调用，说明 Font.initIDs 不在 libawt 的注册表里，
很可能属于 libfontmanager（字体管理器），需要确认加载时机。

## 下一步

1. grep 源码确认 Java_java_awt_Font_initIDs 的实现库；
2. 若是 libfontmanager，手动 dlopen + 调 JNI_OnLoad，或确认 Font 静态块的加载顺序；
3. 继续逐个消除 initIDs 缺失，直到 Swing 组件能真正创建。
