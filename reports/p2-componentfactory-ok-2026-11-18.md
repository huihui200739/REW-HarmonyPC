# P2 里程碑：后端下沉成功，ComponentFactory 生效，崩溃点推进到字体渲染 · 2026-11-18

## 完成

1. 把 ohos.awt.* / ohos.cmm.* 下沉到 src/java.desktop/share/classes/ohos/，作为 JDK 模块内代码；
2. 实现 sun.awt.ComponentFactory 的 23 个 create* 方法（返回 Peers.make(...)）；
3. 踩坑修复：
   - 中文注释导致 `unmappable character for encoding ascii` → 全部改英文；
   - 缺 import java.awt.peer.* → 补上（同模块内可直接访问）；
   - `serialVersionUID` 与 deprecation 警告触发 -Werror → 加 @SuppressWarnings。
4. `make java.desktop-jmod` 成功（MAKE=0），runtime 与 HAP 重打包。

## 设备结果

- 不再出现 `AWTError: UI components are unsupported`；
- 崩溃线程仍是 AWT-EventQueue-；
- 新的崩溃栈：

      #01 libfontmanager.so
      #02 libfreetype.so
      #03 libfreetype.so
      #04 libfreetype.so

即 **REW 已经走到字体渲染**（FreeType 加载字形）。

## 判断

ComponentFactory 这一步通了。现在的崩溃在 FreeType 层，可能与以下有关：
1. 我们提供的 Arial TTF 与 FreeType 版本的兼容性；
2. fontconfig stub 返回 null 导致 fontpath.c 的内部状态异常；
3. FreeType 在 OHOS 上的 mmap/内存限制。

## 下一轮

1. 抓 libfontmanager 崩溃的具体函数（readelf/addr2line 定位偏移）；
2. 试换成更简单的字体（或 JDK 自带测试字体）；
3. 若与 fontconfig 有关，改为完全绕过 fontconfig 的 direct 扫描路径。
