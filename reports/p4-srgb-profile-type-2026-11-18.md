# P4：sRGB profile 已实现，待修类型关系 · 2026-11-18

## 本轮

按 ICC v2 规范实现了完整的 sRGB profile（纯 Java，约 150 行）：

| 标签 | 内容 |
|---|---|
| header | size / version 2.1.0 / 'mntr' / 'RGB ' / 'XYZ ' / 'acsp' / D50 illuminant |
| `wtpt` | D50 (0.9642, 1.0000, 0.8249) |
| `rXYZ` / `gXYZ` / `bXYZ` | sRGB 三原色（50/50 定点） |
| `rTRC` / `gTRC` / `bTRC` | curveType gamma 2.2（u8Fixed8 = 563） |
| `desc` / `cprt` | ASCII 文本 |

## 编译错误（待修）

    OhosCmm.java:9: error: constructor OhosProfile in class OhosProfile cannot be applied to given types
    OhosCmm.java:12: error: incompatible types: Profile cannot be converted to OhosProfile

原因：我让 `OhosProfile extends java.awt.color.ICC_Profile`，
但 `sun.java2d.cmm.PCMM.loadProfile()` 要求返回 `sun.java2d.cmm.Profile`——**两个不同的抽象类**。

## 下一轮

1. 让 `OhosProfile extends sun.java2d.cmm.Profile`；
2. 构造时创建内部 `ICC_Profile` 实例（用 `ICC_Profile.getInstance(byte[])` 需要 native，
   所以要么再写一个 `ICC_Profile` 子类，要么让 CMM 直接返回我们自己的 Profile 并让
   `ICC_ColorSpace` 从 `getData()` 走纯 Java 解析）；
3. 最简路径：`OhosProfile extends Profile`，持有一个 `byte[] data`，
   `OhosCmm.getProfileData()` 直接返回它，`ICC_ColorSpace` 侧用自己的 `ICC_Profile` 包装。
