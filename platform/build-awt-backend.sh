#!/usr/bin/env bash
# 编译 OHOS AWT 最小后端为 ohos-awt.jar
# 说明：模块模式 + --add-exports 才能引用 java.awt.peer；
#       输出目录用英文路径（中文路径下 javac 的模块解析会失败）。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BOOT_JDK="${BOOT_JDK:-$HOME/Downloads/jdk/jdk17/Contents/Home}"
OUT=/tmp/awt-backend-build
JAR="$ROOT/app/entry/src/main/resources/rawfile/ohos-awt.jar"
cd "$ROOT"

rm -rf "$OUT" && mkdir -p "$OUT"
"$BOOT_JDK/bin/javac" -nowarn \
  --module-path "$BOOT_JDK/jmods" --add-modules java.desktop \
  --add-exports java.desktop/java.awt.peer=ALL-UNNAMED \
  --add-exports java.desktop/sun.awt=ALL-UNNAMED \
  --add-exports java.desktop/sun.java2d.cmm=ALL-UNNAMED \
  -d "$OUT" $(find platform/java -name '*.java')
( cd "$OUT" && "$BOOT_JDK/bin/jar" cf "$JAR" . )
echo "built $JAR ($(du -h "$JAR" | cut -f1))"
