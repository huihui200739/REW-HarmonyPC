# P4：SurfaceBridge.blit 未注册（UnsatisfiedLinkError）· 2026-11-18

## 本轮验证

`rew-window.png` 生成成功（11:10，131232 字节），但：

1. 没有 `REWXC: BlitFrame` 日志；
2. stderr 里有：

       [OHOS-SD] SurfaceBridge.push failed:
         java.lang.UnsatisfiedLinkError: 'void ohos.awt.SurfaceBridge.blit(int[], int, int)'

## 结论

**native 方法没注册成功**。原因可能是：

1. `FindClass("ohos/awt/SurfaceBridge")` 在注册时机点失败（类未加载 / classloader 不匹配）；
2. 注册代码在 SunToolkit 那块之后，但被前面的异常/return 跳过；
3. `jvm_probe_napi.cpp` 里那段注册代码的位置在某个 `if` 分支内未执行。

## 下一轮

1. 加 JLOG 打印 `SurfaceBridge class=found/missing` 与 `register ret=N`；
2. 若 FindClass 失败，改用 `Class.forName(name, false, ClassLoader.getSystemClassLoader())`；
3. 注册成功后再验证 `BlitFrame` 日志与画面。

## 当前状态

| 环节 | 状态 |
|---|---|
| REW 渲染成 PNG | ✅ 131232 字节 |
| SurfaceBridge Java 侧 | ✅ 编译进 runtime |
| SurfaceBridge native 注册 | ❌ UnsatisfiedLinkError |
| BlitFrame | 待注册后触发 |
