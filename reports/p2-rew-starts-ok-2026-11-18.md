# 里程碑：REW 启动成功（无异常、进程存活），窗口尚不可见 · 2026-11-18

## 状态

    pidof com.example.rewharmony -> 15221（持续存活）
    jvm-stderr.log -> 只有 3 行 HotSpot 头部，无任何异常

补上 JAXB（jaxb-api 2.3.1 + jaxb-runtime 2.3.9）之后，REW 的 main 不再抛异常，
进程稳定运行。这意味着 **REW 的 Java 侧启动流程走完了**：

- L&F 装配 ✓
- AWT 事件线程 ✓
- 组件树构建 ✓
- Window.pack() 布局 ✓
- 启动画面关闭 ✓
- JAXB 业务依赖 ✓

## 为什么还看不到窗口

我们的 AWT 后端目前是**最小 Peer**（Proxy，所有方法返回默认值）：

- FramePeer/WindowPeer 没有真正创建 OHOS 窗口；
- Graphics2D 绘制没有落到任何 Surface；
- 所以 Swing 认为窗口存在，但屏幕上什么都不显示。

截图确认：屏幕上只有我们的探针 HAP 窗口，没有 REW 的 Swing 界面。

## 下一轮（P3 真实窗口）

1. FramePeer/WindowPeer 需要真正创建 OHOS 窗口（XComponent 或 Window）；
2. 把 Toolkit 的 createFrame 接到一个真实的 Peer 实现（继承 Component/Window 的 native 对接）；
3. 绘制路径：Java2D → 离屏 BufferedImage → 上传到 XComponent 表面；
4. 事件路径：XComponent 输入事件 → AWT EventQueue。

## 结论

**运行时的适配已经完成**（P1/P2 主体），剩下的是图形后端（P3）——
这是从“让 REW 跑起来”到“让 REW 显示出来”的切换点。
