#!/usr/bin/env bash
# 拉取并校验 BiSheng JDK 17 源码（固定 commit）
# 用法：
#   jvm/fetch-jdk.sh                # 克隆到 build/jdk-source/bishengjdk-17
#   jvm/fetch-jdk.sh --print-lock   # 打印锁定信息
#   jvm/fetch-jdk.sh --verify-only  # 只校验已有检出
# 环境变量：JDK_SOURCE_DIR 可指定已有检出目录（避免重复下载）
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT_DIR/jvm/jdk-source.env"

SOURCE_DIR="${JDK_SOURCE_DIR:-$ROOT_DIR/build/jdk-source/bishengjdk-17}"

case "${1:-}" in
  --print-lock)
    printf 'name=%s\nurl=%s\ncommit=%s\nlicense=%s\ndirectory=%s\n' \
      "$JDK_LOCK_NAME" "$JDK_LOCK_URL" "$JDK_LOCK_COMMIT" "$JDK_LOCK_LICENSE" "$SOURCE_DIR"
    exit 0 ;;
  --verify-only) VERIFY_ONLY=1 ;;
  "") VERIFY_ONLY=0 ;;
  *) echo "未知参数: $1" >&2; exit 2 ;;
esac

verify() {
  [[ -d "$SOURCE_DIR/.git" ]] || { echo "检出不存在: $SOURCE_DIR" >&2; return 1; }
  local head
  head="$(git -C "$SOURCE_DIR" rev-parse HEAD)"
  if [[ "$head" != "$JDK_LOCK_COMMIT" ]]; then
    echo "commit 不匹配: 期望 $JDK_LOCK_COMMIT 实际 $head" >&2
    return 1
  fi
  echo "OK: $JDK_LOCK_NAME @ ${head:0:12} ($(du -sh "$SOURCE_DIR" | cut -f1))"
}

if [[ "${VERIFY_ONLY}" == "1" ]]; then
  verify
  exit $?
fi

if [[ -d "$SOURCE_DIR/.git" ]]; then
  verify && exit 0
fi

mkdir -p "$(dirname "$SOURCE_DIR")"
git init -q "$SOURCE_DIR"
git -C "$SOURCE_DIR" remote add origin "$JDK_LOCK_URL"
echo "fetching $JDK_LOCK_COMMIT ..."
GIT_TERMINAL_PROMPT=0 git -C "$SOURCE_DIR" fetch -q --depth 1 origin "$JDK_LOCK_COMMIT"
git -C "$SOURCE_DIR" checkout -q FETCH_HEAD
verify
