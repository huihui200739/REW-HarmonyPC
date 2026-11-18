# 组织仓申请已提交 · issue #464 · 2026-11-18

## 提交

    POST /repos/OpenHarmonyPCDeveloper/community/issues
    → #464 [新建仓申请--[ohos_REW]]
    https://atomgit.com/OpenHarmonyPCDeveloper/community/issues/464

## 申请正文要点

| 项 | 值 |
|---|---|
| 申请人 | 2501_93573368 |
| 仓库名 | ohos_REW |
| 上游 | roomeqwizard.com（闭源免费，EULA） |
| 许可证 | 适配层 Apache-2.0；**不含 REW 本体** |
| 分支 | main |
| 镜像 | atomgit.com/2501_93573368/ohos_REW |

正文如实说明了：
1. 上游闭源（不是开源项目），仓库只放运行时适配层；
2. 当前进展（JVM 交叉编译、java.desktop 全量、REW 启动、Java2D 渲染）；
3. 技术要点（DT_NEEDED 补依赖、后端下沉、可插拔图形环境、side table、identity CMM）；
4. 若社区不接受闭源软件适配仓，可改为存放通用 OHOS JVM 移植工具链。

## 参考先例

同仓库 #448~#463 均为“新建仓申请”，流程活跃（#462 即本项目 Olex2 的申请）。
