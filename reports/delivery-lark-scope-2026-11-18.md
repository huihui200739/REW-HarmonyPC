# 飞书登记：缺应用 scope，需在开发者后台申请 · 2026-11-18

## 实际调用结果

    lark-cli task +create --summary "..."
    → ok: false
      type: authorization / app_scope_not_applied / code 99991672
      message: access denied: app cli_aac5c79bf9b85bee has not applied for the required scope(s):
               task:task:write, task:task:writeonly

## 两条可行路径

### A. 应用后台申请 scope（推荐，一次配好长期可用）

    打开：https://open.feishu.cn/page/scope-apply?clientID=cli_aac5c79bf9b85bee
          &scopes=task%3Atask%3Awrite%2Ctask%3Atask%3Awriteonly
    申请 `task:task:write` 与 `task:task:writeonly`，然后发布应用版本。

### B. 用户身份授权（只需一次，但当前也缺 user token）

    lark-cli auth login

然后命令加 `--as user`。用户 token 需要 `task:task:read` 与 `task:task:write`。

## 与侧载站的对比

| 项 | 阻塞性质 | 解法 |
|---|---|---|
| 侧载站投稿 | 需要登录 Web 界面 | 浏览器登录上传 |
| 飞书登记 | 需要应用 scope 或 user token | 后台申请 scope 或 `auth login` |

## 代码侧交付已闭环

- REW 界面在鸿蒙屏幕显示（有证据）
- 签名 HAP 安装验证通过
- 双仓库同步
- 组织仓申请 #464 待审
