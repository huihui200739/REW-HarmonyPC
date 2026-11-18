# 合规修正：停止跟踪上游二进制 · 2026-11-18

## 问题

前 50 余轮把以下文件提交进了仓库：

| 文件 | 大小 | 问题 |
|---|---|---|
| rew-jars.zip（REW 的 27 个 jar） | 30 MB | **违反 REW EULA（不得分发上游二进制）** |
| fonts/Arial*.ttf | 2.5 MB | Apple 字体许可限制 |
| jre-runtime.zip | 16 MB | 生成物（可再生成，不必入库） |
| ohos-awt.jar | 8 KB | 生成物 |

此外 `.git` 已达 360 MB（其中 pack 60 MB），不适合作为公开镜像。

## 已做

1. `git rm --cached` 移除上述文件；
2. `.gitignore` 补齐对应规则（含 `app/entry/libs/`）；
3. 提交 `chore: 停止跟踪上游二进制与生成物`；
4. 验证：HEAD 树里已无这些文件（grep 计数 0）。

## 未做（TODO）

1. 历史清理：`.git` 仍含旧 blob（360 MB），需要 `git filter-repo` 或重建 orphan 分支后 force push；
2. 推送清理提交（本轮 push 时 GitHub 又不通，稍后重试）；
3. 字体改用开源字体（如 Noto Sans SC）或改为运行时由用户提供。

## 教训

“先把东西跑起来”的阶段容易忽略“什么不该进仓库”。合规检查应该在第一次提交时就做，
而不是等到准备公开镜像时才发现。
