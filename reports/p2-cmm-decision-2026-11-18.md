# P2：CMSManager 初始化成功，但 loadProfileNative 仍崩 · 2026-11-18

## 本轮结果

1. 在 native 里显式调用 sun.java2d.cmm.CMSManager.getModule()：成功返回 module
   （说明 LCMSServiceProvider 与 LCMS.getModule() 链已走通）；
2. 移除手动 dlopen liblcms（排除双实例导致静态变量分离的可能）；
3. 崩溃依旧：liblcms.so(Java_sun_java2d_cmm_lcms_LCMS_loadProfileNative+212) NULL 解引用。

## 判断

颜色管理的 Java 层初始化已完成，问题在 lcms 的 C 侧：loadProfileNative 依赖的字段 ID 在
这个 ABI/构建组合下没有正确建立。继续深挖需要读 liblcms 的 LCMS.c 与 LCMS 类字段一一对照，
成本不低且收益有限（只是颜色一致性问题，不影响测量功能）。

## 决定：改用方案 3（自建 identity CMM）

理由：CMSManager 支持 -Dsun.java2d.cmm=<类> 注入自定义 provider；实现一个 identity 色彩变换，
可以完全绕开 lcms 的 native，且这部分是纯 Java，可控、可测。

计划：

1. 写 ohos.awt.OhosCmmServiceProvider extends sun.java2d.cmm.CMMServiceProvider，
   返回一个 identity PCMM 实现（含 PCMM/Profile/Transform 的最小方法集）；
2. 启动参数加 -Dsun.java2d.cmm=ohos.awt.OhosCmmServiceProvider；
3. 观察 REW 是否越过颜色管理，进入窗口创建。

## 保留的备选

若 identity CMM 的接口面过大，退回“空实现 + 捕获 CMMException”的方案，
让 REW 在无颜色管理的情况下继续，作为阶段性验证。
