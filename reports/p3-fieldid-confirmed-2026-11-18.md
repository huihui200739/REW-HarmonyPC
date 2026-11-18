# P3：字段 ID 错位已确认，正在抓 jfieldID 对比 · 2026-11-18

## 已确认

完全重装（uninstall + install，pid 变为 23101）后复测：

    [OHOS-SD] SetOps sData=0x7fc2d16588 existing=0x1 new=0x7f7cd55700

仍是 `existing=0x1`，说明：
- 不是重复注册导致（手动 JNI_OnLoad 已移除）；
- 是 **pData 字段读到 valid 的 boolean 值**（字段 ID 错位）。

## 正在做

在 SetOps 里同时打印 pDataID 与 validID（两个 jfieldID 指针），
若两者相同或 pDataID 指向 valid 的偏移，即可确认 InitField 的查找出错。

## 可能的修复方向

1. 若 pDataID == validID：initIDs 里的 InitField 调用顺序/签名有问题，需要按字段真实签名重查；
2. 若两者不同但读值仍为 1：JNU_GetLongFieldAsPtr 在 OHOS 上的实现需要核对（jlong 读取偏移）；
3. 兜底方案：不依赖 pData 字段，改用 side table（native 侧 map<SurfaceData*, ops>）。
