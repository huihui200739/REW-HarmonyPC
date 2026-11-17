# P2：NPE 精确定位到 ICC_Profile.getData() 返回 null · 2026-11-18

## 栈

    java.awt.color.ICC_Profile.intFromBigEndian
    java.awt.color.ICC_Profile.getColorSpaceType
    java.awt.color.ICC_ColorSpace.<init>
    java.awt.color.ColorSpace$BuiltInSpace.<clinit>

ICC_Profile.getData() 的实现是：

    return CMSManager.getModule().getProfileData(cmmProfile());

即返回 null 的是颜色管理模块的 getProfileData。

## 两种可能

1. CMSManager 实际拿到的不是我们的 OhosCmm（-Dsun.java2d.cmm 未生效或被回退），
   走的是 lcms（其 getProfileData 在字段未初始化时返回 null）；
2. 我们的 OhosCmm.getProfileData 被调用，但传入的 Profile 不是 OhosProfile，
   且我们的分支返回了 byte[0]（不应为 null）—— 这条可以排除，除非实现有笔误。

## 下一轮（一步定位）

在 OhosCmm.getProfileData 里 System.err.println 打印调用与参数类型，
若完全没有输出，就证明 CMSManager 没用我们的 provider，需要检查 -Dsun.java2d.cmm 的生效情况
（CMSManager 读属性用的是 GetPropertyAction，理论上能读到 -D）。

## 备注

ColorSpace$BuiltInSpace 的静态初始化依赖内置 sRGB profile 的数据，
这是 Java2D 启动的必经之路；只要 getProfileData 能返回非 null 的字节即可继续。
