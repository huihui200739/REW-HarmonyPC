# P4 决定性发现：REW 只建了菜单栏，内容区为空 · 2026-11-18

## 深度组件树（depth<=10）

    {roomeqwizard.g 1600x1000 vis=true valid=false; kids=1
      JRootPane 1600x1000 vis=true valid=false; kids=2
        JPanel 1600x1000 vis=true valid=true; kids=0          <- contentPane，空
        JLayeredPane 1600x1000 vis=true valid=true; kids=2
          JPanel 1600x967 vis=true valid=true; kids=0         <- layered 面板，空
          roomeqwizard.XE 1600x33 vis=true valid=true; kids=7 <- 菜单栏
            JMenu 51x32 / 68x32 / 127x32 / 75x32 / 59x32 / 83x32 / 142x32}

## 结论

1. **REW 主窗口只构建出菜单栏**（7 个 JMenu，宽度合情合理，像 File/Edit/Tools/...）；
2. **contentPane 与 layered pane 都是空的（kids=0）**；
3. Frame 与 JRootPane 的 `valid=false`。

## 这意味着什么

不是绘制问题，是 **UI 构建中断**：REW 的 `JB.<init>` 在创建菜单之后、创建主内容面板之前就停住了。

最可能的原因：

1. `JB.<init>` 里抛了异常，被 FrameCapture 的 `catch (Throwable ignored)` 吞掉了；
2. 或者卡在某个阻塞调用（音频设备枚举、JNA 加载等），一直没返回。

## 下一轮（关键）

1. FrameCapture 不要吞异常——把 main 线程的 Throwable 记录到返回值里；
2. 同时记录 main 线程的堆栈快照（Thread.getAllStackTraces），看它卡在哪一行；
3. 这两条信息能直接给出答案：抛异常 or 卡住。
