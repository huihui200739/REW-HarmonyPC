# P1-a4 崩溃定位 · 2026-11-18

## 方法：把 JVM 的 stderr 抓出来

应用里 stdout/stderr 不进 hilog，先在 native 侧 freopen 到沙箱文件：

```c
freopen((workDir + "/jvm-stderr.log").c_str(), "w", stderr);
freopen((workDir + "/jvm-stdout.log").c_str(), "w", stdout);
```

设备侧读取：`/data/app/el2/100/base/com.example.rewharmony/haps/entry/files/jvm-stderr.log`

## 错误一：java.home 被补丁覆盖（已修）

HotSpot 的 OHOS 补丁（0003）会读环境变量 `ECLIPSE_EMBEDDED_JAVA_HOME` 覆盖 java.home；
未设置时它从 libjvm.so 的路径反推，得到 `/data/storage/el1/bundle`，于是：

```
[OHOS HotSpot] java_home=/data/storage/el1/bundle modules=.../lib/modules stat=-1 errno=2
Error occurred during initialization of VM
Failed setting boot class path.
```

修复：创建 VM 前 `setenv("ECLIPSE_EMBEDDED_JAVA_HOME", javaHome, 1)`。

修复后：

```
[OHOS HotSpot] using embedded_java_home=/data/storage/el2/base/haps/entry/files/jre
[OHOS HotSpot] java_home=.../files/jre modules=.../files/jre/lib/modules stat=0 errno=0
```

## 错误二：libjava.so 缺 fdlibm 符号（未修完）

```
Error occurred during initialization of VM
Unable to load native library: Error relocating .../libjava.so: jcos: symbol not found
```

诊断：

```
$ llvm-nm -D -u libjava.so | grep -iE "cos|jn"
                 U jacos
                 U jcos
                 U jcosh
$ llvm-nm libjava.so | grep -c ieee754
0
```

即 **libfdlibm 没有链接进 libjava.so**。上游规则是
`LIBS := $(BUILD_LIBFDLIBM_TARGET)`（`make/modules/java.base/lib/CoreLibraries.gmk`），
该变量在本环境可能为空。

已做改动（待验证，将形成补丁 0004）：

```diff
-    LIBS := $(BUILD_LIBFDLIBM_TARGET), \
+    LIBS := $(SUPPORT_OUTPUTDIR)/native/$(MODULE)/libfdlibm.a, \
```

`libfdlibm.a` 本身已产出（`build/ohos-aarch64/support/native/java.base/libfdlibm.a`，2.0 MB 目录）。

## 清理副作用：暴露 java.desktop 的 X11 依赖

为强制重链删除 `support/native/java.base/objs` 后，`make java.base-jmod` 把
`java.desktop-libs` 也调度进来，随即失败：

```
src/java.desktop/unix/native/common/awt/utility/rect.h:32:10:
fatal error: 'X11/Xlib.h' file not found
make[2]: *** [java.desktop-libs] Error 2
```

虽然 `ENABLE_HEADLESS_ONLY := true` 已生效，但该目标仍要编译 `libawt` 的
X11 相关源。这说明 **P1-a（拿到完整 runtime）与 P1-b（java.desktop）不是串行关系，
而是耦合的**：要 build 出完整 libjava/base 依赖链，必须先给 java.desktop 提供
X11 兼容层（stub 头或裁剪 libawt）。

## 结论

| 关卡 | 状态 |
|---|---|
| 应用域可执行内存 | 通过（mprotect RX OK） |
| libjvm.so 加载 | 通过 |
| modules 定位 | 通过（ECLIPSE_EMBEDDED_JAVA_HOME） |
| libjava.so 符号解析 | **待修**：fdlibm 未链接 |
| java.desktop X11 依赖 | **待修**：阻塞完整构建链 |

## 下一步

1. 验证 `LIBS` 修改是否真正生效：干净重编 `java.base`（先解决 java.desktop 编不过的问题，否则构建链断在别处）；
2. 给 java.desktop 提供最小 X11 stub 头（`X11/Xlib.h` 等），或在 configure 里彻底剥离 AWT 的 X11 源；
3. 重跑设备探针，目标输出 `JNI_CreateJavaVM: OK`。
