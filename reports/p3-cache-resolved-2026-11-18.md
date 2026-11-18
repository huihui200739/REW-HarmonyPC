# P3：四层缓存已打通，设备部署待完成 · 2026-11-18

## 本轮成果（构建侧）

彻底清理后重建，逐级验证：

| 层 | 含旧诊断标记 OHOS-SD |
|---|---|
| build 产物 support/modules_libs | 0 |
| runtime images/ohos-runtime/lib | 0 |
| HAP 内 libs/arm64-v8a（部署目录） | 0 |

即 **四层缓存链路已全部刷新**，新侧 table 代码确实进入了每一层。

## 设备侧

仍是旧进程（pid 30766，时间戳 08:43），说明这次 install/start 没有真正执行完，
验证结果尚未拿到。推测是 HAP 体积（70MB+）导致安装耗时超出等待窗口。

## 下一轮（分步执行，避免超时）

1. 单独执行 uninstall，确认返回；
2. 单独执行 install，等到 AppMod finish；
3. 单独 aa start，等 60s；
4. 读 pid + hilog + stderr。

## 备注

构建侧的问题已经解决（三轮缓存问题的收尾），剩下的只是把部署动作拆细。
