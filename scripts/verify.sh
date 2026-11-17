#!/usr/bin/env bash
# REW 鸿蒙 PC 适配 —— 一键校验
#   1) 补丁哈希一致性  2) 运行时依赖清单核查  3) 构建入口存在性
#   4) 可选 --device：设备侧冒烟（走 portkit 设备锁，单模拟器串行）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "[1/4] 补丁哈希（source.lock.json）"
python3 - <<'PY'
import hashlib, json, sys, os
if not os.path.exists('source.lock.json'):
    print('  无 source.lock.json，跳过'); raise SystemExit(0)
lock = json.load(open('source.lock.json'))
bad = [r['file'] for r in lock['patches']
       if not os.path.exists(r['file'])
       or hashlib.sha256(open(r['file'],'rb').read()).hexdigest() != r['sha256']]
print('  补丁 %d 份，异常 %d 份' % (len(lock['patches']), len(bad)))
if bad:
    print('  异常：' + ', '.join(bad[:5])); sys.exit(1)
PY

echo "[2/4] 运行时依赖清单"
python3 - <<'PY'
import json, os
p = 'jvm/runtime-manifest.json'
if not os.path.exists(p):
    print('  还没有 jvm/runtime-manifest.json（P1 产出）')
else:
    m = json.load(open(p))
    print('  JDK: %s  目标: %s  文件数: %d' % (m.get('jdk'), m.get('target'), len(m.get('files', []))))
PY

echo "[3/4] 构建入口"
for f in jvm/build-openjdk.sh platform/build-platform.sh app/build-profile.json5; do
  if [ -e "$f" ]; then echo "  有 ${f}"; else echo "  缺 ${f}（对应阶段尚未产出）"; fi
done

if [ "${1:-}" != "--device" ]; then
  echo "[4/4] 跳过设备侧冒烟（加 --device 开启）"
  exit 0
fi

echo "[4/4] 设备侧冒烟"
PORTKIT="$ROOT/../tools/portkit/portkit.py"
if [ -f "$PORTKIT" ]; then
  python3 "$PORTKIT" device exec --project ohos_REW -- bash scripts/device-smoke.sh
else
  echo "  未找到 portkit，直接执行"
  bash scripts/device-smoke.sh
fi
