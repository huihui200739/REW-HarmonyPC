# P4：ArkTS onTouch 转发已加，触摸仍未触发 · 2026-11-18

## 本轮

1. native 导出 `sendTouch(type, x, y)` NAPI 方法（内部调 `DispatchInput`）；
2. ArkTS 给 XComponent 加 `.onTouch()`，把 Down/Up/Move 转成 0/1/2 调 `sendTouch`；
3. `index.d.ts` 补 `sendTouch` 声明（否则 ArkTS 编译报“no exported member”）；
4. `BUILD SUCCESSFUL`，部署，`uitest uiInput click 450 680`。

## 结果

**仍然没有 `Touch type=` 日志**——ArkTS 的 `.onTouch()` 也没被触发。

## 可能原因

1. `uitest uiInput click` 注入的是系统级触摸，可能被 HAP 窗口的装饰层拦截；
2. 坐标 (450,680) 可能落在 XComponent 之外（XComponent 大约从 y≈530 开始，但其内部坐标系与屏幕坐标不同）；
3. XComponent 的 `onTouch` 需要 `hitTestBehavior` 或父容器配置；
4. 应用窗口在前台但没有焦点（`aa start` 后窗口未必自动获得输入焦点）。

## 下一轮方向

1. 先验证「窗口是否获得焦点」：用 `uitest uiInput click` 点击 ArkTS 的「看日志」按钮，
   看日志文本是否变化（这是纯 ArkTS 交互，不经过 XComponent）；
2. 若纯 ArkTS 也不响应 → 是窗口焦点问题，与 XComponent 无关；
3. 若纯 ArkTS 响应 → 问题在 XComponent 的触摸接收配置。

## 现状

| 项 | 状态 |
|---|---|
| REW 界面上屏 | ✅ |
| BlitFrame | ✅ 1600x1000 |
| sendTouch NAPI | ✅ 已导出 |
| ArkTS onTouch | ✅ 代码就位 |
| 触摸事件实际触发 | ❌ 未验证成功 |
