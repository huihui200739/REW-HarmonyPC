# P2 里程碑：REW 已进入 Window.pack() 布局阶段 · 2026-11-18

## 决定性证据

    java.awt.BorderLayout.preferredLayoutSize
    java.awt.Container.preferredSize
    java.awt.Container.getPreferredSize
    java.awt.Window.pack
    roomeqwizard.JB.<init>(y:816)
    roomeqwizard.RoomEQ_Wizard$7.run(y:4840)
    java.awt.event.InvocationEvent.dispatch
    java.awt.EventDispatchThread.run

Window.pack() 是 Swing 建窗的核心步骤：它会遍历组件树、计算首选尺寸、触发布局。
走到这一步意味着：

1. 组件树已建立（ComponentFactory 生效）；
2. 字体度量可用（preferredSize 需要 FontMetrics）；
3. 布局管理器正常运行。

## 本轮顺手修的

- 预加载 libfontmanager（此前 sun.font.FreetypeFontScaler/SunFontManager 的 initIDs 落在 stub，
  导致 sunFontIDs.ttReadBlockMID 为 0，字体流读取崩溃）；
- 注册统计从 real=35 提升到 **real=37 / stub=31**。

## 当前崩溃

线程从 AWT-EventQueue 变为 **VM Thread**，栈为 musl raise/abort + libjvm：

    #00 ld-musl (raise)
    #01 ld-musl (abort)
    #02 libappspawn_helper (exit)
    #03 libjvm.so

典型的 JVM 致命错误（FATAL ERROR）路径，需要看 stderr 里的 FATAL 段（可能 OOM 或断言失败）。

## 下一轮

1. 抓 stderr 中 FATAL ERROR 的完整段落（通常在栈之前）；
2. 若是 OOM，调小堆（-Xmx）并检查 jvm 内存参数；
3. 若是断言，定位到 HotSpot 的 assert 位置。
