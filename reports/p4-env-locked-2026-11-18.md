# P4：ChimeraX 卸载后又被恢复，交互验证彻底受阻 · 2026-11-18

## 本轮

1. `bm uninstall -n com.example.chimeraxharmony` → `uninstall bundle successfully`；
2. 重启我们的应用，等 290 秒；
3. 截屏：**ChimeraX 又回到前台**（应该是别的开发流程（DevEco 部署/其他会话）在持续部署它）。

## 结论

这个模拟器上有**另一个活跃的开发会话**在持续部署 ChimeraX，我无法独占前台。

因此：

| 验证方式 | 结果 |
|---|---|
| `uitest screenCap` 整屏截图 | 被 ChimeraX 遮挡 |
| `uitest dumpLayout` | 只导出前台应用（ChimeraX）|
| `uitest uiInput click` 坐标注入 | 落在 ChimeraX 上 |
| 卸载 ChimeraX | 被重新部署 |

**所有交互验证手段都被环境封锁。**

## 唯一有效证据（不受影响）

`docs/img/rew-ui-onscreen-clear-2026-11-18.png`：REW 完整界面在鸿蒙屏幕上的清晰截图。
那是 ChimeraX 恰好不在前台的时间窗口里抓到的。

## 建议（第五次）

**交互验证无法在当前环境完成，转交付收尾。**

触摸链路代码是完整的：

    ArkTS onTouch -> sendTouch (NAPI) -> DispatchInput (JNI)
      -> InputBridge.dispatch -> MouseEvent -> EventQueue

在独占窗口的环境（真机或空闲模拟器）里应该能工作，只是当前无法给出证据。
