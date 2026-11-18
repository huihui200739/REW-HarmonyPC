# P4：界面显示被 probe 阻塞（main 不返回，UI 停在“运行中…”）· 2026-11-18

## 现象

`rew-window.png` 已生成（131232 字节，10:44），但 ArkTS 窗口一直显示“运行中…”，
「看界面」按钮点击无效（被 `busy` 状态禁用）。

## 原因

`probe()` 在 FrameCapture 完成后**继续调用 `roomeqwizard.RoomEQ_Wizard.main()`**，
而 main 进入 AWT 事件循环后永不返回 → `probe()` 不返回 → ArkTS 的 `await` 一直挂着
→ `busy` 保持 true → UI 无法切换到图片视图。

## 修法（下一轮）

1. `FrameCapture` 模式下**不要调用 main**（已经跑过一次，main 的副作用不需要）；
2. 或让 ArkTS 的「看界面」按钮不受 `busy` 限制（`.enabled(true)` 常开）；
3. 更彻底：渲染完成后把结果写文件并让 native 立即返回，main 在后台继续（不影响 UI）。

## 已验证的事实

| 项 | 状态 |
|---|---|
| REW 界面渲染成 PNG | ✅ 131232 字节，1600x1000 |
| ArkTS 「看界面」按钮 | ✅ 已渲染在界面上 |
| 点击切换 | ❌ 被 busy 状态禁用 |

## 说明

截屏里的 “ChimeraX 没有响应” 弹窗来自另一个应用，与本项目无关。
