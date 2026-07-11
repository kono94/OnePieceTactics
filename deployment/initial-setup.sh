#!/bin/bash
# =============================================================================
# TFT Server First-Time Setup
# =============================================================================
# Run this ONCE after SSHing into your new VPS:
#   bash /opt/tft/deployment/initial-setup.sh
# =============================================================================

set -e

# Check for root/sudo
if [ "$EUID" -ne 0 ]; then
    echo "❌ This script must be run as root or with sudo"
    echo "   Usage: sudo bash /opt/tft/deployment/initial-setup.sh"
    exit 1
fi

PROJECT_DIR="/opt/tft"
cd "$PROJECT_DIR"

echo ""
echo "╔════════════════════════════════════════╗"
echo "║       TFT Server Setup Wizard          ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Check if already initialized
if [ -f ".env" ] && [ -d "deployment/certbot/conf/live" ]; then
    echo "⚠️  Server appears to be already initialized."
    read -p "Re-run setup? (y/N): " confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 0
    fi
fi

# -----------------------------------------------------------------------------
# Step 1: Domain
# -----------------------------------------------------------------------------
echo "Step 1/4: Domain"
echo "─────────────────────────────────────────"
read -p "Enter your domain (e.g., tft.example.com): " DOMAIN

if [ -z "$DOMAIN" ]; then
    echo "❌ Domain is required!"
    exit 1
fi

# -----------------------------------------------------------------------------
# Step 2: Email for SSL
# -----------------------------------------------------------------------------
echo ""
echo "Step 2/4: SSL Certificate"
echo "─────────────────────────────────────────"
read -p "Email for Let's Encrypt notices (optional): " EMAIL

if [ -z "$EMAIL" ]; then
    EMAIL="admin@$DOMAIN"
    echo "Using: $EMAIL"
fi

# -----------------------------------------------------------------------------
# Step 3: Game Mode
# -----------------------------------------------------------------------------
echo ""
echo "Step 3/4: Game Mode"
echo "─────────────────────────────────────────"
echo "Available: onepiece, pokemon"
read -p "Game mode [onepiece]: " GAME_MODE
GAME_MODE=${GAME_MODE:-onepiece}

# -----------------------------------------------------------------------------
# Step 4: Analytics admin password
# -----------------------------------------------------------------------------
echo ""
echo "Step 4/4: Analytics Admin"
echo "─────────────────────────────────────────"
read -r -s -p "Admin password (6+ letters, digits, dots, underscores, or hyphens): " ANALYTICS_ADMIN_PASSWORD
echo ""
if [[ ! "$ANALYTICS_ADMIN_PASSWORD" =~ ^[A-Za-z0-9._-]{6,}$ ]]; then
    echo "❌ Use at least 6 letters, digits, dots, underscores, or hyphens."
    exit 1
fi

mkdir -p /var/lib/one-piece-tactics/analytics
chmod 0700 /var/lib/one-piece-tactics/analytics

# -----------------------------------------------------------------------------
# Create .env
# -----------------------------------------------------------------------------
echo ""
echo "Creating .env..."
printf "DOMAIN=%s\nGAME_MODE=%s\nSPRING_PROFILES_ACTIVE=prod\nANALYTICS_ADMIN_PASSWORD=%s\n" \
    "$DOMAIN" "$GAME_MODE" "$ANALYTICS_ADMIN_PASSWORD" > .env
chown root:docker .env
chmod 0640 .env
echo "✓ .env created"

# -----------------------------------------------------------------------------
# Generate nginx prod config
# -----------------------------------------------------------------------------
echo "Generating nginx config..."
export DOMAIN
envsubst '${DOMAIN}' < deployment/nginx/prod.conf.template > deployment/nginx/prod.conf
echo "✓ nginx/prod.conf generated"

# -----------------------------------------------------------------------------
# SSL Certificate
# -----------------------------------------------------------------------------
echo ""
echo "─────────────────────────────────────────"
echo "SSL Certificate for: $DOMAIN"
echo "─────────────────────────────────────────"
echo ""
echo "Make sure your DNS A record points to this server!"
echo ""
read -p "Press Enter when DNS is ready (or Ctrl+C to abort)..."

# Start ACME-only nginx
echo ""
echo "Starting nginx for ACME challenge..."
docker compose -f docker-compose.acme.yml up -d nginx
sleep 3

# Get certificate
echo "Requesting certificate from Let's Encrypt..."
docker compose -f docker-compose.acme.yml run --rm certbot certonly \
    --webroot \
    --webroot-path /var/www/certbot \
    -d "$DOMAIN" \
    --email "$EMAIL" \
    --agree-tos \
    --non-interactive

# Stop ACME nginx
docker compose -f docker-compose.acme.yml down

# -----------------------------------------------------------------------------
# Certificate Reload Cron
# -----------------------------------------------------------------------------
echo "Installing nginx certificate reload cron..."
cat > /etc/cron.d/tft-nginx-cert-reload << 'EOF'
SHELL=/bin/sh
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
17 */6 * * * root docker exec tft-nginx nginx -s reload >/dev/null 2>&1 || true
EOF
chmod 0644 /etc/cron.d/tft-nginx-cert-reload
echo "✓ nginx certificate reload cron installed"

# -----------------------------------------------------------------------------
# Done!
# -----------------------------------------------------------------------------
echo ""
echo "╔════════════════════════════════════════╗"
echo "║           Setup Complete! ✓            ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "To start the app now:"
echo ""
echo "  docker compose --profile prod up -d --build"
echo ""
echo "Or push a git tag to trigger GitOps deployment!"
echo ""
echo "Your app will be at: https://$DOMAIN"
echo "Admin analytics: https://$DOMAIN/#/admin/analytics"
echo ""
