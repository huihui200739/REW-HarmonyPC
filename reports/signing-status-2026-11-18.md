# 交付准备：文档补齐 + 签名条件盘点 · 2026-11-18

## 已完成

1. README 追加「已知限制」章节：
   - 界面被 REW 自身启动错误对话框（模态）阻塞，含实测依据；
   - 仅 `-Xint` 解释执行；
   - Java Sound 未实现；
   - 仅模拟器验证。
2. PGC 验证记录补充第 11-14 项（窗口数量、菜单栏、渲染阻塞、启动错误）。
3. 已推送 GitHub（`fed44ac`）。

## 签名条件盘点

| 项 | 状态 |
|---|---|
| 本机签名材料 | ✅ 存在于 `~/.ohos/config/`（`.p12` / `.cer` / `.p7b`） |
| 项目签名配置 | ❌ `app/build-profile.json5` 的 `signingConfigs` 为空数组 |
| 密码 | ❌ 未知（`storePassword` / `keyPassword` 未记录在任何项目里） |

## 结论

签名材料齐备，但**缺证书密码**。两条路：

1. 在 DevEco Studio 里手动配置签名（会让密码进入本地配置，可再提取）；
2. 提供密码后由脚本写入 `build-profile.json5` 并产出签名 HAP。

## 后续顺序

签名 → 侧载站投稿（hap.atomgit.com）→ 飞书任务清单登记。
