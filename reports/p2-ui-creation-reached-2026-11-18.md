# P2 里程碑：颜色空间 NPE 修复，REW 进入 UI 组件创建阶段 · 2026-11-18

## 修复

根因：java.awt.color.ICC_Profile.getColorSpaceType() 走的是

    getData(p, icSigHead) -> CMSManager.getModule().getTagData(p, 'head')

而我们的 OhosCmm.getTagData 直接返回 null，导致 intFromBigEndian(null) NPE。

修复：getTagData 对 icSigHead(0x68656164) 返回 128 字节 ICC 头（取 profile 前 128 字节，
不足则用内置最小头）。

## 结果：NPE 消失，进入 UI 创建

    Caused by: java.awt.AWTError: UI components are unsupported by: ohos.awt.OhosToolkit@60deb020

这是 Toolkit.createXxx() 基类默认实现抛出的异常——意味着 REW 已经在**请求创建按钮/标签/窗口**等实际 UI 组件。

## 意义

从“初始化/类加载”正式进入“创建界面”。这是整个 P2 的分水岭：
前面所有工作都是为了走到这一步。

## 下一轮

1. 给 OhosToolkit 实现 createButton/createLabel/createFrame/createWindow 等（返回 Peer）；
2. Peer 可以先是最小实现（空方法 + 记录），让组件树能建起来；
3. 之后再把 Peer 接到 OHOS 的 XComponent/Surface 上做真实绘制。

## 状态

REW 的 main 在 EDT 上执行、图像加载与颜色空间都已通过，现在停在“要 UI 组件”这一步。
