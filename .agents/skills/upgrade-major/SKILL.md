---
name: upgrade-major
description: Inventory or migrate one major dependency or tightly coupled toolchain in Theme Fusion Tactics. Use when asked to list major updates, upgrade a named Maven or npm dependency across a major version, update a major Docker image or GitHub Action, or perform an AI-assisted breaking dependency migration.
---

# Upgrade One Major Dependency

Follow `AGENTS.md`, especially Manual Dependency Maintenance. Handle exactly one named dependency or one inseparable toolchain family at a time.

## Select the Upgrade

- Before selecting or confirming any major update, **must** run the pinned Renovate Docker lookup from `AGENTS.md`. Do not substitute native Maven/npm inventory commands or manual manifest inspection. If Docker reports daemon permission denied, retry the same command with the required approval; if that still fails, stop and report the blocker. If the user does not name a dependency, present only the Renovate-detected major updates with current and target versions, and ask which single upgrade to perform. Do not edit files yet.
- If the user names a dependency, confirm its current version and latest stable major version from authoritative sources.
- Treat tightly coupled packages such as TypeScript, typescript-eslint, vue-tsc, and Vue ESLint configuration as one toolchain only when compatibility requires it. Do not add unrelated majors.

## Migration Workflow

1. Run on the branch currently checked out. Do not reset, stash, clean, or switch branches. Preserve unrelated working-tree changes.
2. Read the official release notes, migration guide, runtime requirements, peer ranges, and known breaking changes before editing.
3. Determine whether a compatible stable dependency graph exists. If it does not, make no upgrade changes and report the exact upstream constraint.
4. Upgrade the selected dependency or toolchain using its native package manager. Regenerate affected lockfiles; never hand-edit them.
5. Apply required source, configuration, build, CI, Docker, and test migrations. Do not use `--force` or `--legacy-peer-deps`.
6. Update `frontend/src/components/Changelog.vue` only after the selected dependency is actually upgraded, recording the version transition and meaningful migration effects. If no upgrade is made, do not modify the changelog.
7. Run the complete relevant validation:
   - Backend: `mvn spotless:apply` followed by `mvn -B verify`, using the normal `~/.m2` cache and requesting the required permission if sandbox access is denied.
   - Frontend: `npm test`, `npm run lint`, and `npm run build`.
   - Docker/Compose or GitHub Actions: perform the applicable syntax and local validation.
8. Fix migration-related failures without expanding to another unrelated major dependency.
9. Present old and new versions, breaking changes handled, source changes, remaining risks, and validation results. Leave changes uncommitted unless the user explicitly requests a commit.
