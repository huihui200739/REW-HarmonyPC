# P2 进展：颜色管理（LCMS）成为新瓶颈 · 2026-11-18

## 本轮结果

1. 先尝试用空实现覆盖 LCMS 的 native 方法：进程不再崩溃，main 抛出可控异常
   java.awt.color.CMMException: Invalid profile: null —— 说明“不崩”这条路能走通，但功能不可用。
2. 改为手动 dlopen liblcms.so 并调用其 JNI_OnLoad（ret=65542，正常），
   但随后仍在同一位置崩溃：

       #01 liblcms.so(Java_sun_java2d_cmm_lcms_LCMS_loadProfileNative+212)

   即 initLCMS 没有把字段 ID 注册进 lcms 的静态变量，loadProfileNative 拿到 NULL 字段访问就崩了。

## 根因分析

JDK 的 LCMS 初始化链：

    System.loadLibrary("awt"); System.loadLibrary("lcms"); initLCMS(Class,Class,Class); new LCMS();

System.loadLibrary 走 java.library.path（当前是 /usr/java/packages/lib:...），不含应用 libs 目录；
而 liblcms.so 是靠 dlopen 的绝对查找路径找到的，两者不是同一套机制。
所以要么把应用 libs 加进 java.library.path，要么在 initLCMS 之前用 RegisterNatives 绑定真实实现。

## 下一轮（两条路）

1. 给 JVM 加 -Djava.library.path=<bundle/libs/arm64>，让 System.loadLibrary 直接命中应用 libs，
   这样 liblcms/liblcms 的 JNI_OnLoad 会由 JVM 自己调用，initLCMS 走真实实现；
2. 若仍不通，就在 native 里手动调用 lcms 的 initLCMS 等价逻辑（注册字段 ID）。

路线 1 更干净，也能顺带解决 libawt/libfontmanager 的 loadLibrary 问题。
