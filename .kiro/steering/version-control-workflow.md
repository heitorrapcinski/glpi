---
inclusion: always
---

# Version Control Workflow

Every spec (feature or bugfix) must follow a strict Git workflow from branch creation to tagging.

## Branch Naming

Create a branch at the start of every new spec using the spec identifier:

```
feature-{year}.{sequential}.[{version}]-{short-description}
bugfix-{year}.{sequential}.[{version}]-{short-description}
```

### Examples

```
feature-2026.000001.[1.0.0-SNAPSHOT]-user-authentication
bugfix-2026.000002.[1.0.1-SNAPSHOT]-fix-login-crash
```

## Workflow Steps

### 1. Start of Spec — Create Branch

Before any implementation begins, create and checkout the branch:

```powershell
# Windows (PowerShell)
git checkout -b feature-2026.000001.[1.0.0-SNAPSHOT]-short-description

# Linux/macOS (Bash)
git checkout -b feature-2026.000001.[1.0.0-SNAPSHOT]-short-description
```

### 2. During Implementation — Normal commits per task

Commit after each task completion using the task ID as reference:

```
2026.000001-0001: <short description of what was done>
```

### 3. Final Task in tasks.md — Version Control Task

The **last task** of every `tasks.md` must always be a version control task with the following sub-tasks:

```markdown
- [ ] {year}.{sequential}-XXXX - Version control and release
  - [ ] Ensure all previous tasks are complete and tests pass
  - [ ] Remove SNAPSHOT suffix from all version references in the codebase
  - [ ] Commit the version bump: "release: {version} - {short-description}"
  - [ ] Merge branch into main/master
  - [ ] Apply Git tag: {version} (without SNAPSHOT)
  - [ ] Push branch, merge, and tag to remote
```

### 4. Version Bump — Remove SNAPSHOT

Before merging, find and replace all occurrences of the version string in the codebase:

- `1.0.0-SNAPSHOT` → `1.0.0`
- `1.1.0-SNAPSHOT` → `1.1.0`
- `1.0.1-SNAPSHOT` → `1.0.1`

Common files to check: `composer.json`, `package.json`, `pom.xml`, `build.gradle`, `version.php`, `VERSION`, etc.

### 5. Merge and Tag

```powershell
# Windows (PowerShell)
git checkout main
git merge --no-ff feature-2026.000001.[1.0.0-SNAPSHOT]-short-description
git tag -a "1.0.0" -m "release: 1.0.0 - short-description"
git push origin main --tags

# Linux/macOS (Bash)
git checkout main
git merge --no-ff feature-2026.000001.[1.0.0-SNAPSHOT]-short-description
git tag -a "1.0.0" -m "release: 1.0.0 - short-description"
git push origin main --tags
```

## Rules

- **Never implement directly on main/master** — always use a feature or bugfix branch
- **Branch must be created as the very first action** before any code change
- **SNAPSHOT must be removed** from all version references before merging
- **Tag must match the final version** (without SNAPSHOT suffix)
- **The version control task is mandatory** and must be the last task in every `tasks.md`
