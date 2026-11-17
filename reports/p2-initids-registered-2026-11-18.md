# P2 里程碑：Swing 初始化越过全部 initIDs，进入颜色管理 · 2026-11-18

## 关键突破：用 RegisterNatives 显式注册 68 个类的 initIDs

问题链：JVM 只在 System.loadLibrary 关联的库里查找 native 方法，手动 dlopen 的库不参与查找；
而 Font/Component 等的 initIDs 分散在 libawt_headless，运行时加载的却是 libawt。

解法：在 create 之后，用 Class.forName(name, false, systemClassLoader) 拿到未初始化的类
（直接 FindClass 会触发静态初始化，而静态初始化正是要调用 initIDs，形成死循环），
再对每个类 RegisterNatives 一个空 initIDs。

结果：

    RegisterNatives initIDs ok=64 fail=4

fail 的 4 个是扫描误判（如 sun/java2d/pipe/SpanClipRenderer 没有 initIDs）。
Font.initIDs、Component.initIDs 等全部通过。

## 新的崩溃点：LCMS（颜色管理）

    Reason: Signal:SIGSEGV(SEGV_MAPERR)@000000000000000000 (NULL deref)
    #01 liblcms.so(Java_sun_java2d_cmm_lcms_LCMS_loadProfileNative+212)
    #12 librewjvm.so(JNIEnv_::CallStaticVoidMethod)

即 REW 已经走到 **加载 ICC profile**——这不只是类初始化，是真的在建 Color/Graphics2D 相关的对象。
崩溃发生在 lcms 的 native 方法里（它的 JNI 字段 ID 由 initLCMS 注册，我们没实现）。

## 下一轮

1. 处理 LCMS：要么补齐 initLCMS 的字段注册，要么用 -Dsun.java2d.cmm=sun.java2d.cmm.lcms 之外的方式
   （JDK 有 java.awt.color 的软件实现路径可尝试）；
2. 继续跑，记录下一个崩溃点；
3. 每次崩溃都落 cppcrash，已形成稳定的诊断闭环。

## 进度判断

Swing 初始化的三大块（L&F 装配、焦点系统、字体系统）已经全部走通，现在进入图形/颜色底层。
这是从“类加载”到“真正创建图形对象”的跨越。
