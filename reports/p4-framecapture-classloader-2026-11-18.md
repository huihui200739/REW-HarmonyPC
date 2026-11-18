# P4：FrameCapture 已执行，卡在 classloader · 2026-11-18

## 本轮突破

修好 i4jruntime 与 C++ 字符串转义后，probe 日志出现：

    FindClass(ohos/awt/OhosToolkit)=OK
    FrameCapture class=found
    FrameCapture method=found
    FrameCapture result=ERROR: java.lang.ClassNotFoundException: roomeqwizard/RoomEQ_Wizard

即 **FrameCapture 被正确调用并执行了**（前两轮的“未执行”已彻底解决）。

## 当前唯一障碍

`Class.forName("roomeqwizard.RoomEQ_Wizard")` 用的是 FrameCapture 自身的 classloader（bootstrap，
因为 FrameCapture 在 java.desktop 模块内），它看不到应用 jar。

## 修法（下一轮）

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) cl = ClassLoader.getSystemClassLoader();
    Class<?> cls = Class.forName(mainClass, true, cl);

## 本轮其它修复

1. `i4jruntime.jar` 补回 zip（`zip -j *.jar` 不递归子目录）；
2. C++ 字符串里的 `\n` 曾被写成真实换行导致编译失败，已修；
3. HAP 内 jar 包验证：35,368,887 字节 / 28 files / 含 i4jruntime ✓。
