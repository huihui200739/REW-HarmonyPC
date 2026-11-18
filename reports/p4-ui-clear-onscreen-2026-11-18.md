# P4：REW 界面在鸿蒙上完整清晰显示（无遮挡证据）· 2026-11-18

![REW on screen clear](img/rew-ui-onscreen-clear-2026-11-18.png)

## 本轮

1. `aa force-stop com.example.chimeraxharmony` 关掉干扰应用；
2. 重启 REW HAP，等待渲染完成（约 5 分钟）；
3. 截屏得到**无遮挡的完整 REW 界面**；
4. 点击 `Cal files` 标签（坐标 450,680），界面未切换。

## 界面内容（全部可读）

- 标签页：Soundcard / **Cal files** / Comms / Analysis / Equaliser / View
- Sample rate `48 kHz`、Output/Input device、Buffer `32k`
- 三个电平表 Out / In / Ref In（−10 ~ −100 dBFS）
- Soundcard calibration、Levels、Help 全文

## 输入事件状态

| 项 | 状态 |
|---|---|
| InputBridge 注册 | ✅ class=found |
| DispatchTouchEvent 回调 | ✅ 代码就位 |
| 点击标签页切换 | ❌ 未生效 |

## 点击未生效的可能原因

1. `XComponent` 默认不接收触摸事件？需要确认 `XComponent` 的 `onTouch` 或 native 回调是否被触发；
2. 坐标换算：`uitest click 450,680` 是屏幕坐标，`OH_NativeXComponent_GetTouchEvent` 返回的是组件内坐标，
   而 REW 的 Frame 在 HAP 窗口内又有偏移；
3. `MouseEvent` 派发到 Frame 而非具体组件（`dispatchEvent` 到 Frame 可能不传递到子组件）。

## 下一轮

1. 在 `DispatchTouchEventCB` 里加 hilog，确认回调是否被触发；
2. 若触发了，打印 `touchEvent.x/y` 看坐标；
3. 把 `dispatchEvent` 改为按坐标查找最深的组件（`Component.getComponentAt(x, y)`）。
