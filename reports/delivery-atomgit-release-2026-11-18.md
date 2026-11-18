# 交付：AtomGit Release 已创建（附件上传待确认）· 2026-11-18

## 本轮

在 AtomGit 镜像仓创建了首个 release：

    POST /repos/2501_93573368/ohos_REW/releases
    { "tag_name": "v5.31.3-ohos.1",
      "name": "REW 5.31.3 鸿蒙 PC 适配 v1",
      "body": "首个可在 HarmonyOS PC 上运行 REW 原版界面的版本..." }

→ 创建成功，`target_commitish` 指向 `e2583c91`。

## 附件上传

AtomGit 的 release 对象返回字段为：

    ['tag_name', 'target_commitish', 'prerelease', 'name', 'body',
     'author', 'created_at', 'assets', 'release_status']

**没有 `id` 与 `upload_url`**，说明它不是标准 Gitea/GitLab 结构，附件上传端点需要另行确认。

## 影响

签名 HAP（71 MB）暂时无法挂在 release 上。但这不影响交付：

- HAP 在本地 `/tmp/rew-sign/rew-signed.hap`；
- `scripts/sign-hap.sh` 可随时重新签名；
- 侧载站在线上传时直接选本地文件即可。

## 下一步

1. 侧载站投稿（Web 上传，需登录）；
2. 或研究 AtomGit 的附件上传端点（`/releases/{tag}/assets`？需试）；
3. 飞书登记（需 scope/授权）。
