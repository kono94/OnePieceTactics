#!/bin/bash
# =============================================================================
# TFT Deploy Script
# =============================================================================
# Called by GitOps trigger after git checkout
# =============================================================================

set -e

PROJECT_DIR="/opt/tft"
cd "$PROJECT_DIR"

echo "[$(date)] Deploying TFT..."

# Check if initialized
if [ ! -f ".env" ]; then
    echo "❌ Server not initialized! Run: bash deployment/init.sh"
    exit 1
fi

# Load environment
export $(grep -v '^#' .env | xargs)
echo "[$(date)] Domain: $DOMAIN"

# Generate nginx prod config
echo "[$(date)] Generating nginx config..."
envsubst '${DOMAIN}' < deployment/nginx/prod.conf.template > deployment/nginx/prod.conf

# Deploy with prod profile
echo "[$(date)] Starting containers..."
docker compose --profile prod up -d --build

# Reload nginx (for cert renewals)
docker exec tft-nginx nginx -s reload 2>/dev/null || true

echo "[$(date)] ✅ Deployed to https://$DOMAIN"
