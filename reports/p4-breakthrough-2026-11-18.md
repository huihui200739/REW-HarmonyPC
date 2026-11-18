# P4 突破：修好 colorConvert 后，越界消失、组件树从 20 行涨到 142 行 · 2026-11-18

## 修复

`OhosColorTransform.colorConvert(short[], short[])` 返回数组按 `out`（3）而非 `src.length`（1），
灰度值复制到 RGB 三通道。

## 设备结果

| 指标 | 修复前 | 修复后 |
|---|---|---|
| `ArrayIndexOutOfBounds` 出现次数 | 1 | **0** |
| 组件树行数 | 20 | **142** |
| `roomeqwizard.*` 组件数 | 4 | **78** |

## 新出现的界面结构

    {roomeqwizard.fI$11 1600x1000 vis=true valid=false; kids=1          <- 新的主面板窗口
      JRootPane 1600x1000 valid=false; kids=2
        JPanel 1598x998 valid=false; kids=0
        JLayeredPane 1598x998 valid=false; kids=1
          JPanel 1598x998 valid=false; kids=1
            roomeqwizard.GC 1598x998 vis=true valid=false; kids=1     <- REW 主界面面板

**`roomeqwizard.GC` 就是 REW 的主界面容器**——之前因为越界，`constructGui` 在创建它时中断；
修复后界面构建继续推进，现在 GC 已经建立并在往下生长。

## 结论

P4 十几轮追踪的病因确认是 **`OhosColorTransform` 的数组长度 bug**，已修复。
REW 的界面构建已越过原来的断点，正在继续。

## 下一轮

1. 让等待时间更长（组件树还在生长），观察最终结构；
2. 组件树稳定后重新抓图，看是否能画出内容；
3. 若仍空白，继续查绘制路径（但这次起点是“界面已建好”）。
