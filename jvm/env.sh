# 公共环境：OHOS NDK / 工具链 / 目录（被其他脚本 source）
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export ROOT_DIR

OHOS_NDK_ROOT="${OHOS_NDK_ROOT:-/Applications/DevEco-Studio.app/Contents/sdk/default/openharmony/native}"
export OHOS_NDK_ROOT
OHOS_API_LEVEL="${OHOS_API_LEVEL:-$(python3 -c "import json;print(json.load(open('$OHOS_NDK_ROOT/oh-uni-package.json'))['apiVersion'])" 2>/dev/null || echo 24)}"
export OHOS_API_LEVEL

JDK_SOURCE_DIR="${JDK_SOURCE_DIR:-$ROOT_DIR/build/jdk-source/bishengjdk-17}"
export JDK_SOURCE_DIR
TOOLCHAIN_DIR="${JDK_TOOLCHAIN_DIR:-$ROOT_DIR/build/jdk-toolchain/api-$OHOS_API_LEVEL}"
export TOOLCHAIN_DIR

OHOS_TARGET="aarch64-linux-ohos${OHOS_API_LEVEL}.0.0"
OPENJDK_TARGET="aarch64-unknown-linux-musl"
CONF_NAME="${JDK_CONF_NAME:-ohos-aarch64}"
SYSROOT="$OHOS_NDK_ROOT/sysroot"
NDK_BIN="$OHOS_NDK_ROOT/llvm/bin"

# boot JDK：优先 BOOT_JDK，其次常见安装位置
BOOT_JDK="${BOOT_JDK:-}"
if [[ -z "$BOOT_JDK" ]]; then
  for c in "$HOME/Downloads/jdk/jdk17/Contents/Home" /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home; do
    [[ -x "$c/bin/java" ]] && BOOT_JDK="$c" && break
  done
fi
export BOOT_JDK

make_toolchain_wrappers() {
  mkdir -p "$TOOLCHAIN_DIR/bin"
  cat > "$TOOLCHAIN_DIR/bin/ohos-clang" <<EOF
#!/bin/sh
exec "$NDK_BIN/clang" --target="$OHOS_TARGET" "\$@"
EOF
  cat > "$TOOLCHAIN_DIR/bin/ohos-clang++" <<EOF
#!/bin/sh
exec "$NDK_BIN/clang++" --target="$OHOS_TARGET" "\$@"
EOF
  chmod +x "$TOOLCHAIN_DIR/bin/ohos-clang" "$TOOLCHAIN_DIR/bin/ohos-clang++"
}
