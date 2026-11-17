# 构建记录 · P1-a1/a2 · 2026-11-18

## 环境

| 项 | 值 |
|---|---|
| 主机 | macOS arm64（10 核 / 16 GB） |
| OHOS NDK | DevEco SDK `native` 6.1.1.125，API 24 |
| 编译器 | OHOS clang 15.0.4（`--target=aarch64-linux-ohos24.0.0`） |
| 目标 | `aarch64-unknown-linux-musl`（OpenJDK 构建系统标识） |
| Boot/Build JDK | Temurin 17.0.21 |
| JDK 源码 | BiSheng JDK 17 @ `e366af4e348dbbc9b987ed23982565de398dd0e3`（913 MB shallow 检出） |
| autoconf | 2.73（自 Homebrew bottle 解包，shebang/前缀占位符就地修正） |
| m4 | GNU m4 1.4.21（同上） |

## 工具链自检

```
$ clang --target=aarch64-linux-ohos24.0.0 --sysroot=<ndk>/sysroot -o t t.c
→ ELF 64-bit LSB pie executable, ARM aarch64
```

## 补丁应用

```
应用 0001-separate-macos-build-sysroot.patch
应用 0002-add-minimal-ohos-configure-mode.patch
应用 0003-adapt-hotspot-to-ohos-libc.patch
完成：3 个补丁新应用；源码 HEAD=e366af4e348d
```

## configure 结果（成功）

```
Configuration summary:
* Name:           ohos-aarch64
* Debug level:    release
* HS debug level: product
* JVM variants:   server
* OpenJDK target: OS: linux, CPU architecture: aarch64, address length: 64
* Version string: 17.0.17-internal+0-adhoc.huihui.bishengjdk-17 (17.0.17-internal)
* Toolchain:      clang (clang/LLVM) 15.0.4  [我们的 wrapper: build/jdk-toolchain/api-24/bin/ohos-clang]
* Cores to use:   10   Memory limit: 16384 MB
```

产物目录：`build/jdk-source/bishengjdk-17/build/ohos-aarch64`（实际检出位于 `~/Downloads/jdk-src/bishengjdk-17`，由 `JDK_SOURCE_DIR` 指定）。

## 踩坑记录

| 问题 | 原因 | 处理 |
|---|---|---|
| `Cannot find autoconf` | 主机无 autoconf（无 Homebrew） | 从 Homebrew bottle 解包 2.73 + m4 1.4.21，替换 `@@HOMEBREW_PERL@@`/`@@HOMEBREW_CELLAR@@`/`@@HOMEBREW_PREFIX@@` 占位符，建 `opt/m4` 软链 |
| `OHOS_API_LEVEL，: unbound variable` | bash 把中文标点并入变量名 | 全部改 `${VAR}` 显式边界 |
| `缺 configure` 误报 | 判断用了 `-x`，源码里的 configure 无执行位 | 改 `-f` |
| 安装器只能在 Java 8 跑 | install4j `maxJavaVersion=1.8` | Rosetta + Temurin 8u512 静默安装抽包（见 REW-JAR-RUNTIME-PROBE.md） |

## 下一步

- `jvm/build-jvm.sh` 编译 `hotspot-server-libs`，出口标准：`libjvm.so` 为 AArch64 ELF、导出 `JNI_CreateJavaVM`、无 `GLIBC_*` 符号引用。
