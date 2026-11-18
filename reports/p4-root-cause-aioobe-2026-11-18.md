# P4 根因确认：ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1 · 2026-11-18

## REW 自己的错误报告（从其 JXErrorDialog 的 JXEditorPane 导出，3787 字符）

    REW V5.31.3 running N/A JRE 17.0.17-internal 64-bit on linux 5.10.210
    Locale: language en, keyboard , UTF-8 at 160 DPI Running in /

    Message:
        java.lang.reflect.InvocationTargetException
    Stack Trace:
        java.desktop/java.awt.EventQueue.invokeAndWait
        ...
    Caused by:
        java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1

## 这是决定性证据

REW 启动失败的真实原因是 **数组越界**：访问索引 1，但数组长度只有 1。

## 最可能的来源

结合我们的移植层，嫌疑最大的是**设备枚举**：

| 枚举对象 | 我们的实现 | REW 可能的假设 |
|---|---|---|
| 屏幕设备 | `OhosGraphicsEnvironment.getScreenDevices()` 返回 **1 个** | 期望 ≥2（多显示器场景）|
| 音频设备 | 未实现（Java Sound 缺失） | 期望至少一个输入/输出 |
| 显示器配置 | `getScreenDevices()[0]` | 直接取 `[1]` 导致越界 |

`Index 1 out of bounds for length 1` 与“只有一个屏幕/一个设备”高度吻合。

## 下一轮

1. 导出的错误报告里堆栈被换行切碎，需要**完整还原**（按行拼接后取 `at ` 行）；
2. 若能定位到 REW 的哪一行（`roomeqwizard.xx.y(...)`），就能确定是屏幕还是音频；
3. 若是屏幕：让 `getScreenDevices()` 返回 2 个（虚拟第二屏）即可绕过；
4. 若是音频：需要先实现 Java Sound 的 Mixer 枚举。

## 意义

这是 P4 十几轮里第一次拿到**REW 侧的具体异常类型与消息**，
从“不知道为什么卡住”变成“越界了，去补数据”。
