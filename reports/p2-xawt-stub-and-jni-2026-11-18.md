# P2 进展：xawt 兼容壳生效，暴露 JNI 注册与 X11 兜底两条问题 · 2026-11-18

## 本轮确认的事实

1. 四个关键系统属性都已生效（native 直接读取验证）：

       user.home=/data/storage/el2/base/haps/entry/files/home
       awt.toolkit=ohos.awt.OhosToolkit
       java.awt.graphicsenv=ohos.awt.OhosGraphicsEnvironment
       java.awt.headless=false

2. 但运行期仍实例化了 sun.awt.X11GraphicsEnvironment（栈里 initDisplay(boolean) 报 UnsatisfiedLinkError），
   说明 GraphicsEnvironment 的选择没有采纳我们的类，或类加载失败后回退。

3. 同时出现 java.awt.Component.initIDs() 的 UnsatisfiedLinkError ——
   libawt.so 已加载，但其中的 JNI 符号没有注册（JNI_OnLoad 未执行或符号缺失）。

4. libawt_xawt.so 兼容壳（awt_xawt_stub.c，只导 JNI_OnLoad）已打包进 HAP，
   把原来的“找不到库”错误换成了更靠后的 initDisplay 错误——这一步是有效的。

## 判断

Component.initIDs 缺失是更根本的问题：即使 GraphicsEnvironment 换成我们的实现，
Swing 组件仍需要 libawt 的 native 注册表（Component/ColorModel/Font 等 initIDs）。
这条 JNI 注册链在 OHOS 上没跑通，需要单独排查（可能与我们 dlopen 方式、JNI_OnLoad 未被调用有关）。

## 下一轮

1. 在 native 里 dlopen libawt.so 后显式调用其 JNI_OnLoad，或确认 JVM 加载路径是否触发注册；
2. 打印 java.class.path 与 FindClass(ohos/awt/OhosGraphicsEnvironment) 结果，确认类可见性；
3. 修 REW 的 home 目录（mkdir -p home/REW），消掉日志 handler 的 SEVERE 噪声。
