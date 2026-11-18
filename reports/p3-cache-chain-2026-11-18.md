# P3：库文件始终未更新，构建缓存链路仍未打通 · 2026-11-18

## 事实

逐级检查 libawt.so 是否含旧诊断标记 `OHOS-SD`：

| 位置 | 结果 |
|---|---|
| build 产物 support/modules_libs | 0（新库，无旧标记） |
| runtime images/ohos-runtime/lib | **2（旧库）** |
| HAP 的 libs/arm64-v8a | **1（旧库）** |

即：**make 确实产出了新库，但 jlink 出来的 runtime 里还是旧的**。

## 推断

`java.desktop.jmod` 没有被真正重建（make 认为它是最新的），
jlink 从旧 jmod 解出旧 .so；随后部署又从 runtime 拷贝，一路都是旧的。

## 下一轮（彻底清理后重建）

    rm -f build/ohos-aarch64/images/jmods/java.desktop.jmod
    rm -f build/ohos-aarch64/support/modules_libs/java.desktop/libawt.so
    rm -rf build/ohos-aarch64/images/ohos-runtime
    make CONF=ohos-aarch64 java.desktop-jmod
    jvm/build-runtime.sh

之后再用 strings 逐级验证（产物 → runtime → HAP），确认每一层都是新库才上机。

## 教训

这条链有四个缓存层（.o / .so / .jmod / runtime），任何一层没刷新都会导致
“改了代码但设备行为不变”。必须每层都验证。
