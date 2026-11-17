# P1-a4 设备侧门禁记录 · 2026-11-18

## 已完成

| 步骤 | 结果 |
|---|---|
| `java.base-jmod` + `jlink --add-modules java.base` | 通过，runtime 41 MB，10 个 .so 全 AArch64、无 GLIBC 引用 |
| ArkTS 工程 + N-API 模块（`librewjvm.so`） | HAP 构建成功（30 MB，未签名 debug） |
| `hdc install` 到模拟器 | 成功（模拟器接受未签名 debug HAP） |
| 应用启动、runtime 解压到沙箱 | 成功（`/data/storage/el2/base/haps/entry/files/jre`） |

## 关键结论一：可执行内存策略按进程域区分

| 执行域 | mmap RW | mprotect RX | mmap RWX |
|---|---|---|---|
| `hdc shell`（shell 域） | OK | **FAIL** | **FAIL** |
| HAP 应用进程（normal_hap 域） | OK | **OK**（实测执行机器码返回 42） | 未测 |

这解释了为什么 hdc shell 里直接跑 JVM 会报
`Failed to mark memory page as executable - check if grsecurity/PaX is enabled`，
而应用内是有希望的：**必须在 HAP 进程内跑 JVM**，不能用 shell 直跑做验证。

## 关键结论二：应用内 JNI_CreateJavaVM 前崩溃（未定位）

应用启动后日志：

```
I A00000/REWJVM: java.home=/data/storage/el2/base/haps/entry/files/jre
I A00000/REWJVM: --- exec-mem probe ---
I A00000/REWJVM: mmap RW: OK
I A00000/REWJVM: mprotect RX: OK (call=42)
I A00000/REWJVM: MEMPROBE mmap RW: OK | mprotect RX: OK (call=42) |
（之后进程消失，无 cppcrash 记录、无 native 阶段日志）
```

已加入 native 阶段日志（`OH_LOG_INFO`，tag 默认 `JVM`），但未输出到
`hilog -T REWJVM` 过滤结果里，说明崩溃发生在 `RunJvm()` 入口之后极早期。

下一轮定位手段：
1. 给 native 日志显式 `LOG_TAG REWJVMN`，全量 `hilog -x` 不过滤；
2. 在 `Probe()` 入口、`dlopen` 前后各打一条；
3. 用 `cppcrash` 目录 + `hdc shell "hdc file recv /data/log/faultlog"` 抓栈；
4. 若 `dlopen` 即崩，考虑 `RTLD_LAZY` 与 `libc++_shared` 版本匹配（JDK 用 NDK 15 编译，应用进程用的 libc++ 版本需一致）。

## 产物

| 文件 | 说明 |
|---|---|
| `jvm/build-runtime.sh` | jlink 最小 runtime + 全 .so 校验 |
| `jvm/probe/java_version_probe.c` | shell 域探针（已证明 execmem 被禁） |
| `jvm/probe/exec_mem_probe.c` | 可执行内存策略探针 |
| `app/` | ArkTS 工程 + N-API `rewjvm` 模块 + runtime 打包 |
| `app/entry/libs/arm64-v8a/` | libjvm 等 9 个 .so + libc++_shared.so |
| `app/entry/src/main/resources/rawfile/jre-runtime.zip` | 6.7 MB（runtime 去掉 .so） |
