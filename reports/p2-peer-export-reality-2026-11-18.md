# P2：Peer 编译 — 上一轮的“手动成功”是误判 · 2026-11-18

## 纠错

上一轮我用手动命令验证“Peer 可以编译”，命令末尾带了 `| head -12`，
只看到开头 12 行警告，实际的 40 个错误被截掉了。**手动并没有成功**，
脚本和手动是同一结果：`找不到符号 java.awt.peer.FramePeer`。

## 尝试过的解法

1. `--module-path $BOOT_JDK/jmods --add-modules java.desktop --add-exports java.desktop/java.awt.peer=ALL-UNNAMED`
   —— 仍报找不到符号；
2. 在 JDK 源码 module-info.java 里加 `exports java.awt.peer;` 并重编 java.desktop
   —— 编译失败：`error: warnings found and -Werror specified`（导出内部包触发警告，被 -Werror 拦下）。
   已回退该改动，避免破坏构建。

## 现状

java.awt.peer 在 JDK 17 里是**未导出**包，常规 --add-exports 组合没有绕过去；
直接改模块描述符又被 -Werror 挡住。

## 下一轮方案（按可行性排序）

1. 给 make 传 `WARNINGS_AS_ERRORS=false` 或 `DISABLE_WARNINGS_AS_ERRORS=1`，配合 module-info 导出，
   只影响我们自己构建的 java.desktop，风险可控；
2. 绕过类型引用：不在自己的 Toolkit 里覆写 createXxx，而是**打补丁改 JDK 的 Toolkit**，
   让 createXxx 返回一个通用的 dummy peer（在 java.desktop 内部实现，天然可访问 java.awt.peer）；
3. 用 java.lang.reflect.Proxy 在 JDK 内部创建（同样需要补丁）。

## 判断

方案 2 其实最干净：把“最小 Peer 工厂”下沉到 java.desktop 内部（作为补丁 0008），
我们的 OhosToolkit 只需要设置一个开关或继承该实现，彻底绕开导出限制。
