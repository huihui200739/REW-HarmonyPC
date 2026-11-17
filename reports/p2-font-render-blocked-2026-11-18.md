# P2：字体渲染崩溃未消除，需确认补丁是否进入 runtime · 2026-11-18

## 本轮做了

1. 定位崩溃符号：libfontmanager.so 偏移 0x3cdb4 落在 `ReadTTFontFileFunc`（0x3cc84 之后）；
2. 确认设备上字体与配置都已部署（fonts/Arial*.ttf、jre/lib/fontconfig.properties.src）；
3. 打补丁让 X11FontManager 无条件使用 MFontConfiguration（properties 驱动），绕过 FcFontConfiguration；
4. `make java.desktop-jmod` 成功，runtime 与 HAP 重打包。

## 结果

崩溃点未变：

    #01 libfontmanager.so (ReadTTFontFileFunc)
    #02-04 libfreetype.so

两种可能：
1. 补丁没有进入 runtime（jlink 用的是缓存的 jmod / 或 java.desktop 的 java 类没重编）；
2. 补丁生效了，但 ReadTTFontFileFunc 崩溃另有原因（例如 FreeType 在 OHOS 上解析 TTF 的问题）。

## 下一轮（按顺序验证）

1. 在 runtime 的 java.desktop 里确认 X11FontManager 是否为新版（比较 class 大小/时间戳）；
2. 用 -Dsun.awt.debugFonts=true 看 FontConfiguration 实际选中的类；
3. 若确认走的是 MFontConfiguration，则问题在 FreeType 读字体：试换字体文件（如 DejaVu），
   或检查 FreeType 的内存分配（FT_New_Memory_Face 需要可写内存）。

## 进度

组件工厂已通，当前唯一卡点是字体渲染的第一步（读 TTF 头）。
