# P3 突破性证据：SetOps 读到 existing=0x1（字段 ID 疑似错位）· 2026-11-18

## 诊断输出（给 libawt 的 SurfaceData_SetOps 加了 fprintf）

    [OHOS-SD] SetOps sData=0x7fc2d16588 existing=0x1 new=0x7f7cdab680

`existing` 是 pData 字段的值，读到 **0x1**——既不是 0（正常初值）也不是合法指针。

## 为什么 0x1 很可疑

SurfaceData 的字段定义：

    private long pData;
    private boolean valid;   // 构造时 valid = true，即 1

读到 1 恰好等于 `valid` 的 boolean 值 —— **强烈暗示 pDataID 实际指向了 valid 字段**，
即字段 ID 错位。

## 可能的根因

1. `Java_sun_java2d_SurfaceData_initIDs` 被执行了两次（我们手动调 JNI_OnLoad + JVM 自己加载），
   第二次拿到的 jclass 来自不同 classloader，字段 ID 指向另一份类定义；
2. JNU_GetLongFieldAsPtr 在 OHOS 上的实现与字段宽度约定不一致。

## 本轮已做的尝试

移除了 native 里手动调用 libawt/libawt_headless 的 JNI_OnLoad（保留 dlopen 供 dlsym），
让 JVM 自己加载时完成注册。但设备上仍是旧进程的日志，需要在确认重启后复测。

## 下一轮

1. 确保应用完全重启（uninstall + install，验证 pid 变化）；
2. 复测 SetOps 的 existing 值；
3. 若仍为 0x1，则在 initIDs 里打印 GetFieldID 返回的 jfieldID 与字段偏移，确认错位来源。
