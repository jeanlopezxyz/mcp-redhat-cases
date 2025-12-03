# Contributing to Red Hat Cases MCP Server

Thank you for your interest in contributing! This document explains our workflow and guidelines.

## Quick Reference

```
All changes go to main via Pull Requests, never direct push.

feature/x ──PR──┐
                ↓
fix/y ────PR────┼───→ main ───→ v1.x.x (tag) ───→ npm + GitHub Release
                ↑
docs/z ───PR────┘
```

## Branch Protection

The `main` branch is protected. Direct pushes are **not allowed**.

| Rule | Setting |
|------|---------|
| Required approving reviews | 1 |
| Dismiss stale reviews | Yes |
| Required status checks | `build` must pass |
| Require up-to-date branches | Yes |
| Allow force pushes | No |
| Allow deletions | No |

## Complete Contribution Workflow

### Step 1: Fork and Clone (First Time Only)

```bash
# Fork the repository on GitHub first, then:
git clone https://github.com/YOUR_USERNAME/mcp-redhat-cases.git
cd mcp-redhat-cases
git remote add upstream https://github.com/jeanlopezxyz/mcp-redhat-cases.git
```

### Step 2: Sync with Upstream

```bash
git checkout main
git fetch upstream
git merge upstream/main
git push origin main
```

### Step 3: Create a Feature Branch

**Important**: Always create a new branch for your changes. Never work directly on `main`.

```bash
git checkout main
git pull origin main
git checkout -b <type>/<description>
```

Branch naming conventions:
| Type | Use Case | Example |
|------|----------|---------|
| `feature/` | New functionality | `feature/add-attachment-support` |
| `fix/` | Bug fixes | `fix/case-search-pagination` |
| `docs/` | Documentation | `docs/update-installation-guide` |
| `refactor/` | Code improvements | `refactor/simplify-auth-flow` |

### Step 4: Make Your Changes

```bash
# Set up your environment
export REDHAT_TOKEN="your-token"

# Run in dev mode
./mvnw quarkus:dev

# Run tests
./mvnw test

# Build
./mvnw package -DskipTests
```

### Step 5: Commit Your Changes

Write clear, descriptive commit messages:

```bash
git add .
git commit -m "Add attachment upload support for cases"
```

Good commit message examples:
- `Add listAttachments tool for case management`
- `Fix pagination in searchCases when query is empty`
- `Update README with SSE mode documentation`

### Step 6: Push Your Branch

```bash
git push -u origin <your-branch-name>
```

Example:
```bash
git push -u origin feature/add-attachment-support
```

### Step 7: Create a Pull Request

Option A - Using GitHub CLI:
```bash
gh pr create --title "Add attachment support" --body "Description of changes"
```

Option B - Using GitHub Web:
1. Go to https://github.com/jeanlopezxyz/mcp-redhat-cases
2. Click "Compare & pull request" for your branch
3. Fill in the PR template
4. Submit the PR

### Step 8: Wait for Review

Your PR must pass these checks before merge:

```
PR Status:
├── CI (build) ............ Must pass
├── Review ................ 1 approval required
└── Up-to-date ............ Branch must be current with main
```

If CI fails:
1. Check the workflow logs
2. Fix the issues locally
3. Push new commits (this will trigger CI again)

**Note**: Pushing new commits will dismiss previous reviews, requiring re-approval.

### Step 9: Merge

Once approved and CI passes:
1. Click "Merge pull request" on GitHub
2. Delete the feature branch (optional but recommended)

### Step 10: Clean Up Locally

```bash
git checkout main
git pull origin main
git branch -d <your-branch-name>
```

## Complete Example

Here's a real example of the full workflow:

```bash
# 1. Start fresh from main
git checkout main
git pull origin main

# 2. Create feature branch
git checkout -b docs/add-contributing-guide

# 3. Make changes to files...
# (edit CONTRIBUTING.md, README.md, etc.)

# 4. Stage and commit
git add CONTRIBUTING.md README.md
git commit -m "Add CONTRIBUTING.md with collaboration guidelines"

# 5. Push to remote
git push -u origin docs/add-contributing-guide

# 6. Create PR
gh pr create --title "Add CONTRIBUTING.md" --body "Add collaboration guidelines"

# 7. Wait for CI + review...

# 8. After merge, clean up
git checkout main
git pull origin main
git branch -d docs/add-contributing-guide
```

## Development Guidelines

### Code Style

- Follow existing code patterns
- Use meaningful variable and method names
- Add JavaDoc for public methods
- Keep methods focused and small

### Testing

- Add tests for new features
- Ensure existing tests pass
- Test both stdio and SSE modes when applicable

### Project Structure

```
src/main/java/com/redhat/cases/
├── adapter/mcp/          # MCP tools and prompts
├── application/service/  # Business logic
├── domain/model/         # Domain entities
└── infrastructure/       # External integrations (Red Hat API)
```

## Release Process (Maintainers Only)

Releases are created by maintainers after PRs are merged:

```bash
# 1. Ensure main is up to date
git checkout main
git pull origin main

# 2. Create and push tag
git tag v1.x.x
git push origin v1.x.x
```

This triggers GitHub Actions to automatically:
- Build the uber-jar
- Create a GitHub Release with the JAR
- Publish to npm

### When to Create a Release

| Situation | Create Release? |
|-----------|-----------------|
| Major new feature merged | Yes |
| Several bug fixes merged | Yes |
| Documentation only changes | Optional |
| Each individual PR | Not necessarily |

## Getting Help

- Open an issue for bugs or feature requests
- Tag your issue appropriately (`bug`, `enhancement`, `question`)
- Provide reproduction steps for bugs

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
