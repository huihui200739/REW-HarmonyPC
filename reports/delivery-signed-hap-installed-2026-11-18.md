# 🎉 交付验证：签名 HAP 安装成功 · 2026-11-18

## 验证

    hdc install -r /tmp/rew-sign/rew-signed.hap
    → [Info]App install path:... msg:install bundle successfully.
    → AppMod finish

**签名 HAP 在 HarmonyOS PC 模拟器上安装成功。**

## 完整交付链

```
未签名 HAP（71 MB）
  → hap-sign-tool sign-profile  （OpenHarmony 官方调试证书）
  → hap-sign-tool sign-app      （keyAlias: openharmony application profile debug）
  → rew-signed.hap（71 MB）
  → hdc install                 → install bundle successfully ✅
```

## 之前卡住的原因（已全部绕开）

| 原以为的阻塞 | 实际 |
|---|---|
| 需要华为账号登录 DevEco | ❌ 不需要，SDK 自带 OpenHarmony 调试证书 |
| 需要证书密码 | ✅ 默认 `123456` |
| hvigor 要求 32 位密文密码 | ✅ 绕过 hvigor，直接调 hap-sign-tool |

## 剩余交付项

| 项 | 状态 | 需要 |
|---|---|---|
| 侧载站投稿 | 🔄 | 公开仓库 ✅ + SPDX 许可证 ✅ + 2in1 ✅，需 Web 提交 |
| 飞书任务登记 | ⏸️ | 用户执行 `lark-cli auth login` |

## 资产

| 文件 | 路径 |
|---|---|
| 签名 HAP | `/tmp/rew-sign/rew-signed.hap`（71 MB）|
| 调试 profile | `/tmp/rew-sign/rew-debug.p7b` |
| 签名证书 | `/tmp/rew-sign/rew-debug.cer` |
| 签名脚本（待入库） | 建议加入 `scripts/sign-hap.sh` |
