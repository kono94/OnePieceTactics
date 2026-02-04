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

# Deploy with prod profile
echo "[$(date)] Starting containers..."
docker compose --profile prod up -d --build

# Reload nginx (for cert renewals)
docker exec tft-nginx nginx -s reload 2>/dev/null || true

echo "[$(date)] ✅ Deployed to https://$DOMAIN"
