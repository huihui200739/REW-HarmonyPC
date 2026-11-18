# P4：触摸回调未触发（DispatchTouchEvent 未被调用）· 2026-11-18

## 本轮

1. `DispatchTouchEventCB` 加 hilog（打印 type/x/y）；
2. `InputBridge.dispatch` 改用 `getComponentAt(x, y)` 找最深组件后派发；
3. 构建部署，点击 `Cal files`（450,680）。

## 结果

日志里**没有 `Touch type=...`**，说明 `DispatchTouchEventCB` 根本没被调用。
`BlitFrame 1600x1000` 正常（渲染没问题）。

## 结论

**XComponent 没有收到触摸事件。** 可能原因：

1. ArkTS 侧 `XComponent` 在 `Column` 里，外层组件把触摸事件截获了；
2. XComponent 需要 `onTouch` 或 `hitTestBehavior` 配置才会把事件传给 native；
3. `type: 'surface'` 的 XComponent 默认只做渲染，触摸需显式注册。

## 下一轮

1. ArkTS 给 XComponent 加 `.onTouch((e) => { ... })` 手动转发；
   或用 `@ohos.multimodalInput` 监听全局触摸；
2. 或在 native `DispatchTouchEventCB` 里确认是否需要在 `OnSurfaceCreated` 后调用
   `OH_NativeXComponent_RegisterCallback`（已注册）之外的额外设置。

## 当前状态

| 项 | 状态 |
|---|---|
| REW 界面上屏 | ✅ 清晰显示 |
| BlitFrame 渲染 | ✅ 1600x1000 |
| InputBridge（Java 侧） | ✅ class=found |
| 触摸事件到达 native | ❌ 未触发 |
