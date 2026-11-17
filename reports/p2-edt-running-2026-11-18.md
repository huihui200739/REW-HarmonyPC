# P2 里程碑：AWT 事件线程已运行，REW 在 EDT 里加载图像 · 2026-11-18

## 决定性证据

    at ohos.awt.OhosToolkit.createImage(OhosToolkit.java:44)
    at ohos.awt.OhosToolkit.getImage(OhosToolkit.java:38)
    at roomeqwizard.JB.<init>(y:1868)
    at roomeqwizard.RoomEQ_Wizard$7.run(y:4840)
    at java.awt.event.InvocationEvent.dispatch
    at java.awt.EventQueue.dispatchEventImpl
    at java.awt.EventDispatchThread.run

意味着：

1. **java.awt.EventQueue 与 EventDispatchThread 已正常创建并运行**（崩溃线程名就是 AWT-EventQueue-）；
2. REW 的 main 已经把初始化任务投递到 EDT 执行；
3. REW 正在通过我们的 OhosToolkit.getImage/createImage 加载图像（图标/资源）。

这是第一次出现“REW 的业务代码在 AWT 线程里跑”。

## 顺带确认：自定义颜色管理已被采用

    [OHOS-CMM] getProfileData called, arg=ohos.cmm.OhosProfile
    [OHOS-CMM] returning 6876 bytes / 234080 bytes / 556 bytes

即 identity CMM 生效，且能返回真实 profile 数据。

## 当前 NPE

    NullPointerException: Cannot load from byte/boolean array because "<parameter1>" is null
      at java.awt.color.ICC_Profile.intFromBigEndian
      at java.awt.color.ICC_Profile.getColorSpaceType
      at java.awt.color.ICC_ColorSpace.<init>
      at java.awt.color.ColorSpace$BuiltInSpace.<clinit>

发生在 ImageIO 解码路径里的内置色彩空间初始化。注意这与上面的 getProfileData 成功并不矛盾——
BuiltInSpace 走的是另一条 profile 构造路径（内置 sRGB 数据），其 data 为 null。

## 下一轮

1. 在 OhosCmm.getProfileData 里对“未知类型”返回一个**最小合法 ICC 头**（128 字节，
   头部声明 RGB/XYZ 且 size 正确），而不是空数组，避免下游 intFromBigEndian 读 null/越界；
2. 或在 OhosProfile 里保证 data 永远非空；
3. 继续跑，看 REW 是否能画出第一帧。
