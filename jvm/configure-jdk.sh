#!/usr/bin/env bash
# 配置 BiSheng JDK 17 交叉编译到 OHOS（aarch64）
#   jvm/configure-jdk.sh --check-tools    只检查依赖
#   jvm/configure-jdk.sh --print-command  打印 configure 命令
#   jvm/configure-jdk.sh                  执行 configure
set -euo pipefail
source "$(dirname "$0")/env.sh"

usage() { sed -n '2,6p' "$0"; exit 0; }

check_tools() {
  local ok=1
  [[ -d "$SYSROOT/usr/include" ]] || { echo "缺 OHOS sysroot: $SYSROOT" >&2; ok=0; }
  [[ -x "$NDK_BIN/clang" ]] || { echo "缺 NDK clang: $NDK_BIN/clang" >&2; ok=0; }
  [[ -n "$BOOT_JDK" && -x "$BOOT_JDK/bin/java" ]] || { echo "缺 boot JDK 17（用 BOOT_JDK 指定）" >&2; ok=0; }
  if [[ -n "$BOOT_JDK" && -x "$BOOT_JDK/bin/java" ]]; then
    local v; v="$("$BOOT_JDK/bin/java" -version 2>&1 | head -1)"
    [[ "$v" == *'"17'* ]] || { echo "boot JDK 必须是 17：$v" >&2; ok=0; }
  fi
  for t in make unzip zip; do command -v "$t" >/dev/null || { echo "缺工具: $t" >&2; ok=0; }; done
  command -v autoconf >/dev/null || echo "  提示：未装 autoconf（configure 已随源码提供时不影响构建）"
  [[ -f "$JDK_SOURCE_DIR/configure" ]] || { echo "缺 configure：$JDK_SOURCE_DIR" >&2; ok=0; }
  [[ $ok == 1 ]] && echo "工具链检查通过：API ${OHOS_API_LEVEL}，target ${OHOS_TARGET}，boot JDK ${BOOT_JDK}"
  return $((1-ok))
}

CONFIGURE_ARGS=(
  "--openjdk-target=$OPENJDK_TARGET"
  --with-toolchain-type=clang
  "--with-sysroot=$SYSROOT"
  "--with-boot-jdk=$BOOT_JDK"
  "--with-build-jdk=$BOOT_JDK"
  --with-jvm-variants=server
  --with-debug-level=release
  --with-stdc++lib=dynamic
  --enable-headless-only
  --enable-ohos-port
  --disable-warnings-as-errors
  --with-native-debug-symbols=none
  "--with-conf-name=$CONF_NAME"
  "--with-extra-cflags=-I$ROOT_DIR/jvm/x11-stub"
)

case "${1:-}" in
  --check-tools) check_tools; exit $? ;;
  --print-command)
    make_toolchain_wrappers
    printf 'CC=%s/bin/ohos-clang CXX=%s/bin/ohos-clang++ bash configure %s\n' \
      "$TOOLCHAIN_DIR" "$TOOLCHAIN_DIR" "${CONFIGURE_ARGS[*]}"
    exit 0 ;;
  "") ;;
  *) usage ;;
esac

check_tools
make_toolchain_wrappers
cd "$JDK_SOURCE_DIR"
CC="$TOOLCHAIN_DIR/bin/ohos-clang" CXX="$TOOLCHAIN_DIR/bin/ohos-clang++" bash configure "${CONFIGURE_ARGS[@]}"
echo "configure 完成：$JDK_SOURCE_DIR/build/$CONF_NAME"