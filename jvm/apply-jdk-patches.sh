#!/usr/bin/env bash
# 在固定的 BiSheng JDK 17 检出上应用 OHOS 补丁（幂等）
set -euo pipefail
source "$(dirname "$0")/env.sh"
source "$ROOT_DIR/jvm/jdk-source.env"

[[ -d "$JDK_SOURCE_DIR/.git" ]] || { echo "源码未检出：$JDK_SOURCE_DIR" >&2; exit 1; }
HEAD="$(git -C "$JDK_SOURCE_DIR" rev-parse HEAD)"
[[ "$HEAD" == "$JDK_LOCK_COMMIT" ]] || { echo "commit 不匹配：$HEAD" >&2; exit 1; }

PATCH_DIR="$ROOT_DIR/patches/jdk"
applied=0
for p in "$PATCH_DIR"/0*.patch; do
  name="$(basename "$p")"
  if git -C "$JDK_SOURCE_DIR" apply --check "$p" 2>/dev/null; then
    git -C "$JDK_SOURCE_DIR" apply "$p"
    echo "  应用 $name"
    applied=$((applied+1))
  elif git -C "$JDK_SOURCE_DIR" apply --reverse --check "$p" 2>/dev/null; then
    echo "  已应用 ${name}（跳过）"
  else
    echo "  无法应用 ${name}（可能与源码冲突）" >&2
    exit 1
  fi
done
echo "完成：$applied 个补丁新应用；源码 HEAD=${HEAD:0:12}"
