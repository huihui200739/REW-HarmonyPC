# P2：字体配置未被采纳，需对齐 MFontConfiguration 格式 · 2026-11-18

## 本轮

1. 生成最小 fontconfig.properties.src（dialog/serif/sansserif/monospaced × plain/bold/italic/bolditalic，
   全部映射到 Arial），随 HAP 部署到 files/jre/lib/；
2. 同时部署 4 个 Arial TTF 到 files/fonts；
3. 结果：仍是

       NullPointerException: Cannot read the array length because
       sun.awt.FontConfiguration.table_elcIDs is null

## 定位

Linux 的字体系有两套实现：

| 类 | 数据来源 |
|---|---|
| FcFontConfiguration | fontconfig 库（动态） |
| MFontConfiguration | fontconfig.properties（静态） |

我们的 build 没有链接 fontconfig，理论上应选 MFontConfiguration，但从 NPE 看，
它没有成功读到我们的配置——很可能是配置字段不全（MFontConfiguration 需要比我们写的更多段，
例如 allfonts.*、defaultFont、以及更完整的编码表）。

## 下一轮

1. 直接以 make/data/fontconfig/bsd.fontconfig.properties 为底（189 行，字段完整），
   只替换字体名与 filename 路径，保留全部字段；
2. 若仍不生效，在 SunFontManager 里打印它实际选择的 FontConfiguration 类名，确认走的是哪条路；
3. 必要时打补丁强制使用 MFontConfiguration。

## 状态

进程持续存活，main 持续执行，卡点仍是字体表初始化。
