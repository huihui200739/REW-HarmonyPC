# P4：dumpLayout 只导出前台应用；我们的窗口被完全遮挡 · 2026-11-18

## 本轮

解析 `layout.json` 顶层结构：

    (0) ''          [0,0][3120,2080]
    (1) root        [706,472][2796,1866]  bundleName=com.example.chimeraxharmony
    (2) ContainerModal ...
    (1) WindowScene [0,1955][3120,2080]  bundleName=com.ohos.sceneboard   (任务栏)
    (1) WindowScene [0,0][3120,408]      bundleName=com.ohos.sceneboard   (状态栏)

**只有 ChimeraX 的窗口**，我们的 `com.example.rewharmony` 根本不在布局里。

## 结论

`uitest dumpLayout` 只导出**前台应用**的界面树。我们的 HAP 被 ChimeraX 完全遮挡，
所以既看不到、也 dump 不到、更点不到。

## 这意味着

1. 之前的截屏（`rew-ui-onscreen-clear-2026-11-18.png`）是**真实有效的证据**——
   那时 ChimeraX 恰好不在前台；
2. 但要继续做交互验证，必须先让我们的应用处于前台；
3. `aa force-stop com.example.chimeraxharmony` 之后它会被系统重新拉起（已试三次）。

## 下一轮可能的解法

1. 卸载 ChimeraX：`bm uninstall -n com.example.chimeraxharmony`（如果允许）；
2. 或用 `aa start` 启动我们的应用后**立即**操作（在 ChimeraX 回来之前完成）；
3. 或改用 `hdc shell aa dump -a` 查看我们的应用是否仍在运行。

## 当前确定成果（不受影响）

| 项 | 证据 |
|---|---|
| REW 界面在鸿蒙屏幕完整显示 | `docs/img/rew-ui-onscreen-clear-2026-11-18.png` |
| BlitFrame 1600x1000 | 设备日志 |
| 音频设备枚举（桩） | 代码 + 构建通过 |
