# JVM 构建门禁 · P1-a3 · 2026-11-18

## 结果

`hotspot-server-libs` 在 `ohos-aarch64` 配置下编译通过。

```
Finished building target 'hotspot-server-libs' in configuration 'ohos-aarch64'
```

## 产物

| 项 | 值 |
|---|---|
| 路径 | `<jdk-src>/build/ohos-aarch64/support/modules_libs/java.base/server/libjvm.so` |
| 大小 | 20 MB（release，未 strip，带 debug_info） |
| 类型 | `ELF 64-bit LSB shared object, ARM aarch64, version 1 (SYSV), dynamically linked` |
| 导出符号 | `JNI_CreateJavaVM@@SUNWprivate_1.1` |
| GLIBC 符号引用 | **0** |
| 动态依赖 | `libc++_shared.so`、`libc.so` |

## 复现命令

```bash
export JDK_SOURCE_DIR=<bishengjdk-17 检出>
export BOOT_JDK=<Temurin 17>
export PATH=<autoconf 2.73>/bin:<m4 1.4.21>/bin:$PATH
jvm/apply-jdk-patches.sh        # 3 个补丁
jvm/configure-jdk.sh            # --enable-ohos-port / --enable-headless-only
jvm/build-jvm.sh --jobs 6       # make CONF=ohos-aarch64 JOBS=6 hotspot-server-libs
jvm/build-jvm.sh --verify-artifact
```

## 校验输出

```
$ jvm/build-jvm.sh --verify-artifact
  ELF 64-bit LSB shared object, ARM aarch64, version 1 (SYSV), dynamically linked, with debug_info, not stripped
  校验通过：AArch64 / JNI_CreateJavaVM / 无 GLIBC 符号

$ llvm-nm -D --defined-only libjvm.so | grep JNI_CreateJavaVM
000000000088dbd0 T JNI_CreateJavaVM@@SUNWprivate_1.1

$ llvm-readelf -d libjvm.so | grep NEEDED
  Shared library: [libc++_shared.so]
  Shared library: [libc.so]
```

## 说明与边界

1. 这证明 **HotSpot 能在 OHOS NDK 工具链下编译链接**，不等证明能在设备上加载运行——
   加载还受动态链接器路径、seccomp、W^X（JIT 可执行内存）、应用沙箱限制，属于 P1-a4。
2. 用的是 `--enable-headless-only`：`java.desktop`（AWT/Swing）尚未参与构建，那是 P1-b。
3. OpenJDK 的 `make` 不接受 `-jN`，必须 `JOBS=N`（已在脚本里固定）。
4. `file` 输出是小写 `aarch64`，校验脚本已改为大小写都接受。

## 下一步

| 步骤 | 内容 |
|---|---|
| P1-a4 | 构建最小 runtime（`jlink --add-modules java.base`）+ ArkTS HAP，设备上 `java -version` |
| P1-b1 | 去掉 headless，编译 `java.desktop`（libawt/libfontmanager） |
