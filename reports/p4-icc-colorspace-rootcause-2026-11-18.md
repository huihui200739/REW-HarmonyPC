# P4 真正根因：ICC_ColorSpace.toRGB 数组越界（identity CMM 的 profile 不完整）· 2026-11-18

## 完整堆栈（还原后）

    Caused by: java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1
      at java.awt.color.ICC_ColorSpace.toRGB
      at java.awt.Color.<init>
      at roomeqwizard.m.ρ(y:2827)
      at roomeqwizard.m.ο(y:2422)
      at roomeqwizard.m.<init>(y:3089)
      at roomeqwizard.HA.<init>(y:2651)
      at roomeqwizard.oI.П(y:1219)
      at roomeqwizard.oI.<init>(y:3385)
      at roomeqwizard.GC$_A$1.stateChanged(y:1226)
      at javax.swing.JTabbedPane.setSelectedIndex
      at roomeqwizard.GC.<init>(y:251)
      at roomeqwizard.fI.U(y:1946)
      at roomeqwizard.g.constructGui(y:2351)
      at roomeqwizard.RoomEQ_Wizard$9.run(y:5476)

## 结论

**REW 在构建界面时用 `new Color(...)`（带 ColorSpace 的构造），触发 `ICC_ColorSpace.toRGB`，
而我们的 identity CMM 返回的 ICC profile 只有 128 字节头、没有 TRC/矩阵标签，导致越界。**

这与屏幕设备、音频设备都无关——是**颜色管理**的问题。

## 为什么现在才暴露

之前 `Color` 构造走的是 sRGB 快路径；REW 的某个面板用了带 `ColorSpace` 的构造（可能是打印/导出相关），
才触发了 `toRGB` 的真实查表。

## 下一轮（明确）

构造一个**完整的最小 sRGB ICC profile**（ICC v2，约 500 字节）：

| 标签 | 内容 |
|---|---|
| `wtpt` | D65 白点 XYZ |
| `rXYZ`/`gXYZ`/`bXYZ` | sRGB 三原色 XYZ |
| `rTRC`/`gTRC`/`bTRC` | gamma 2.2 曲线（curveType，count=1，值=563/256） |
| `desc` | 'sRGB' |
| `cprt` | 版权字符串 |

这样 `toRGB` 能正常做矩阵运算，不再越界。
