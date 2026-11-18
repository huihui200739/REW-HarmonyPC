# 提交闭环第一步：git 历史清理 + AtomGit 镜像完成 · 2026-11-18

## 1. 历史清理

用 `git filter-branch --index-filter` 从全部历史中移除：

    app/entry/src/main/resources/rawfile/rew-jars.zip     (30 MB, REW 二进制，EULA 禁止分发)
    app/entry/src/main/resources/rawfile/jre-runtime.zip  (16 MB, 生成物)
    app/entry/src/main/resources/rawfile/ohos-awt.jar     (生成物)
    app/entry/src/main/resources/rawfile/fonts/           (Arial, 许可受限)
    app/entry/libs/                                       (构建产物)

结果：

| 指标 | 清理前 | 清理后 |
|---|---|---|
| size-pack | 60.13 MiB | **6.92 MiB** |
| 最大跟踪文件 | 30 MB | 76 KB (jni.h) |
| 提交数 | 66 | 66（历史保留） |

推送：`git push --force origin main` → `991ee38...92bdbfc (forced update)` 成功。

## 2. AtomGit 个人镜像

    tools/atomgit_mirror.sh REW-HarmonyPC ohos_REW "<描述>" f1be4ab0

    [1/4] 不存在，创建
    [3/4] To https://atomgit.com/2501_93573368/ohos_REW.git
    [4/4] 本地 tree: f1be4ab0... / ✅ 与源 tree 一致

镜像地址：https://atomgit.com/2501_93573368/ohos_REW

## 3. 下一步：组织仓申请

需要提 issue 到 `OpenHarmonyPCDeveloper/community`，但有一个**许可证前置问题**：

- 上游 REW 是**闭源**软件（EULA），不是开源项目；
- 组织仓通常要求开源许可证；
- 本仓库的适配层是 Apache-2.0，但不含 REW 本体。

提申请时需要明确说明这一点，或者先与社区确认这类“闭源软件运行时适配”是否属于可接受的仓库类型。

## 状态

- GitHub: https://github.com/huihui200739/REW-HarmonyPC（92bdbfc，6.9 MB）
- AtomGit 镜像: https://atomgit.com/2501_93573368/ohos_REW（tree 一致）
