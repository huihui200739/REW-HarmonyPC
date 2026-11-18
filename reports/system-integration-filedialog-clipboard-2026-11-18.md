# 系统集成补齐：FileDialog / Clipboard / 音频后端 · 2026-11-18

## 本轮完成

### 1. FileDialog（最关键的缺口）

`ohos.awt.OhosFileDialogPeer`：用 `JFileChooser` 实现 `FileDialogPeer`。

| 行为 | 实现 |
|---|---|
| `setVisible(true)` / `show()` | 在 EDT 上弹 `JFileChooser` |
| 初始目录 | 取 `FileDialog.getDirectory()` |
| 初始文件名 | 取 `FileDialog.getFile()` |
| 打开/保存模式 | 按 `FileDialog.getMode()` 切 `JFileChooser` 类型 |
| 文件过滤器 | `*.mdat`（REW 测量文件）+ 全部文件 |
| 用户确认 | `target.setDirectory()` + `target.setFile()` 回写 |
| 用户取消 | `target.setFile(null)` |

**这解决了 REW 打开/保存 `.mdat` 完全无效的问题。**

### 2. 剪贴板

`ohos.awt.OhosClipboard`（进程内内存剪贴板）：

- `setContents` / `getContents` / `getData` / `isDataFlavorAvailable` 全部实现；
- 无内容时返回空字符串的 `Transferable`（不抛异常）。

### 3. Java Sound 音频后端（上轮完成，本轮合并）

- 播放：`OhosSourceDataLine` + OHAudio Renderer
- 采集：`OhosTargetDataLine` + OHAudio Capturer

## 构建

    java.desktop-jmod: MAKE=0
    HAP: BUILD SUCCESSFUL

## 剩余系统集成（仍为 stub）

| 模块 | 影响 |
|---|---|
| 打印 | REW 的“打印图表”无效（低频）|
| 输入法 | 中文输入不可用（英文可用）|

## 声音设备选择

按用户要求**只实现代码，不做验证**：

- `OhosMixerProvider` 提供 `OHOS Audio Out` / `OHOS Audio In` 两个 Mixer；
- `getLine` 返回真实的 `OhosSourceDataLine` / `OhosTargetDataLine`（接 OHAudio）；
- REW 的设备下拉框会显示这两个名字，选中后能真正打开音频线。
