# 交付进展：AtomGit 镜像已同步到 T1 版本；签名仍缺密码 · 2026-11-18

## 镜像同步（完成）

    tools/atomgit_mirror.sh REW-HarmonyPC ohos_REW "<描述>" 4e5d2107
    [3/4] 92bdbfc..f02e205  main -> main
    [4/4] 本地 tree: 4e5d2107 / ✅ 与源 tree 一致

镜像：https://atomgit.com/2501_93573368/ohos_REW

## 签名（阻塞）

本机签名材料：

| 文件 | 说明 |
|---|---|
| `~/.ohos/config/release/YueJiPC_EC_Release.p12` | 发布证书密钥库 |
| `~/.ohos/config/release/YueJiPC_EC_Release.cer` | 证书 |
| `~/.ohos/config/release/YueJiPC_Release_Profile.p7b` | 描述文件 |
| `~/.ohos/config/default_*.p12/.cer/.p7b` | 调试证书组 |

尝试的常见密码（123456 / huawei / Huawei123456 / abc123 / 000000）均不匹配。

## 结论

**签名只差证书密码**，两条可行路径：

1. 用户在 DevEco Studio 里打开一次项目 → File → Project Structure → Signing Configs → 勾选自动签名，
   配置会写入 `app/build-profile.json5`（含加密后的密码引用），之后可直接 hvigorw 构建签名包；
2. 用户直接提供 `storePassword`/`keyPassword`。

## 其余交付项

| 项 | 状态 |
|---|---|
| 代码/文档推送 | ✅ f02e205 |
| AtomGit 镜像 | ✅ tree 一致 |
| 组织仓申请 #464 | ✅ 待审 |
| HAP 签名 | ⏸️ 缺密码 |
| 侧载站 | 依赖签名 |
| 飞书登记 | 待办 |
