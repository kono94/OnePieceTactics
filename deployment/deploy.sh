#!/bin/bash
# =============================================================================
# TFT Deploy Script
# =============================================================================
# Called by GitOps trigger after git checkout
# Requires: initial-setup.sh to have been run first (creates .env and nginx/prod.conf)
# =============================================================================

set -e

PROJECT_DIR="/opt/tft"
cd "$PROJECT_DIR"

echo "[$(date)] Deploying TFT..."

# Check if initialized
if [ ! -f ".env" ]; then
    echo "❌ Server not initialized! Run: sudo bash deployment/initial-setup.sh"
    exit 1
fi

if [ ! -f "deployment/nginx/prod.conf" ]; then
    echo "❌ nginx/prod.conf missing! Run: sudo bash deployment/initial-setup.sh"
    exit 1
fi

# Load environment for logging
export $(grep -v '^#' .env | xargs)
echo "[$(date)] Domain: $DOMAIN"

# Get git version info
GIT_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "dev")
GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Deploy with prod profile
echo "[$(date)] Building with version: $GIT_TAG ($GIT_COMMIT)"
docker compose --profile prod build \
  --build-arg VITE_GIT_TAG="$GIT_TAG" \
  --build-arg VITE_GIT_COMMIT="$GIT_COMMIT" \
  --build-arg VITE_BUILD_TIME="$BUILD_TIME"

docker compose --profile prod up -d

# Reload nginx (for cert renewals)
docker exec tft-nginx nginx -s reload 2>/dev/null || true

echo "[$(date)] ✅ Deployed to https://$DOMAIN"
