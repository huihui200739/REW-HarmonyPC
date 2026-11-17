# P2：Peer 编译 — 手动成功、脚本失败，差异待查 · 2026-11-18

## 已验证可行的手动命令

    $J/bin/javac -nowarn --module-path "$J/jmods" --add-modules java.desktop \
      --add-exports java.desktop/java.awt.peer=ALL-UNNAMED \
      --add-exports java.desktop/sun.awt=ALL-UNNAMED \
      --add-exports java.desktop/sun.java2d.cmm=ALL-UNNAMED \
      -d /tmp/peertest $(find platform/java -name '*.java')

→ 只有警告（内部 API），无错误。**说明 java.awt.peer 可以通过模块路径 + --add-exports 引用**。

## 脚本里同样参数却报 40 个错误

已排查：
- BOOT_JDK 解析正确（bash -x 确认）；
- -d 输出目录换成英文 /tmp 仍失败；
- 相对路径/绝对路径都试过。

怀疑点：脚本内 `$(find ...)` 经 shell 分词后传给 javac 的路径含中文转义（bash -x 显示 `$'/Users/.../鸿\x...'`），
而手动执行时是同一个 shell 环境——差异可能在于脚本的 `set -euo pipefail` 或 IFS。

## 下一轮

1. 把编译命令原样写入 Makefile 或用数组传参（避免单词拆分），
2. 或直接把源码复制到 /tmp 编译再拷回 jar；
3. 编译通过后重打包 HAP，看 REW 是否能建立组件树。

## 当前设备行为

仍是 java.awt.AWTError: UI components are unsupported by ohos.awt.OhosToolkit
（因为 jar 还是旧版本，没有 Peer 工厂）。
