# P2：字体配置需要真实配置与字体文件 · 2026-11-18

## 本轮

1. 给 FontConfiguration.getExtraFontPath()/getVersion() 做了降级（head==null 时返回 null/默认值），
   避免直接抛 RuntimeException：

       RuntimeException: Fontconfig head is null -> 不再出现

2. 但随即暴露更底层的问题：

       NullPointerException: Cannot read the array length because
       sun.awt.FontConfiguration.table_elcIDs is null

   即 FontConfiguration 没有加载任何字体配置表（因为我们跳过了读取），后续映射表全为 null。

## 结论

降级只能把错误往后推，字体这条路必须有真实配置：

- JDK 在 Linux 上靠 fontconfig 动态读取；OHOS 没有 fontconfig，需要提供 JDK 自己的
  fontconfig.properties（.src 文本会被运行时编译），并让它指向沙箱字体目录；
- 同时需要真实的 TTF 字体文件（测试用系统字体即可，不入库）。

## 下一轮计划

1. 写一份最小 fontconfig.properties.src（参考 make/data/fontconfig/bsd.fontconfig.properties），
   指定字体目录与 defaultFont/sequence 映射；
2. 把一份 TTF（如系统自带开源字体）打进 HAP 的 rawfile，启动时解压到 files/fonts；
3. 启动参数加 -Dsun.awt.fontconfig 或把配置放到 java.home/lib/fontconfig.properties.src；
4. 观察 REW 是否能创建第一个窗口。

## 状态

进程依然存活，main 仍在执行——只是卡在字体表初始化。
