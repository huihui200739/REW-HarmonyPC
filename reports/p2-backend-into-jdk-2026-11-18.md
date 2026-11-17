# P2：ComponentFactory 路线的导出死结，决定把后端下沉进 java.desktop · 2026-11-18

## 事实链

1. `Component instanceof ComponentFactory` 是组件创建的门，Toolkit 必须 implements 该接口；
2. ComponentFactory 的方法签名全部引用 `java.awt.peer.*` 类型；
3. `java.awt.peer` 在 JDK 17 中是未导出包；
4. `--add-exports` 对 classpath 编译无效；模块模式下 `--release` 与导出冲突；
5. 改 module-info 加 `exports java.awt.peer;` 后，构建报
   `error: warnings found and -Werror specified`（导出内部包触发警告），
   用 `DISABLE_WARNINGS_AS_ERRORS=1` 传给 make 也无效。

## 结论

外部 jar 实现 ComponentFactory 这条路被模块系统锁死。

## 决定：把后端下沉进 java.desktop

把 ohos.awt.* 的源码放进 `src/java.desktop/share/classes/ohos/awt/`，
作为 JDK 的一部分编译（包在 java.desktop 模块内，天然可访问 java.awt.peer）。

优点：
- 没有导出问题；
- 可以在 JDK 内部直接继承/实现任意内部接口；
- 与运行时是同一份代码，无版本错配。

代价：
- 每次改后端要重编 java.desktop（约 1 分钟，可接受）；
- 后端代码进 JDK 补丁集（需要纳入 patches/jdk/ 管理）。

## 下一轮

1. 把 platform/java/ohos/**  复制进 JDK 源码树；
2. 生成 OhosToolkit 的 ComponentFactory 实现（约 25 个方法，返回 Peers.make）；
3. 重编 java.desktop，runtime 里就会带上 ohos.awt 包；
4. 启动参数仍用 -Dawt.toolkit=ohos.awt.OhosToolkit（类在模块内，bootstrap 可见）。
