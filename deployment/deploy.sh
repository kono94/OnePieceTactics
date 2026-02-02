#!/bin/bash
# deployment/deploy.sh
# This script is executed by the server-side trigger after a git pull.

set -e

echo "[$(date)] Restarting OnePieceTactics with Docker Compose..."

# Go to the deployment directory if needed, or run from root
# Since cloud-init runs this from /opt/onepiece, we can use relative paths
docker compose -f docker-compose.yml -f docker-compose.prod-override.yml up -d --build

echo "[$(date)] Deployment successful."
