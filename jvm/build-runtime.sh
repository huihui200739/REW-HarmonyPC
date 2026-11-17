#!/usr/bin/env bash
# 构建 java.base-only 最小 OHOS runtime（jlink），并校验所有 .so
#   jvm/build-runtime.sh              构建
#   jvm/build-runtime.sh --check-config
#   jvm/build-runtime.sh --verify-artifact
#   jvm/build-runtime.sh --artifact-path
# 思路来自社区 ohos_eclipse-ide（MIT）；本仓库独立实现与校验。
set -euo pipefail
source "$(dirname "$0")/env.sh"

CONFIG_DIR="$JDK_SOURCE_DIR/build/$CONF_NAME"
SPEC_FILE="$CONFIG_DIR/spec.gmk"
JMODS_DIR="$CONFIG_DIR/images/jmods"
RUNTIME_IMAGE="$CONFIG_DIR/images/ohos-runtime"
LLVM_READELF="$OHOS_NDK_ROOT/llvm/bin/llvm-readelf"
TEMP_IMAGE=""

fail() { echo "runtime 构建失败：$*" >&2; exit 1; }
cleanup() { if [ -n "$TEMP_IMAGE" ] && [ -d "$TEMP_IMAGE" ]; then rm -rf "$TEMP_IMAGE"; fi; }
trap cleanup EXIT

resolve_build_jdk() {
  [ -f "$SPEC_FILE" ] || fail "缺少配置：$SPEC_FILE"
  BUILD_JDK="$(sed -n 's/^BUILD_JDK:=[[:space:]]*//p' "$SPEC_FILE" | head -n 1)"
  [ -n "$BUILD_JDK" ] || fail "请用外部 --with-build-jdk 重新 configure"
  JLINK="$BUILD_JDK/bin/jlink"
  [ -x "$JLINK" ] || fail "找不到 jlink：$JLINK"
}

check_config() {
  [ -f "$SPEC_FILE" ] || fail "未 configure：$CONFIG_DIR"
  command -v make >/dev/null || fail "缺 make"
  [ -x "$LLVM_READELF" ] || fail "缺 llvm-readelf"
  resolve_build_jdk
  echo "runtime 配置就绪：${CONF_NAME}（build JDK: ${BUILD_JDK}）"
}

verify_native_lib() {
  local lib="$1" info
  info="$("$LLVM_READELF" -h -s -d "$lib")"
  grep -Fq 'Class:                             ELF64' <<< "$info" || fail "非 ELF64：$lib"
  grep -Fq 'Machine:                           AArch64' <<< "$info" || fail "非 AArch64：$lib"
  if grep -Eq 'GLIBC_[0-9]' <<< "$info"; then fail "含 GLIBC 符号版本：$lib"; fi
}

verify_artifact() {
  check_config
  [ -s "$RUNTIME_IMAGE/lib/modules" ] || fail "缺 lib/modules"
  [ -s "$RUNTIME_IMAGE/lib/server/libjvm.so" ] || fail "缺 lib/server/libjvm.so"
  [ -s "$RUNTIME_IMAGE/lib/libjava.so" ] || fail "缺 libjava.so"
  [ -s "$RUNTIME_IMAGE/release" ] || fail "缺 release 元数据"
  local n=0 so
  while IFS= read -r -d '' so; do verify_native_lib "$so"; n=$((n+1)); done < <(find "$RUNTIME_IMAGE/lib" -type f -name '*.so' -print0)
  local syms
  syms="$("$LLVM_READELF" -s "$RUNTIME_IMAGE/lib/server/libjvm.so" 2>/dev/null || true)"
  grep -Fq JNI_CreateJavaVM <<< "$syms" || fail "libjvm.so 未导出 JNI_CreateJavaVM"
  echo "校验通过：$n 个 .so 全为 AArch64 且无 GLIBC 引用"
  echo "runtime 路径：${RUNTIME_IMAGE}（$(du -sh "$RUNTIME_IMAGE" | cut -f1)）"
}

case "${1:-}" in
  --check-config) check_config ;;
  --artifact-path) echo "$RUNTIME_IMAGE" ;;
  --verify-artifact) verify_artifact ;;
  "")
    check_config
    echo "构建 java.base-jmod …"
    ( cd "$JDK_SOURCE_DIR" && make CONF="$CONF_NAME" CREATING_BUILDJDK=true java.base-jmod )
    [ -s "$JMODS_DIR/java.base.jmod" ] || fail "未产出 java.base.jmod"
    echo "jlink 生成 runtime …"
    TEMP_IMAGE="$CONFIG_DIR/images/.ohos-runtime.$$"
    rm -rf "$TEMP_IMAGE"
    "$JLINK" --module-path "$JMODS_DIR" --add-modules java.base \
      --strip-debug --no-header-files --no-man-pages --output "$TEMP_IMAGE"
    rm -rf "$RUNTIME_IMAGE"
    mv "$TEMP_IMAGE" "$RUNTIME_IMAGE"
    TEMP_IMAGE=""
    verify_artifact ;;
  *) echo "未知参数：$1" >&2; exit 2 ;;
esac
