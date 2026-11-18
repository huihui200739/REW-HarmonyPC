# P4：FrameCapture 未执行的真正原因——i4jruntime.jar 缺失 · 2026-11-18

## 定位

probe 日志显示：

    FindClass(REW main)=FAIL
      exception: java.lang.NoClassDefFoundError: com/install4j/api/launcher/StartupNotification$Listener

**REW 主类根本没加载成功**，所以 FrameCapture 那段（依赖 FindClass 成功）自然不执行。
前两轮误判为“控制流问题”，实际是 classpath 缺 jar。

## 根因

`i4jruntime.jar`（install4j 运行时，REW 主类的依赖）在 `rew-app/.install4j/` 子目录里，
而我重打 `rew-jars.zip` 时用的是 `zip -j *.jar`（只匹配顶层），把它漏掉了。
这与合规清理无关，是我打包脚本的缺陷。

## 已修

    cp .install4j/i4jruntime.jar ./i4jruntime.jar && zip -qj rew-jars.zip *.jar

验证：本地 zip = 35,368,887 字节 / **28 files** / 含 i4jruntime ✓

## 待处理

HAP 里的 `rew-jars.zip` 仍是旧的（27 files），说明 hvigor 的 rawfile 打包有缓存。
需要在 /tmp/rew-build 彻底清理后重建。

## 教训

`zip -j *.jar` 的通配不会递归子目录。这条与“四层构建缓存”是同一类问题：
**构建产物要验证内容，而不只是验证命令退出码**。
