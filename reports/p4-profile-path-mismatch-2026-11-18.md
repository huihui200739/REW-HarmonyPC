# P4：完整 sRGB profile 编译通过，但越界仍在 · 2026-11-18

## 本轮

1. `OhosProfile` 改为 `extends sun.java2d.cmm.Profile`（`super(0L)`，数据在 Java 侧）；
2. 按 ICC v2 组装完整 sRGB profile（header + 9 个标签 + tag table）；
3. `OhosCmm.getProfileData` 返回该 profile 字节；
4. `MAKE=0`，runtime/HAP 重建部署。

## 结果

    Caused by: ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1  (仍出现)
    PNG 仍是 7080 字节（纯白）

## 说明什么

REW 的 `new Color(colorspace, ...)` 走的是 `ICC_ColorSpace.toRGB()`，
而它需要的不是 `PCMM.getProfileData`，而是 **`ICC_Profile` 对象自身的标签解析**——
`ICC_Profile` 是 `java.awt.color` 里的类，其 `getData()/getTagData()` 是 **native** 实现，
我们替换的 `PCMM` 只在 `CMM.createTransform` 那条路上生效。

## 下一轮

1. 检查 `ICC_ColorSpace.toRGB` 的源码，确认它到底读哪个数据源；
2. 若直接用 `profile.getData()`（native），则需要让 `ICC_Profile` 也能拿到我们的字节，
   办法是打补丁让 `ICC_Profile.getData()` 优先走 `CMM.getProfileData`；
3. 或者更简单：让 `OhosProfile` 同时实现 `ICC_Profile` 的 Java 侧解析（重写 toRGB 依赖的私有路径不可行，需补丁）。
