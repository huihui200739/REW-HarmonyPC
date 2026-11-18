# P4：FrameCapture 仍未被调用，接入路径需再查 · 2026-11-18

## 本轮

1. 把 native 里的 `goto after_main` 结构改写成普通 if 块；
2. 在 FindClass/GetStaticMethodID 两处加了 JLOG 诊断；
3. 重建 HAP、安装、启动，等待 95 秒。

## 结果

设备日志中**没有任何 FrameCapture 行**（连诊断的 `FrameCapture class=` 都没有），
也没有 rew-window.png。

## 推断

这段代码根本没被执行到，说明控制流在更早处就跳过了——
最可能是 `FindClass("roomeqwizard/RoomEQ_Wizard")` 之后的分支判断，
或者 main 调用处的结构被前面的 `if (rew)` 包住但没有进入。

## 下一轮（最小验证）

1. 在 `invoking main()` 之前加一条无条件 JLOG，确认该行真的执行；
2. 若无输出，说明整个 Java2D/FrameCapture 块都在某个未进入的分支里，需要打印分支条件；
3. 建议把 FrameCapture 调用提到 probe 函数最外层（不依赖 REW 类加载成功），
   这样可以独立验证。

## 阶段判断

P1/P2 完成、P3（Java2D 渲染）已突破，P4 刚起步且接入有障碍。
若要尽快形成可交付成果，建议下一步优先做“FrameCapture 独立可调用”这件事——
它是把 Java2D 能力转成“看得见的 REW 界面”的唯一桥梁。
