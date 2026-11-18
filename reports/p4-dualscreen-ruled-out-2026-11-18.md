# P4：双屏假设排除，越界另有来源 · 2026-11-18

## 本轮

把 `OhosGraphicsEnvironment.getScreenDevices()` 从 1 个设备改为 **2 个**（虚拟第二屏），
重新构建部署。

## 结果

    Caused by: ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1

**错误完全一样**，说明不是屏幕设备数组。

## 其他候选（按可能性排序）

1. **音频设备**：REW 必然枚举输入/输出设备，Java Sound 我们完全没实现（`AudioSystem.getMixerInfo()` 返回空数组）；
2. **字体/字体族数组**：`getAvailableFontFamilyNames()` 我们只返回 4 个名字；
3. **显示器 ID / 图形配置**；
4. **JNA / usb4java 的设备列表**（REW 用它们做音频硬件探测）。

`Index 1 out of bounds for length 1` 与“音频设备数组为空或只有 1 个”同样高度吻合，
而 Java Sound 恰恰是我们明确未实现的模块。

## 下一轮（关键）

1. 先把 REW 的错误堆栈**完整还原**（导出的 HTML 被换行切碎，需按行拼接后提取所有 `at roomeqwizard.*`）；
2. 同时检查 `AudioSystem.getMixerInfo()` 在当前 runtime 下的返回值（大概率空数组）；
3. 若指向音频：给 Java Sound 补一个最小 MixerProvider（不发声，只让枚举非空），
   这是比实现完整 OHAudio 后端便宜得多的绕过方案。
