---
name: commit-and-push
description: Commit code changes and push via Git. Use when the user asks to commit, push, or save their work to the repository.
argument-hint: commit message or description of changes
---

# Commit and Push

You are committing and pushing code changes for Guileless Bopomofo.

## Commit Message Convention

- **Subject line**: imperative mood, concise summary of the change (e.g. "Fix version() returning null", "Add unit tests for pure Kotlin logic codes.")
- **Body** (optional): explain *why* the change was made, not just what. Include context when the change is non-trivial.
- **Signed-off-by trailer**: all commits use `--signoff` to add the `Signed-off-by` trailer automatically.
- No conventional commits prefix (no `feat:`, `fix:`, etc.) — this project uses plain English subject lines.

### Subject Line Patterns

Follow the existing style:
- `Add ...` — new feature or file
- `Fix ...` — bug fix
- `Update ...` — dependency or content update
- `Remove ...` — deletion of code or files
- `Refactor: ...` — code restructuring
- `Style: ...` — formatting or cosmetic changes
- `Upgrade ...` — dependency version upgrades
- `Release X.Y.Z` — release commits (handled by `/release-engineering`)

## Workflow

1. **Review changes** — run `git status` and `git diff` to understand what will be committed.
2. **Stage files** — add specific files by name rather than `git add -A`. Be careful not to stage:
   - Sensitive files (`.env`, credentials, signing keys)
   - Build artifacts (`app/build/`, `app/.cxx/`)
   - Chewing data files in assets (these are generated during build)
3. **Commit** — use `--signoff` flag. Pass the message via HEREDOC for proper formatting:
   ```bash
   git commit --signoff -m "$(cat <<'EOF'
   Subject line here

   Optional body explaining why.

   Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>
   EOF
   )"
   ```
4. **Push** — always confirm with the user before pushing. This project has two remotes:
   - `origin` — GitHub (git@github.com:hiroshiyui/GuilelessBopomofo.git)
   - `gitlab` — GitLab mirror (git@gitlab.com:hiroshiyui/GuilelessBopomofo.git)

   By default, push to `origin`. Push to `gitlab` only if the user requests it.

## Branch Conventions

- `master` — main/stable branch
- `current` — active development branch
- Feature branches use descriptive names (e.g. `issue-65-implement-all-physical-keyboard-layouts`)

Never force-push to `master` or `current` without explicit user approval.

## Task: $ARGUMENTS
