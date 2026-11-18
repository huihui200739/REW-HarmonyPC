# P4 上屏：XComponent surface 已生效（显示黑色矩形）· 2026-11-18

## 本轮

1. 重写 `xcomponent_bridge.cpp`（干净的命名空间边界）；
2. 把 `RegisterXComponent` 的 `extern` 声明移到 `jvm_probe_napi.cpp` 的匿名 namespace **外面**；
3. `BUILD SUCCESSFUL`，部署启动，截屏。

## 结果

**XComponent 的 surface 区域出现了**——界面上出现一块 1600x1000 左右的黑色矩形。
（对比：之前 `Image` 方案是纯白空白，连区域都没有。）

## 为什么是黑色而不是绿色

`FillSolid(0xFF2E7D32)` 在 `OnSurfaceChangedCB` 里调用；如果该回调没触发，
surface 就保持默认（黑）。可能原因：

1. `OnSurfaceChanged` 没被调用（只触发了 `OnSurfaceCreated`）；
2. 或 `OH_NativeWindow_NativeWindowRequestBuffer` 返回失败；
3. 或 `SET_BUFFER_GEOMETRY` 在 RequestBuffer 之后调用无效（顺序问题）。

## 下一轮

1. 在三个回调里加 hilog，确认哪个被调用；
2. 把 `SET_BUFFER_GEOMETRY` 移到 RequestBuffer **之前**；
3. 在 `OnSurfaceCreated` 里也调用一次 FillSolid。

## 意义

**XComponent 这条路走通了**（surface 能创建、能显示），
剩下的是“往里写正确的像素”。比 Image 方案的死胡同强得多。
