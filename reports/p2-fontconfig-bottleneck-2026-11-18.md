# P2 里程碑：进程存活、main 执行到字体管理器 · 2026-11-18

## 本轮三个修复

1. identity CMM 注入（-Dsun.java2d.cmm=ohos.cmm.OhosCmmServiceProvider）
   并打补丁让 CMSManager 用 system classloader 加载 provider
   （否则 Class.forName 走 bootstrap，找不到应用 classpath 的类，静默回退 lcms）。
2. 运行时补 --add-exports=java.desktop/sun.java2d.cmm=ALL-UNNAMED，
   否则报 IllegalAccessError: module java.desktop does not export sun.java2d.cmm。
3. patchelf --add-needed libawt_headless.so libfontmanager.so：
   libfontmanager 的 NEEDED 里只有 libawt，而它需要的 AWTFreeFont 定义在 libawt_headless。

## 结果：进程存活、main 真正执行

    pidof com.example.rewharmony → 14046（存活）
    I REWJVM: invoking main()...
    I REWJVM: main() threw:

## 新卡点：字体配置

    Caused by: java.lang.RuntimeException: Fontconfig head is null, check your fonts or fonts configuration

即 Swing 已经走到字体管理器初始化（libfontmanager 加载成功、AWTFreeFont 解析成功），
但 fontconfig 拿不到任何字体配置 —— 沙箱里没有字体目录/配置。

## 下一步

1. 把字体文件（Noto/思源）与最小 fontconfig 配置放进 HAP，启动时解压到沙箱；
2. 设置 FONTCONFIG_PATH / FONTCONFIG_FILE 指向沙箱配置；
3. 若仍走不通，改 fontpath.c 的 direct 扫描路径（不依赖 fontconfig）。

## 进度

从“崩溃在 lcms”到“进程存活、main 执行到字体管理器”，本轮跨过了颜色管理这道关，
也补掉了字体库的符号依赖。REW 距离创建第一个窗口只差字体配置。
