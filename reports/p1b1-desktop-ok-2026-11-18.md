# P1-b1 达成：java.desktop 全量编译通过 · 2026-11-18

## 结果

```
$ make CONF=ohos-aarch64 java.desktop-libs   → EXIT=0
build/ohos-aarch64/support/modules_libs/java.desktop/
  libawt.so  libawt_headless.so  libfontmanager.so  libfreetype.so
  libjavajpeg.so  libjawt.so  liblcms.so  libmlib_image.so
```

8 个 native 库全部产出，AWT/Swing 的 native 底座打通。

## 关键动作

| 动作 | 说明 |
|---|---|
| X11/GL/CUPS/fontconfig 最小 stub 头 | 放在 `jvm/x11-stub/`，configure 用 `--with-extra-cflags=-I<stub>` |
| `LIBAWT_HEADLESS_EXCLUDES` | 尝试排除 CUPSfuncs/fontpath，但 EXTRA_SRC 不受 EXCLUDES 控制，最终靠 stub 头编过 |
| **跳过 libjsound** | OHOS 没有 ALSA；用 `ifeq ($(ENABLE_HEADLESS_ONLY), false)` 包裹，headless 构建不编 jsound。Java Sound 后端在 P3 由 OHAudio 提供 |
| 导出补丁 | `patches/jdk/0004-ohos-java-desktop-build.patch`（344 行，含 build sysroot 分离与 desktop 构建调整） |

## stub 头踩坑清单（可复用于其它 JDK/OHOS 移植）

| 现象 | 修法 |
|---|---|
| `XImage` typedef 重定义 | 只保留 `struct _XImage {...}; typedef struct _XImage XImage;` |
| 缺 `XPixmapFormatValues` / `Region` | 补类型；`Region` 是 `REGION*` |
| `glxext.h` 函数返回函数类型 | `Status` 未定义 → 补 `typedef int Status;`；`__GLXextFuncPtr` 由 JDK 自带 glx.h 定义，stub 不要重复 |
| `ppd_size_t` 缺字段 | 补 `left/bottom/right/top` |
| `fontpath.c` 与 stub 的 typedef 冲突 | `FcCharSetDestroyFuncType` 等由 fontpath.c 自己定义，stub 里删掉 |
| 给了 `cups.h` 反而编 CUPSfuncs | stub 头只给“无法避免的编译期类型”，不要替系统库证明存在性 |

## 下一步

1. `make java.desktop-jmod` 并 `jlink --add-modules java.base,java.desktop`；
2. 把 8 个新 .so 加进 HAP 的 `libs/arm64-v8a`，runtime zip 重打；
3. 探针复测 `FindClass("roomeqwizard/RoomEQ_Wizard")`，看下一个缺失点（预期 `java.awt.Toolkit` 初始化或字体）。
