# 快速开始 · 从零到 REW 界面上屏

> 目标：在一台干净的 macOS + DevEco Studio 环境上，从 clone 到看到 REW 界面。
> 预计耗时：首次约 2-3 小时（主要是 JDK 编译与 runtime 打包）。

## 0. 前置条件

| 项 | 要求 |
|---|---|
| macOS | 13+（Apple Silicon 推荐）|
| DevEco Studio | 5.0+（含 OpenHarmony SDK API 24）|
| JDK | Temurin 17（boot JDK）|
| 磁盘 | ≥15 GB 空闲 |
| 设备 | HarmonyOS PC 模拟器或真机（API 24 / arm64 / 2in1）|

## 1. 获取源码与依赖

```bash
git clone https://github.com/huihui200739/REW-HarmonyPC
cd REW-HarmonyPC

# 拉取 BiSheng JDK 17 源码（约 500 MB）
jvm/fetch-jdk.sh
```

## 2. 构建 OpenJDK 运行时

```bash
# 应用补丁（ohos 平台 + java.desktop 适配）
jvm/apply-jdk-patches.sh

# 配置（交叉编译到 aarch64-linux-ohos，headless-off）
jvm/configure-jdk.sh

# 编译 JVM 与 java.base（jobs 按机器核数调）
jvm/build-jvm.sh --jobs 6

# jlink 出最小 runtime（73 MB，12 个模块）
jvm/build-runtime.sh
```

## 3. 准备 REW

从 https://www.roomeqwizard.com/ 下载 Linux 版安装包，解包后把 27 个 jar
（含 `.install4j/i4jruntime.jar`）打成一个 zip：

```bash
cd <REW 解包目录>
cp .install4j/i4jruntime.jar .
zip -qj rew-jars.zip *.jar     # 注意 -j 不递归，i4jruntime 要单独 cp 过来
```

放到 `app/entry/src/main/resources/rawfile/rew-jars.zip`。

同时把 `jvm/build-runtime.sh` 生成的 runtime 打成 `jre-runtime.zip` 放同目录。

## 4. 构建与安装 HAP

```bash
# 构建（hvigor 不接受中文路径，建议 rsync 到 /tmp 构建）
rsync -a --exclude build --exclude oh_modules --exclude .hvigor app/ /tmp/rew-build/app/
cd /tmp/rew-build/app && ohpm install
hvigorw assembleHap --mode module -p product=default -p buildMode=debug --no-daemon

# 签名（用 SDK 自带调试证书，无需华为账号）
/path/to/REW-HarmonyPC/scripts/sign-hap.sh \
  entry/build/default/outputs/default/entry-default-unsigned.hap

# 安装
hdc install -r entry-default-signed.hap
hdc shell aa start -a EntryAbility -b com.example.rewharmony
```

## 5. 看到界面

等约 4-5 分钟（REW 构建 UI + 渲染），然后：

```bash
# 渲染结果会写进沙箱
hdc shell ls -l /data/app/el2/100/base/com.example.rewharmony/haps/entry/files/rew-window.png
hdc file recv /data/app/el2/100/base/com.example.rewharmony/haps/entry/files/rew-window.png ./rew-window.png
```

同时 XComponent 上会显示同一个画面（直接看设备屏幕）。

## 6. 常见问题

| 现象 | 原因 | 解法 |
|---|---|---|
| `Cannot find autoconf` | 缺 build 依赖 | `brew install autoconf` 或解包 Homebrew bottle |
| `make -jN not supported` | JDK make 用 `JOBS=` | `jvm/build-jvm.sh --jobs 6` |
| hvigor 报中文路径错误 | 路径含中文 | rsync 到 `/tmp` 构建 |
| `java.desktop … initIDs` UnsatisfiedLinkError | libawt 未 dlopen | 检查 `jvm_probe_napi.cpp` 的预加载列表 |
| `ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1` | `colorConvert` 返回数组长度错 | 见 README「关键修复」|
| 离屏渲染空白 | 用了 `printAll` | 改 `paint` |
| XComponent 全黑 | `SET_BUFFER_GEOMETRY` 位置错 | 移到 `RequestBuffer` 之前 |
| `UnsatisfiedLinkError: SurfaceBridge.blit` | native 方法未注册 | 检查 JNI 初始化里的 `RegisterNatives` |

## 7. 验证脚本

```bash
scripts/verify.sh              # 补丁哈希 + 依赖清单 + 构建入口
scripts/sign-hap.sh <hap>      # 一键签名
```
