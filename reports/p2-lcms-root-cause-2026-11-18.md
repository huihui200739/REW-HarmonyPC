# P2 状态：颜色管理仍崩，根因收窄到 initLCMS 未生效 · 2026-11-18

## 已排除的原因

- java.library.path 已加上 /data/storage/el1/bundle/libs/arm64，System.loadLibrary 能命中应用 libs；
- liblcms 的 JNI_OnLoad 已由我们手动调用（ret=65542 正常）；
- 崩溃栈稳定在同一位置：liblcms.so(Java_sun_java2d_cmm_lcms_LCMS_loadProfileNative+212)，NULL 解引用。

## 收窄后的判断

liblcms 的 loadProfileNative 依赖 initLCMS 缓存的字段 ID。崩溃说明这些静态变量仍是 NULL，
即 Java 侧的 sun.java2d.cmm.lcms.LCMS.initLCMS(...) 没有成功执行（或执行了但 GetFieldID 失败）。

LCMS.getModule() 的顺序是：

    System.loadLibrary("awt"); System.loadLibrary("lcms");
    initLCMS(LCMSTransform.class, LCMSImageLayout.class, ICC_Profile.class);
    theLcms = new LCMS();

只要中间一步抛出（例如 loadLibrary 因某种原因失败），后面的 initLCMS 就不会执行，
而 liblcms 的 native 方法却已注册，于是调用时撞上 NULL 字段。

## 下一轮的三条路（按成本排序）

1. 在 native 里于 main 之前显式触发 sun.java2d.cmm.CMSManager.getModule()，
   让整个颜色管理链在受控时机初始化，异常可捕获、栈更清晰；
2. 若 initLCMS 确实失败，用 RegisterNatives 把自己的 initLCMS 换成真实版本
   （需要按 liblcms 源码里的字段名逐个 GetFieldID）；
3. 彻底绕开 lcms：实现 sun.java2d.cmm.CMMServiceProvider 的 OHOS 版（identity 色彩变换），
   通过 -Dsun.java2d.cmm=ohos.awt.OhosCmmServiceProvider 注入。工作量最大，但一旦做成，
   颜色管理不再依赖任何 native 实现。

## 进度

除颜色管理外，Swing 的类初始化、L&F 装配、焦点、字体、Toolkit/GraphicsEnvironment 注入均已打通。
颜色管理是 Swing 启动路径上最后一个 native 依赖。
