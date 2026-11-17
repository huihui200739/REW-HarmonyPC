# P2：REW 越过 SplashScreen 与 pack，卡在 Java 8 的 JAXB 依赖 · 2026-11-18

## 本轮修复

1. 给 Peers 的 Proxy 补默认返回值（Insets/Dimension/Rectangle/Point/Font/Color/Cursor），
   解决 `Container.insets()` 拿到 null 的 NPE；
2. 注册 sun.awt.SunToolkit.closeSplashScreen 的空实现，越过 SplashScreen 关闭调用。

## 进展

- 进程持续存活；
- Window.pack() 通过；
- 新错误：

      ClassNotFoundException: javax.xml.bind.JAXBException

## 原因

REW 是 Java 8 应用，用了 JAXB（javax.xml.bind）。JDK 11 起 JAXB 从 JDK 移除，
移植到 JDK 17 后必须显式提供 jaxb-api + 实现（如 jaxb-runtime）。

## 下一轮

1. 下载 jaxb-api 与 jaxb-runtime（Maven Central）；
2. 打进 HAP 的 jars 目录（与 REW 的 26 个 jar 同处 classpath）；
3. 若还有其他 Java 8 移除的模块（JAX-WS、CORBA、javax.annotation），一并补齐。

## 进度判断

从 pack() 到 JAXB，说明 REW 的启动流程已经进入业务初始化阶段，
不再是 AWT 基础设施问题——这是好事：后续问题都是“缺哪个 jar/API”，而不是“后端没实现”。
