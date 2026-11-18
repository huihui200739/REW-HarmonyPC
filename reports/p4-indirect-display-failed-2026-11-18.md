# P4：PixelMap 方案也未显示（截图被其他应用弹窗干扰）· 2026-11-18

## 本轮

1. ArkTS 改用 `image.createImageSource(buf).createPixelMap()` 加载沙箱 PNG，
   再用 `Image(pixelMap)` 显示；
2. 构建、部署、启动、点击「看界面」均执行；
3. 截屏显示窗口内容仍空白，且被 “ChimeraX 没有响应” 弹窗遮挡（另一个应用）。

## 判断

两条“间接显示”路线（`Image('file://')` 与 `PixelMap`）都没在浏览器视图里显示出图。
可能是：

1. ArkTS 的 `Image` 组件在这个 HAP 里缺少必要的声明/权限；
2. 或 `Image(this.rewPixels)` 的响应式更新没有触发重绘（`@State` 对 PixelMap 的可见性有限）；
3. 或截图时图片还在解码。

## 建议

**这条“间接显示”路线价值有限**——真正的上屏应该走 **XComponent + native surface**：

- ArkTS 侧 `XComponent({ type: 'surface' })`；
- native 侧拿到 `OH_NativeWindow`，把 Java 渲染的像素 memcpy 进图形缓冲区；
- 这是标准的 OHOS 上屏路径，能同时解决显示与后续的输入事件。

## 当前状态

| 项 | 状态 |
|---|---|
| REW 界面渲染成 PNG | ✅ 131 KB / 1600x1000（证据充分）|
| ArkTS 视图切换 | ✅ |
| Image / PixelMap 显示 | ❌ 两条路线都没出图 |
| XComponent 上屏 | ⬜ 未开始（推荐下一步）|
