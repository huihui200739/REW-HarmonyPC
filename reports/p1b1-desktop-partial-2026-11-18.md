# P1-b1 进展：java.desktop 已产出 4 个库，剩余 3 类阻塞 · 2026-11-18

## 1. REW 确实需要 java.desktop（实测）

把官方 25 个 jar 打进 HAP 的 rawfile，应用内解压到 filesDir/jars，探针拼 classpath 后加载主类：

```
FindClass(ArrayList)=OK
FindClass(REW main)=FAIL
  exception: java.lang.NoClassDefFoundError: javax/swing/plaf/PanelUI
```

确认两件事：
1. REW 的 jar 已在 classpath 上被解析（否则会是 ClassNotFoundException: roomeqwizard.RoomEQ_Wizard）；
2. 第一个缺的类是 javax.swing.plaf.PanelUI，属于 java.desktop。

## 2. X11 stub 的用法（重要教训）

做法：jvm/x11-stub/ 提供最小头，configure 加 --with-extra-cflags=-I<stub>。

教训：给了 cups/cups.h 之后，configure 的 CUPS 探测“成功”，于是 libawt_headless 去编 CUPSfuncs.c；
不给反而会被 --enable-ohos-port 关掉。所以 stub 头只应提供无法避免的编译期类型（X11），
不要替系统库做存在性证明。

## 3. 当前产出

```
build/ohos-aarch64/support/modules_libs/java.desktop/
  libawt.so         661K
  libjavajpeg.so    205K
  liblcms.so        372K
  libmlib_image.so  442K
```

即 java.desktop 的主体已经能编出 4 个 native 库，说明 X11 stub 方向成立。

## 4. 剩余 3 类阻塞

| # | 错误 | 处理 |
|---|---|---|
| 1 | ppd_size_t 缺 left/top/right/bottom 字段 | 补 stub 字段（纯类型） |
| 2 | fontconfig/fontconfig.h 缺失（fontpath.c） | 提供 fontconfig stub；最终要接 OHOS 字体发现 |
| 3 | glxext.h:998 函数返回函数类型 | GLX 函数指针 typedef 需按真实 X11 头签写 |

## 5. 下一轮

1. 收尾这 3 类阻塞，让 java.desktop-libs 全绿；
2. 把 java.desktop 的 jmod 加入 jlink --add-modules java.base,java.desktop；
3. 重打包 HAP，用探针看 REW 主类能否进入初始化（下一个缺失点很可能是 java.awt.Toolkit 或字体）。

## 备注

libfdlibm 仍未真正链进 libjava（当前靠 libjdkshim.so 兜底）；等 java.desktop 打通后一并还这笔技术债。
