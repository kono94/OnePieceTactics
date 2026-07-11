# TFT Deployment Guide

Simple GitOps deployment to Hetzner VPS.

---

## Quick Reference

| Command | What it does |
|---------|--------------|
| `docker compose --profile dev up` | Local dev (HTTP) |
| `docker compose --profile prod up -d` | Production (HTTPS) |
| `git tag 1.0.0 && git push origin 1.0.0` | Trigger GitOps deploy |

The analytics dashboard is available at `https://<your-domain>/#/admin/analytics` after production setup.

---

## Files

```
deployment/
├── cloud-init.yaml       # Paste into Hetzner when creating VPS
├── initial-setup.sh               # Run once after first SSH
├── deploy.sh             # Called by GitOps on each deploy
└── nginx/
    ├── dev.conf          # Local development (HTTP)
    ├── prod.conf.template # Production template (HTTPS)
    └── acme.conf         # SSL certificate bootstrap
```

---

## One-Time Setup

### 1. Generate SSH Keys

```bash
# Your admin key
ssh-keygen -t ed25519 -f ~/.ssh/id_tft_admin

# GitHub Actions deploy key
ssh-keygen -t ed25519 -f ~/.ssh/id_tft_deploy
```

### 2. Edit cloud-init.yaml

Replace the placeholders:
```
<YOUR_ADMIN_SSH_KEY>        → cat ~/.ssh/id_tft_admin.pub
<YOUR_GITHUB_ACTION_SSH_KEY> → cat ~/.ssh/id_tft_deploy.pub
```

### 3. Create VPS

1. Hetzner Console → Create Server
2. OS: Ubuntu 24.04
3. Paste `cloud-init.yaml` in Cloud config
4. Create and note the IP

### 4. Configure DNS

Add A record: `tft.yourdomain.com` → `<SERVER_IP>`

Wait 5-10 min for propagation.

### 5. GitHub Secrets

Repo → Settings → Secrets → Actions:

| Secret | Value |
|--------|-------|
| `SERVER_IP` | Your server IP |
| `SSH_PRIVATE_KEY` | Content of `~/.ssh/id_tft_deploy` (private key!) |

### 6. First-Time Server Setup

```bash
# SSH in (wait ~2 min for cloud-init to finish)
ssh -p 2222 deployer@<SERVER_IP> -i ~/.ssh/id_tft_admin

# Run setup wizard
bash /opt/tft/deployment/initial-setup.sh
```

The wizard requires an admin dashboard password of at least 6 letters, digits, dots, underscores, or hyphens and
creates the persistent SQLite data directory at `/var/lib/one-piece-tactics/analytics`.

### Upgrade an existing server

Servers initialized before analytics was added need a one-time configuration update before the next deployment:

```bash
sudo mkdir -p /var/lib/one-piece-tactics/analytics
sudo chmod 0700 /var/lib/one-piece-tactics/analytics
```

Add the password to `/opt/tft/.env`:

```dotenv
SPRING_PROFILES_ACTIVE=prod
ANALYTICS_ADMIN_PASSWORD=choose-a-strong-password
```

---

## Deploy Updates

Push a tag:
```bash
git tag 1.0.0
git push origin 1.0.0
```

---

## Troubleshooting

### Access the analytics dashboard

Open `https://<your-domain>/#/admin/analytics` and enter the password stored as
`ANALYTICS_ADMIN_PASSWORD` in `/opt/tft/.env`. Successful login creates an eight-hour bearer session in that browser
tab. Logging out, closing the tab, restarting the backend, or allowing the session to expire requires another login.

To change the password, edit `/opt/tft/.env` and recreate the backend container:

```bash
docker compose --profile prod up -d --force-recreate backend
```

### GitHub deploy fails with `insufficient permission for adding an object`

The forced deploy user must own the repository metadata under `/opt/tft/.git`. This
can break if Git commands are run manually with `sudo`.

```bash
ssh -p 2222 deployer@<SERVER_IP> -i ~/.ssh/id_tft_admin
sudo chown -R github-deployer:github-deployer /opt/tft/.git
```

### Browser sees an expired certificate after certbot renewed it

Nginx needs to reload after certbot writes a renewed certificate.

```bash
ssh -p 2222 deployer@<SERVER_IP> -i ~/.ssh/id_tft_admin
sudo docker exec tft-nginx nginx -s reload
```

---

## SSH Config (Recommended)

Add to `~/.ssh/config`:
```
Host tft
  HostName <SERVER_IP>
  User deployer
  Port 2222
  IdentityFile ~/.ssh/id_tft_admin
```

Then: `ssh tft`
