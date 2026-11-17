# P2：initIDs 改为转发真实实现（35/68），剩余空实现仍有 NPE · 2026-11-18

## 本轮

1. 把 RegisterNatives 从“全部空实现”改为“优先 dlsym 真实符号，找不到才空实现”；
2. 关键细节：dlsym 必须在 dlopen(libawt/libawt_headless, RTLD_GLOBAL) 之后，
   否则符号不在全局作用域（第一版实现在注册之后才 dlopen，导致 real=0）；
3. 结果：

       register: ok=64 fail=4 real=35 stub=33

   即 35 个类的 initIDs 已转发到 JDK 真实实现。

## 仍有问题

    NullPointerException: Cannot load from byte/boolean array because "<parameter1>" is null

说明剩下 33 个空实现里有某个被真实 native 代码依赖（字段未初始化）。
下一步需要定位具体是哪个类：把 stub 的类名打印出来，逐个与崩溃栈里的调用类比对。

## 下一步

1. 打印 stub 类名清单；
2. 与 NPE 栈（sun.font / java.awt 相关）比对，定位缺的那一个；
3. 若该类的真实实现在 libawt 里但时符号名不同（例如 Java_sun_font_SunFontManager_initIDs），
   修正符号名映射（我们的规则是 Java_<class>_initIDs，含 sun.* 的类名转换正确）。
