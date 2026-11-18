# 交付：飞书任务登记需要用户授权 · 2026-11-18

## 检查结果

`lark-cli` 已安装（`~/.local/n/bin/lark-cli`，v1.0.67），appId `cli_aac5c79bf9b85bee`。

    lark-cli task +get-my-tasks
    → identity "bot" is not supported, this command only supports: user

    lark-cli task +get-my-tasks --as user
    → need_user_authorization (user: ou_9e47d2d3ceafda468d48ed5324fb24f1)
      hint: run: lark-cli auth login to re-authorize
      requires scope(s): task:task:read

## 结论

**飞书任务清单登记需要用户身份授权**（`task:task:read` / `task:task:write` 等 scope），
而 `lark-cli auth login` 是需要交互的（浏览器/扫码）。

## 交付进度总览

| 项 | 状态 | 阻塞 |
|---|---|---|
| 代码 / 补丁 / 文档 | ✅ | — |
| GitHub 推送 | ✅（最后 2 个 commit 待推）| 网络 |
| AtomGit 镜像 | ✅ tree 一致 | — |
| 组织仓申请 #464 | ✅ open 待审 | 社区 |
| **HAP 签名** | ⏸️ | 证书密码 |
| 侧载站投稿 | ⏸️ | 依赖签名 |
| **飞书任务登记** | ⏸️ | 用户授权（`lark-cli auth login`）|

## 需要用户做的两件事

1. **签名证书密码**（或在 DevEco 配一次自动签名）；
2. **飞书授权**：在本机执行 `lark-cli auth login`，或在 DevEco/浏览器里完成一次授权。

完成这两项后，我可以立即：产出签名 HAP → 投侧载站 → 登记飞书清单。
