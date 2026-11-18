#!/usr/bin/env bash
# 用 OpenHarmony SDK 自带的调试证书给 HAP 签名（无需华为账号）
# 用法：sign-hap.sh <unsigned.hap> [output.hap]
set -euo pipefail
SDK="${DEVECO_SDK:-${DEVECO_HOME:-/Applications/DevEco-Studio.app/Contents}/sdk/default/openharmony}"
LIB="$SDK/toolchains/lib"
JAVA="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null)}/bin/java"
IN="${1:?用法: sign-hap.sh <unsigned.hap> [output.hap]}"
OUT="${2:-${IN%.hap}-signed.hap}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "[1/2] 生成调试 profile"
"$JAVA" -jar "$LIB/hap-sign-tool.jar" sign-profile \
  -keyAlias "openharmony application profile debug" \
  -signAlg SHA256withECDSA -mode localSign \
  -profileCertFile "$LIB/OpenHarmonyProfileDebug.pem" \
  -inFile "$LIB/UnsgnedDebugProfileTemplate.json" \
  -keystoreFile "$LIB/OpenHarmony.p12" \
  -outFile "$WORK/debug.p7b" \
  -keyPwd 123456 -keystorePwd 123456 > /dev/null

echo "[2/2] 签名 HAP"
"$JAVA" -jar "$LIB/hap-sign-tool.jar" sign-app \
  -keyAlias "openharmony application profile debug" \
  -signAlg SHA256withECDSA -mode localSign \
  -appCertFile "$LIB/OpenHarmonyProfileDebug.pem" \
  -profileFile "$WORK/debug.p7b" \
  -inFile "$IN" -outFile "$OUT" \
  -keystoreFile "$LIB/OpenHarmony.p12" \
  -keyPwd 123456 -keystorePwd 123456

echo "已签名: $OUT ($(du -h "$OUT" | cut -f1))"
