# P4 精确定位：我自己写的 colorConvert 返回数组长度不足，导致 toRGB 越界 · 2026-11-18

## JDK 的 toRGB 实现（ICC_ColorSpace.java:213-227）

    int nc = this.getNumComponents();
    short[] tmp = new short[nc];
    ...
    tmp = this2srgb.colorConvert(tmp, null);   // dest 传 null
    float[] result = new float[3];
    for (int i = 0; i < 3; i++) {
        result[i] = ((float) (tmp[i] & 0xffff)) / 65535.0f;   // 读 tmp[0..2]
    }

## 我们的实现（OhosColorTransform.java:45）

    public short[] colorConvert(short[] src, short[] dest) {
        short[] out = (dest != null && dest.length >= src.length) ? dest : new short[src.length];
        System.arraycopy(src, 0, out, 0, src.length);
        return out;
    }

## Bug

REW 的 ColorSpace 是**灰度（nc=1）**：`tmp` 长度 1，`dest=null` → 返回 `new short[1]`。
然后 `toRGB` 读 `tmp[1]`、`tmp[2]` → **ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1**。

这与屏幕、音频、ICC profile 全部无关——**是我自己的恒等变换数组长度算错了**。

## 修法

    short[] r = new short[out];            // out 分量数（3），不是 src.length
    if (src.length == out)      System.arraycopy(src, 0, r, 0, out);
    else if (src.length == 1)   { r[0] = r[1] = r[2] = src[0]; }   // 灰度 -> RGB
    else                        System.arraycopy(src, 0, r, 0, Math.min(src.length, out));
    return r;

byte[] 版本同理。

## 意义

P4 追踪 15+ 轮，最终根因是**一行数组长度计算**。
这也解释了为什么之前所有方向（屏幕/音频/profile/绘制）都试不通——它们都不是病因。
