#!/usr/bin/env bash
set -euo pipefail

# Build script for AndroidLibXrayLite (libv2ray.aar)
# Workaround: patches wlynxg/anet for Go 1.26+ compatibility
# (net.zoneCache was removed in Go 1.23+)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
AAR_OUTPUT="$PROJECT_DIR/V2rayNG/app/libs/libv2ray.aar"

echo "=== Building AndroidLibXrayLite AAR ==="

cd "$PROJECT_DIR/AndroidLibXrayLite"

# Apply anet patch via go.mod replace directive
ANET_VENDOR="$PROJECT_DIR/vendor/anet-fixed"
if [ -d "$ANET_VENDOR" ]; then
    # Check if replace already exists
    if ! grep -q "replace github.com/wlynxg/anet" go.mod; then
        echo "Adding replace directive for wlynxg/anet -> $ANET_VENDOR"
        cat >> go.mod << EOF

replace github.com/wlynxg/anet => $ANET_VENDOR
EOF
    fi
else
    echo "ERROR: patched anet vendor not found at $ANET_VENDOR"
    exit 1
fi

# Build AAR
echo "Running gomobile bind..."
gomobile bind -v -androidapi 21 -ldflags='-s -w' \
    -target=android/arm64 \
    -o "$AAR_OUTPUT" ./

echo "=== AAR built successfully: $AAR_OUTPUT ==="
