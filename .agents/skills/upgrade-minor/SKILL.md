---
name: upgrade-minor
description: Apply and validate all compatible non-major dependency updates in Theme Fusion Tactics. Use for routine dependency maintenance, minor or patch upgrades, lockfile refreshes, Docker or GitHub Actions digest updates, or requests such as "upgrade routine dependencies" and "update everything except majors".
---

# Upgrade Minor Dependencies

Follow `AGENTS.md`, especially Manual Dependency Maintenance. Treat "minor" in the skill name as shorthand for every compatible non-major update: patch, minor, pin, digest, and lockfile maintenance.

## Workflow

1. Run on the branch currently checked out. Do not reset, stash, clean, or switch branches. Preserve unrelated working-tree changes.
2. Before inspecting or changing any dependency, **must** run the pinned Renovate Docker lookup from `AGENTS.md` to inventory Maven, npm, Docker/Compose, and GitHub Actions updates. Do not substitute `npm outdated`, Maven Versions Plugin output, or manual manifest inspection. If Docker reports daemon permission denied, retry the same command with the required approval; if that still fails, stop and report the blocker. Use native npm/Maven commands only to apply versions identified by Renovate.
3. Exclude every major, replacement, and peer-incompatible update. Summarize exclusions before applying changes.
4. Check authoritative release notes for unusual behavior, runtime, or configuration changes.
5. Apply all compatible non-major updates:
   - Use npm commands with the correct dependency type and exact-version behavior so `package.json` and `package-lock.json` remain synchronized.
   - Edit Maven versions explicitly and run Spotless after backend changes.
   - Preserve complete commit-SHA pinning and readable version comments for GitHub Actions.
   - Verify Docker tags and digests against official upstream sources.
6. Never use `--force`, `--legacy-peer-deps`, or hand-edited npm lockfiles. Leave incompatible packages unchanged and report why.
7. Update `frontend/src/components/Changelog.vue` with the completed maintenance work.
8. Run `mvn spotless:apply` and `mvn -B verify` from `backend` when Maven changed. Use the normal `~/.m2` cache and request the required permission if sandbox access is denied. If npm registry access is denied, retry npm with the required network approval. Run `npm test`, `npm run lint`, and `npm run build` from `frontend`. Run relevant Docker/Compose validation when container files changed.
9. Fix failures caused by the updates while staying within non-major scope. If safe resolution requires a major upgrade, revert only that dependency's attempted changes and report the blocker.
10. Present the changed versions, exclusions, release-note risks, and validation results. Leave changes uncommitted unless the user explicitly requests a commit.
