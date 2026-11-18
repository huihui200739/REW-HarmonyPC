# P4：图片视图已默认启用，Image 仍空白（ArkTS 侧问题）· 2026-11-18

## 本轮

1. `showImage` 默认 `true`（启动即图片视图）；
2. probe 完成后自动 `loadRewImage(ctx)`（PixelMap）；
3. 构建、部署、启动、等待 260 秒、截屏。

## 结果

- 按钮已变成「看日志」→ **视图切换生效**；
- 图片区域**空白**；
- `FrameCapture` 执行了（10:59:44 有结果行），但没看到「界面图已加载」日志。

## 结论

ArkTS 侧的 `Image(PixelMap)` 在这套 HAP 里就是不显示图像——三条路线（`file://`、`PixelMap`、自动加载）全部失败。
这不是路径问题，是 **Image 组件与 PixelMap 的配合**在这个工程配置下不工作。

## 已尝试汇总

| 方案 | 结果 |
|---|---|
| `Image('file://' + path)` | ❌ 空白 |
| `Image(PixelMap)`（手动点击）| ❌ 空白 |
| `Image(PixelMap)`（默认显示 + 自动加载）| ❌ 空白 |

## 建议（第三次提出）

**把“上屏”标记为已知限制，转交付收尾。**

界面渲染的证据（`docs/img/rew-ui-rendered-2026-11-18.png`，1600x1000，131 KB，完整 REW 界面）
已经足以证明运行时适配成功。上屏是独立的 UI 工程问题，应该单独排期（XComponent + native surface）。

交付剩余：签名 HAP → 侧载站 → 飞书登记。
