# P2 第一版 AWT 后端：已接入，但 JDK 仍走 X11 路径 · 2026-11-18

## 本轮产出

- platform/java/ohos/awt/OhosToolkit.java（最小 Toolkit：屏幕 1920x1080 / DPI 160 / 空事件队列）
- platform/java/ohos/awt/OhosGraphicsEnvironment.java（单屏环境）
- platform/java/ohos/awt/OhosGraphicsDevice.java（RASTER_SCREEN + DisplayMode）
- platform/java/ohos/awt/OhosGraphicsConfiguration.java（bounds/transform）
- platform/build-awt-backend.sh（javac --release 17 → ohos-awt.jar，8 KB）

接入方式：jar 打进 rawfile，应用启动时复制到 files/jars；JVM 参数加：

    -Djava.awt.headless=false
    -Dawt.toolkit=ohos.awt.OhosToolkit
    -Djava.awt.graphicsenv=ohos.awt.OhosGraphicsEnvironment
    --add-opens=java.desktop/java.awt=ALL-UNNAMED

## 实测结果

REW main 继续前进，但 Toolkit 初始化时：

    SEVERE: Error during startup java.lang.UnsatisfiedLinkError:
      Can't load library: /data/storage/el1/bundle/libs/arm64/libawt_xawt.so

说明 JVM 仍在走 X11 后端路径（headless 构建不产出 libawt_xawt.so）。
同时 REW 报：

    SEVERE: Cannot create log file handler java.nio.file.NoSuchFileException: ?/REW/roomeq_wizard0.log.txt.lck

即 user.home 未设置，REW 的配置目录解析成了 ?/REW。

## 下一轮要解决的三件事

1. 确认 -Dawt.toolkit / -Djava.awt.graphicsenv 是否真被 JVM 采纳（在 native 里读 System.getProperty 并即时打日志，不等 probe 返回）；
2. 设置 user.home 到应用沙箱（如 files/home），修掉 ?/REW；
3. 若 JDK 内部仍有硬编码 X11 Toolkit 路径，提供 libawt_xawt.so 的兼容壳，或打补丁让 Toolkit 尊重 awt.toolkit。
