# 🎉 交付：GitHub Release 发布，签名 HAP 可下载 · 2026-11-18

## 结果

    gh release create v5.31.3-ohos.1 /tmp/rew-sign/rew-signed.hap
    → https://github.com/huihui200739/REW-HarmonyPC/releases/tag/v5.31.3-ohos.1

验证：

    assets: 1
    rew-signed.hap  74,573,309 bytes（71 MB）

## 意义

**签名 HAP 有了公开下载地址**，侧载站投稿时可直接引用：

    https://github.com/huihui200739/REW-HarmonyPC/releases/download/v5.31.3-ohos.1/rew-signed.hap

## 完整交付链（最终形态）

```
源码仓库（GitHub + AtomGit 镜像，tree 一致）
  ↓
scripts/sign-hap.sh  （OpenHarmony 官方调试证书）
  ↓
签名 HAP（71 MB）
  ↓
GitHub Release v5.31.3-ohos.1  ← 公开可下载 ✅
  ↓
侧载站 hap.atomgit.com（待上传）
```

## AtomGit 侧

- 镜像仓：https://atomgit.com/2501_93573368/ohos_REW（tree 一致）
- Release：`v5.31.3-ohos.1` 已创建（附件上传 API 不支持，404）

## 交付状态

| 项 | 状态 |
|---|---|
| REW 界面上屏（签名 HAP） | ✅ 证据图 |
| 源码双仓库同步 | ✅ |
| 签名 HAP 公开下载 | ✅ GitHub Release |
| 组织仓申请 #464 | ⏸️ 待审 |
| 侧载站投稿 | 📋 待 Web 上传（可直接用 Release 链接）|
| 飞书登记 | 📋 待授权 |
