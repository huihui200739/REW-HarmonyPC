#!/usr/bin/env bash
# 编译 hotspot-server-libs 并验证 libjvm.so
#   jvm/build-jvm.sh --check-config
#   jvm/build-jvm.sh [--jobs N]
#   jvm/build-jvm.sh --verify-artifact
set -euo pipefail
source "$(dirname "$0")/env.sh"

CONFIG_DIR="$JDK_SOURCE_DIR/build/$CONF_NAME"
LIBJVM="$CONFIG_DIR/support/modules_libs/java.base/server/libjvm.so"
JOBS="${JOBS:-$(sysctl -n hw.ncpu 2>/dev/null || echo 8)}"

verify_artifact() {
  [[ -f "$LIBJVM" ]] || { echo "缺 libjvm.so：$LIBJVM" >&2; return 1; }
  local desc
  desc="$(file -b "$LIBJVM")"
  echo "  $desc"
  case "$desc" in *AArch64*) ;; *) echo "  不是 AArch64" >&2; return 1 ;; esac
  ( "$OHOS_NDK_ROOT/llvm/bin/llvm-nm" -D --defined-only "$LIBJVM" 2>/dev/null || nm -D "$LIBJVM" ) \
    | grep -q JNI_CreateJavaVM || { echo "  未导出 JNI_CreateJavaVM" >&2; return 1; }
  if ( "$OHOS_NDK_ROOT/llvm/bin/llvm-readelf" -s "$LIBJVM" 2>/dev/null | grep -q "GLIBC_" ); then
    echo "  含 GLIBC_ 符号引用" >&2; return 1
  fi
  echo "  校验通过：AArch64 / JNI_CreateJavaVM / 无 GLIBC 符号"
}

case "${1:-}" in
  --check-config)
    [[ -f "$CONFIG_DIR/spec.gmk" ]] || { echo "未 configure：$CONFIG_DIR" >&2; exit 1; }
    echo "配置存在：$CONFIG_DIR"; exit 0 ;;
  --verify-artifact) verify_artifact; exit $? ;;
esac

JOBS_ARG="-j$JOBS"
[[ "${1:-}" == "--jobs" ]] && JOBS_ARG="-j$2"

[[ -f "$CONFIG_DIR/spec.gmk" ]] || { echo "未 configure：先跑 jvm/configure-jdk.sh" >&2; exit 1; }
echo "构建 hotspot-server-libs（$JOBS_ARG）…"
( cd "$JDK_SOURCE_DIR" && make CONF="$CONF_NAME" "$JOBS_ARG" hotspot-server-libs )
verify_artifact
