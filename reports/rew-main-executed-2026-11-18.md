# 里程碑：REW 主类加载成功、main() 已执行到 Swing 初始化 · 2026-11-18

## 1. 主类加载成功

```
FindClass(REW main)=OK
```

此前是 NoClassDefFoundError: javax.swing.plaf.PanelUI（补齐 java.desktop 后通过）；
再此前是 com/install4j/api/launcher/StartupNotification$Listener（补 i4jruntime.jar 后通过）。

## 2. main() 真正执行了

```
invoking main()...
Exception in thread "main" java.lang.NullPointerException:
  Cannot invoke "java.util.Locale.getCountry()" because the return value of
  "java.awt.im.InputContext.getLocale()" is null
	at roomeqwizard.RoomEQ_Wizard.x(y:6581)
	at roomeqwizard.RoomEQ_Wizard.e(y:4537)
	at roomeqwizard.RoomEQ_Wizard.A(y:6462)
	at roomeqwizard.RoomEQ_Wizard.B(y:5811)
	at roomeqwizard.RoomEQ_Wizard.main(y:5534)
```

调用栈里已经出现：

```
at java.desktop/java.awt.Font.toString
at org.jdesktop.swingx.plaf.DefaultsList.add
at org.jdesktop.swingx.plaf.TaskPaneAddon.addBasicDefaults
at org.jdesktop.swingx.plaf.LookAndFeelAddons.contribute
at org.jdesktop.swingx.JXTaskPane.<clinit>
at roomeqwizard.lG.<clinit>(y:2510)
at roomeqwizard.RoomEQ_Wizard.main(y:4100)
```

即：FlatLaf/Metal L&F + SwingX 的类初始化链已经在 OHOS 的 JVM 里跑通，
Font 对象能创建，UI 默认值表能装配。

## 3. 当前唯一卡点

java.awt.im.InputContext.getLocale() 返回 null（无输入法/无图形环境），REW 未做空判断，
直接调 .getCountry() → NPE。

修复方向：给 JDK 打补丁，让 InputContext.getLocale() 在无输入法时返回 Locale.getDefault()
（headless/无 IME 环境下的合理语义），作为 patches/jdk/0005。

## 4. runtime 现状

| 项 | 值 |
|---|---|
| jlink 模块 | java.base, java.desktop, java.logging, java.prefs, java.xml, java.datatransfer, java.scripting, java.management, java.naming, jdk.unsupported, jdk.crypto.ec, java.sql |
| runtime 大小 | 73 MB |
| native 库 | 20 个 .so，全 AArch64、无 GLIBC 引用 |
| HAP | 74 MB（runtime zip 16 MB + 26 个 jar 31 MB） |

## 5. 下一步

1. 0005 补丁修 InputContext locale；
2. 继续跑，逐个消除 AWT/字体/输入法层面的空值假设；
3. 之后进入真正的图形后端（Java2D → OHOS Surface）与音频（OHAudio）。
