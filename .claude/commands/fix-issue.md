Automatically fix code based on a GitHub Issue or PR review comments.

## Input

`$ARGUMENTS` can be:
- `#42` or `42` — fix GitHub **Issue** #42
- `PR#42` or `pr42` — fix based on **PR** #42's review comments
- empty — fix based on the latest open PR's review comments

## Steps

### 1. Determine target type and number

Parse `$ARGUMENTS`:
- If starts with `PR` or `pr` → target is a **Pull Request**
- If starts with `#` or is a plain number → target is an **Issue**
- If empty → fetch latest open PR:
  ```bash
  gh pr list --state open --limit 1 --json number,title,headRefName
  ```

### 2. Get repo info

```bash
gh repo view --json nameWithOwner --jq '.nameWithOwner'
```

### 3. Fetch context based on target type

**If Issue:**
```bash
# Issue details and all comments
gh issue view {number} --json number,title,body,labels,assignees,comments,url

# Search codebase for related files (use keywords from issue title/body)
# Claude will grep for relevant symbols/filenames mentioned in the issue
```

**If PR:**
```bash
# PR metadata + branch info
gh pr view {number} --json number,title,body,headRefName,baseRefName,state,url

# Code diff (what was changed)
gh pr diff {number}

# Inline review comments
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, path:.path, line:.line, body:.body, diff_hunk:.diff_hunk}]'

# Review summaries
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '[.[] | {id:.id, user:.user.login, state:.state, body:.body}]'

# General comments
gh api repos/{owner}/{repo}/issues/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, body:.body}]'
```

### 4. Understand what needs to be fixed

Read and reason about the fetched content:
- For Issues: understand the bug/feature request, find the relevant code files
- For PR comments: list every actionable fix request explicitly

**Only fix things explicitly mentioned.** Do not refactor unrelated code.

### 5. Checkout the right branch

**If PR target:**
```bash
gh pr checkout {number}
```

**If Issue target:** stay on current branch (or ask user which branch to work on).

### 6. Read relevant files

Use the Read tool to read the full content of each file that needs to be modified. Do not guess — read first, then edit.

### 7. Apply fixes

Use the Edit tool to make precise, minimal changes. For each fix:
- Make the change
- Briefly note what was changed and why

### 8. Verify

**Backend changes:**
```bash
cd library-system/backend && export JAVA_HOME=/opt/homebrew/opt/openjdk@17 && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile -q
```

**Frontend changes:**
```bash
cd library-system/frontend && npx vue-tsc --noEmit
```

If verification fails, fix the errors before proceeding.

### 9. Commit

```bash
git add {changed files}
git commit -m "fix: {concise description}

Resolves #{number}

Changes:
- {change 1}
- {change 2}"
```

### 10. Push and notify

```bash
git push
```

Then post a comment on the Issue or PR:

```bash
# For Issue:
gh issue comment {number} --body "Fixed in {commit-hash}. Changes: ..."

# For PR:
gh pr comment {number} --body "Addressed all review comments:
- {item 1} → {what was done}
- {item 2} → {what was done}

Please re-review."
```
