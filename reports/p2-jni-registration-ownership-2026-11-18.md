# P2 重要修正：空实现注册 initIDs 已经撞墙，需改为真实注册 · 2026-11-18

## 新证据

打开 -Dsun.awt.debugFonts 后，stdout 里出现大量：

    [warning][jni,resolve] Re-registering of platform native method:
      java.awt.Font.initIDs()V from code in a different classloader
      sun.font.SunFontManager.initIDs()V ...

说明：JDK 自己的库（libawt/libawt_headless）确实注册了这些方法，
而我们用 RegisterNatives 覆盖成了空实现，两者来自不同 classloader，互相打架。

随后错误变为：

    NullPointerException: Cannot load from byte/boolean array because "<parameter1>" is null

即空 initIDs 让某些字段保持 null，后续真实 native 代码取用时炸掉。

## 结论：之前的“空实现”是权宜之计，现在必须换成真实注册

正确做法是让 JVM 通过 System.loadLibrary 加载 libawt/libawt_headless 并把它们
关联到应用 classloader，从而使用库里的真实 initIDs；而不是我们用 RegisterNatives 顶替。

## 下一轮方案

1. 移除 native 里那 68 个类的空 initIDs 注册；
2. 改为在 Java 侧（ohos.awt 后端启动时）显式 System.loadLibrary("awt") / ("awt_headless")，
   让 JVM 自行完成 JNI_OnLoad 与 classloader 关联；
3. 如果仍出现 “different classloader”，则在 native 里用 RegisterNatives 转发到库里的真实函数
   （dlsym 取符号），而不是空实现。

## 状态

进程本轮退出（新 NPE 触发未捕获异常链），但诊断信息很关键：
问题的本质是 JNI 注册归属，不是符号缺失。
