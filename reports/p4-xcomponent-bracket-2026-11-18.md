# P4 上屏：XComponent 骨架差一个括号 · 2026-11-18

## 本轮进展

1. 确认 `OH_NativeWindow_NativeWindowFlushBuffer` 签名：`(window, buffer, fenceFd, Region)`；
2. `Region` 结构是 `{ Rect* rects; int32_t rectnum; }`（不是 x/y/w/h）；
   传 `{nullptr, 0}` 表示填充整个 buffer；
3. 修正后解决了 Region 相关 3 个错误；
4. 新问题：`RegisterXComponent` 的命名空间位置——脚本处理时多补了一个 `}`。

## 当前错误

    xcomponent_bridge.cpp:71:1: error: extraneous closing brace ('}')

## 下一轮（30 秒的事）

删掉第 71 行多余的 `}` 即可编译通过，然后：
1. 部署验证 XComponent 显示纯绿色（骨架可用）；
2. 再接入 REW 的 `int[]` 像素。

## 记录

这轮的问题全是 API 细节（FlushBuffer 签名、Region 结构、命名空间位置），
不是设计问题。骨架代码本身是对的（回调注册 + mmap + 填充 + flush 的流程完整）。
