# 交付：侧载站投稿需要 Web 界面操作 · 2026-11-18

## 探查结果

| 项 | 结果 |
|---|---|
| `https://hap.atomgit.com/` | 200（Next.js 应用）|
| `/api`、`/api/haps`、`/api/apps`、`/api/v1/haps`、`/upload` | 全部 404 |
| 页面 HTML 里的 `/api/...` 路径 | 未找到（Next.js Server Actions，无公开 REST）|

## 结论

**侧载站没有公开的上传 API**，投稿需要 Web 界面操作：

1. 打开 https://hap.atomgit.com/
2. 用 AtomGit 账号登录（`2501_93573368`）
3. 上传 `rew-signed.hap`（71 MB）
4. 填写应用信息（名称、描述、仓库链接、许可证）

## 投稿材料（已齐备）

| 项 | 值 |
|---|---|
| 应用名 | Room EQ Wizard (REW) |
| 版本 | 5.31.3 |
| 签名 HAP | `/tmp/rew-sign/rew-signed.hap`（71 MB，已验install成功）|
| 公开仓库 | https://atomgit.com/2501_93573368/ohos_REW |
| 许可证 | Apache-2.0（适配层）|
| 设备类型 | 2in1 |
| 系统要求 | OpenHarmony API 24 / arm64 |

## 交付状态

| 项 | 状态 | 需要 |
|---|---|---|
| 代码/文档/镜像 | ✅ | — |
| 组织仓申请 #464 | ✅ open 待审 | 社区 |
| HAP 签名 + 安装验证 | ✅ | — |
| **侧载站投稿** | 🔄 | **Web 界面（登录 + 上传）** |
| **飞书登记** | ⏸️ | `lark-cli auth login` |

## 说明

剩余两项都是**需要登录态的 Web 操作**，命令行无法完成。
代码侧的交付已全部闭环。
