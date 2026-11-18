# P3：jfieldID 诊断未输出，准备改用 side table · 2026-11-18

## 情况

源码里已加入 `ids pDataID=... validID=...` 的打印（grep 命中 1 处），
SurfaceData.o 时间戳也更新了，但设备日志里只出现旧的 SetOps 行，没有 ids 行。

可能原因：
1. make 复用了旧的 .o（增量判断看的是 .d 依赖，不是内容）；
2. 库被 jlink 的缓存覆盖（runtime 里是旧 libawt）；
3. fprintf 被编译器优化掉（不太可能，有 fflush）。

## 决定：跳过该诊断，直接采用兜底方案

不再依赖 `pData` 字段（它明显错位），改为 **native 侧 side table**：

    static struct { jobject sData; SurfaceDataOps* ops; } gTable[N];
    SetOps: 查表判重 -> 追加；GetOps: 查表取。

优点：完全绕开字段机制，字段 ID 错位不再影响运行。
代价：需要改 SurfaceData.c 里所有 pData 的读写点（约 6 处）。

## 下一步

1. 列出 SurfaceData.c 中所有 pDataID 使用点；
2. 用 side table 替换（保留原字段写法作为回退）；
3. 重编 libawt，验证 SetOps 不再报错、SunGraphics2D 能画图。
