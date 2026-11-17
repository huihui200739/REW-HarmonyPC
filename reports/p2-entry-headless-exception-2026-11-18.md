# 里程碑：REW main 越过 L&F，进入图形环境请求 · 2026-11-18

## InputContext 补丁生效

0005 补丁（sun.awt.im.InputContext.getLocale() 在无输入法时返回 Locale.getDefault()）之后，
REW 不再因 locale 为 null 崩溃，继续往下执行：

    java.awt.HeadlessException:
    No X11 DISPLAY variable was set, but this program performed an operation which requires it.
        at sun.awt.HeadlessToolkit.getScreenResolution
        at roomeqwizard.ID.H(y:1289)
        at roomeqwizard.RoomEQ_Wizard.x(y:3989)
        at roomeqwizard.RoomEQ_Wizard.main(y:5534)

REW 已完全越过 L&F 装配阶段，开始请求屏幕分辨率/图形环境——这是 P2 图形后端的正式入口。

## 性质变化

| 阶段 | 缺什么 | 性质 |
|---|---|---|
| 之前 | java.desktop 类与 native | 移植问题（已解决） |
| 现在 | 非 headless 的 GraphicsEnvironment | 实现问题（写代码） |

## 下一步方案

JDK 支持用系统属性替换图形环境实现：

    -Djava.awt.headless=false
    -Djava.awt.graphicsenv=<our.GraphicsEnvironment>

需要的最小集合：GraphicsEnvironment（注册入口）、GraphicsDevice（屏幕尺寸/DPI）、
GraphicsConfiguration（bounds/transform）。先让 getScreenResolution 不再抛异常，
REW 即可继续初始化；之后才是把 Java2D 画到 OHOS Surface。

## 补丁清单

| 补丁 | 内容 |
|---|---|
| 0001-0003 | build sysroot 分离、ohos configure 模式、HotSpot libc 适配（社区提供） |
| 0004 | java.desktop 构建调整（X11 stub、libjsound 在 headless 下跳过） |
| 0005 | InputContext.getLocale() 返回默认 Locale + jsound 条件 |

## 备注

应用内 runtime 解压只判断 lib/modules 是否存在，runtime 更新后必须 uninstall/install 才生效；
下一轮改成按 zip 摘要判断版本。
