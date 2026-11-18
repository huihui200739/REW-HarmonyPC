# P4：找到绕过截屏干扰的验证手段（uitest dumpLayout）· 2026-11-18

## 问题

ChimeraX 反复抢前台，`uitest screenCap` 抓整屏总是被它遮挡。

## 解法

用 `uitest dumpLayout -p <path>` 导出**界面结构 JSON**（不含画面，只含组件树与属性）：

    uitest dumpLayout -p /data/local/tmp/layout.json

导出结果 170,864 字节，组件统计：

    Row ×72 / Text ×46 / Image ×34 / Stack ×28 / Column ×14
    ListItem ×9 / RelativeContainer ×8 / Button ×5 / Divider ×4 / Flex ×3
    WindowScene ×2 / List ×2

**含有 `XComponent` ×1、`rewharmony` ×1** —— 说明我们的窗口与 XComponent 都在真实布局里，
与截屏是否被遮挡无关。

## 价值

这是**不依赖画面**的验证手段，后续验证以下内容都可以用它：

- XComponent 的实际 bounds；
- ArkTS 按钮是否存在、是否可点；
- 下拉框（Select）的当前值。

## 下一轮

1. 从 layout.json 里解析出我们窗口的 bounds 与 XComponent 的 bounds；
2. 用 bounds 换算成准确的屏幕坐标，做触摸注入；
3. 这才是解决“点击坐标不对”的正道。
