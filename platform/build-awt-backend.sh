#!/usr/bin/env bash
# 编译 OHOS AWT 最小后端为 ohos-awt.jar
# 说明：项目路径含中文会让 javac 的模块解析失败，因此把源码复制到 /tmp 编译再回拷。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BOOT_JDK="${BOOT_JDK:-$HOME/Downloads/jdk/jdk17/Contents/Home}"
SRC=/tmp/awt-backend-src
OUT=/tmp/awt-backend-out
JAR="$ROOT/app/entry/src/main/resources/rawfile/ohos-awt.jar"

rm -rf "$SRC" "$OUT"
mkdir -p "$SRC" "$OUT"
cp -R "$ROOT/platform/java/." "$SRC/"

cd "$SRC"
"$BOOT_JDK/bin/javac" -nowarn \
  --module-path "$BOOT_JDK/jmods" --add-modules java.desktop \
  --add-exports java.desktop/java.awt.peer=ALL-UNNAMED \
  --add-exports java.desktop/sun.awt=ALL-UNNAMED \
  --add-exports java.desktop/sun.java2d.cmm=ALL-UNNAMED \
  -d "$OUT" $(find . -name '*.java')
( cd "$OUT" && "$BOOT_JDK/bin/jar" cf "$JAR" . )
echo "built $JAR ($(du -h "$JAR" | cut -f1))"
