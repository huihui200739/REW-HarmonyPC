#!/usr/bin/env bash
# 编译 OHOS AWT 最小后端为 ohos-awt.jar
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BOOT_JDK="${BOOT_JDK:-$HOME/Downloads/jdk/jdk17/Contents/Home}"
SRC="$ROOT/platform/java"
OUT="$ROOT/build/awt-backend"
JAR="$ROOT/app/entry/src/main/resources/rawfile/ohos-awt.jar"

rm -rf "$OUT" && mkdir -p "$OUT"
"$BOOT_JDK/bin/javac" --release 17 -nowarn -d "$OUT" $(find "$SRC" -name '*.java')
mkdir -p "$(dirname "$JAR")"
( cd "$OUT" && "$BOOT_JDK/bin/jar" cf "$JAR" . )
echo "built $JAR ($(du -h "$JAR" | cut -f1))"
