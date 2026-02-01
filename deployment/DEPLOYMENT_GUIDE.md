# Hardened Cloud-Init & Consolidated GitOps

This setup consolidates all deployment files into the `/deployment` folder and follows the **KISS** principle.

## 1. Local SSH Key Generation

Run these commands on your **local machine** to prepare the keys for [cloud-init.yaml](cloud-init.yaml).

### A. Admin Key (For you to log in)
Create key:
```bash
ssh-keygen -t ed25519 -f ~/.ssh/id_onepiece_admin
```
- **Public Key**: `cat ~/.ssh/id_onepiece_admin.pub` -> Paste into `YOUR_ADMIN_SSH_KEY_HERE`.


### B. Trigger Key (For GitHub Actions)
This key is used *only* by GitHub to trigger the server update.
```bash
ssh-keygen -t ed25519 -f ~/.ssh/id_onepiece_trigger
```
- **Public Key**: `cat ~/.ssh/id_onepiece_trigger.pub` -> Paste into `YOUR_GITHUB_ACTION_SSH_KEY_HERE`.
- **Private Key**: `cat ~/.ssh/id_onepiece_trigger` -> Paste into **GitHub Secrets** as `SSH_PRIVATE_KEY`.


---

## 2. Key Mapping & Purpose

| Key | User on Server | Port | Purpose |
| :--- | :--- | :--- | :--- |
| **Admin Key** | `deployer` | 2222 | Full admin access (Sudo). |
| **Trigger Key** | `github-deployer` | 2222 | **Restricted**: Can ONLY trigger deployment. |




> [!NOTE]
> **Root Access**: The `root` user is disabled for SSH login. You must log in as `deployer`.





---

## 3. Deployment Flow

1. Launch your server with the edited `deployment/cloud-init.yaml`.
2. Log in using your Admin Key: `ssh -p 2222 deployer@SERVER_IP -i ~/.ssh/id_onepiece_admin`. Or edit your ssh config file and add:
```
Host hetznerluffy
  HostName <SERVER_IP>
  User deployer
  Port 2222
  IdentityFile ~/.ssh/id_onepiece_admin
  ```
3. Your code will be located at `/opt/onepiece`.
4. On Github, create a tag (e.g. 0.0.1) to trigger the deployment. The `github-deployer` user will execute the `/usr/local/bin/deploy-trigger` single command ssh login which will in return trigger the `./deploy.sh` script.


### Updating logic:
To change how the app restarts (e.g., adding environment variables), simply edit [deployment/deploy.sh](./deploy.sh) in your repo and push a new tag.

## 4. GitHub Action
Add this to `.github/workflows/deploy.yml`:

```yaml
name: Deploy
on:
  push:
    tags: [ 'v*' ]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SERVER_IP }}
          port: 2222
          username: github-deployer
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: "ignored"
```
