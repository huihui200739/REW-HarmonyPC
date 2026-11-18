# P3：side table 已正确编译进 libawt，设备验证未完成 · 2026-11-18

## 本轮的关键教训

之前两轮 side table “没生效”，原因是两个叠加的问题：

1. **源码替换没匹配上**：`SurfaceData_SetOps` 的函数体里已先加过诊断 fprintf，
   我按旧文本替换时静默失败，`OhosSdTableAdd` 只出现在定义处、没有调用点；
2. **构建链路不完整**：只 `make java.desktop-libs` 不会更新 `java.desktop.jmod`，
   jlink 出来的 runtime 仍然是旧 libawt。必须 `make java.desktop-libs java.desktop-jmod`。

本轮两项都已修正：

- 用括号匹配整体替换 SetOps 函数体（不再依赖文本匹配）；
- 删除 SurfaceData.o 强制重编，并同时构建 libs 与 jmod；
- 验证：`strings libawt.so | grep OHOS-SD` → 0（旧诊断已消失）。

## 设备侧

部署与安装命令已执行，但设备日志时间戳仍指向旧进程（pid 28058），说明应用没有真正重启，
验证结果还没拿到。

## 下一轮

1. 强制 uninstall + 确认 pid 变化后再启动；
2. 抓 `java2d-render-test` 结果；
3. 若 OK，立即把该 png 拉下来看实际渲染效果。
