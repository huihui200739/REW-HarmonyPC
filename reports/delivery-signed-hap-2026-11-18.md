# 🎉 交付突破：用 SDK 官方调试证书签出 HAP · 2026-11-18

## 关键发现

DevEco SDK 自带**完整的 OpenHarmony 调试签名材料**（不需要华为账号、不需要证书密码）：

    /Applications/DevEco-Studio.app/Contents/sdk/default/openharmony/toolchains/lib/
      OpenHarmony.p12                    (密钥库，密码 123456)
      OpenHarmonyProfileDebug.pem        (调试证书链)
      OpenHarmonyProfileRelease.pem      (发布证书链)
      UnsgnedDebugProfileTemplate.json   (profile 模板)
      hap-sign-tool.jar                  (签名工具)

## 三步签名流程

**1. 生成 profile（p7b）**

    java -jar hap-sign-tool.jar sign-profile \
      -keyAlias "openharmony application profile debug" \
      -signAlg SHA256withECDSA -mode localSign \
      -profileCertFile OpenHarmonyProfileDebug.pem \
      -inFile UnsgnedDebugProfileTemplate.json \
      -keystoreFile OpenHarmony.p12 \
      -outFile rew-debug.p7b \
      -keyPwd 123456 -keystorePwd 123456

→ `sign-profile success`，产出 3.5 KB p7b

**2. 对 HAP 签名**

    java -jar hap-sign-tool.jar sign-app \
      -keyAlias "openharmony application profile debug" \
      -signAlg SHA256withECDSA -mode localSign \
      -appCertFile OpenHarmonyProfileDebug.pem \
      -profileFile rew-debug.p7b \
      -inFile entry-default-unsigned.hap \
      -keystoreFile OpenHarmony.p12 \
      -outFile rew-signed.hap \
      -keyPwd 123456 -keystorePwd 123456

→ **`Create a sign info successfully.` → rew-signed.hap（71 MB）**

## 踩过的三个坑

| 坑 | 说明 |
|---|---|
| keyAlias 猜错 | 密钥库 8 个别名，只有 `openharmony application profile debug` 能签 profile 与 app |
| 证书必须是链 | 单个 `.cer` 报 `Profile cert must a cert chain`，必须用 SDK 的 `.pem` |
| hvigor 不收明文密码 | `storePassword` 必须 ≥32 字符（加密后的密文），所以绕过 hvigor 直接用 hap-sign-tool |

## 意义

**签名不再是阻塞项**——用 OpenHarmony 官方调试证书即可签名，无需华为账号。

## 下一步

1. 验证签名 HAP 能否安装（`hdc install`）；
2. 投侧载站 hap.atomgit.com；
3. 飞书任务登记（仍需用户授权）。
