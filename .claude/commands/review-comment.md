Review and respond to comments on a GitHub Pull Request.

## Input

`$ARGUMENTS` is the PR number (e.g. `#42` or `42`). If empty, fetch the latest open PR automatically.

## Steps

### 1. Resolve PR number

If `$ARGUMENTS` is empty or not provided:
```bash
gh pr list --state open --limit 1 --json number,title,headRefName
```
Use the returned number. If no open PR exists, tell the user and stop.

If `$ARGUMENTS` is provided, strip the leading `#` and use that number.

### 2. Get repo owner/name

```bash
gh repo view --json nameWithOwner --jq '.nameWithOwner'
```

### 3. Fetch all PR context in parallel

Run these commands and collect all output before analyzing:

```bash
# PR metadata
gh pr view {number} --json number,title,body,author,headRefName,baseRefName,additions,deletions,state,url

# Full code diff
gh pr diff {number}

# General conversation comments
gh api repos/{owner}/{repo}/issues/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, body:.body, created_at:.created_at}]'

# Inline review comments (file + line specific)
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, path:.path, line:.line, body:.body, diff_hunk:.diff_hunk, in_reply_to_id:.in_reply_to_id}]'

# Review summaries (APPROVED / CHANGES_REQUESTED / COMMENTED)
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '[.[] | {id:.id, user:.user.login, state:.state, body:.body, submitted_at:.submitted_at}]'
```

### 4. Analyze

For each comment and review:
- Understand exactly what the reviewer is asking
- Locate the relevant code in the diff or by reading the file
- Determine whether the concern is: **must fix** / **suggestion** / **question** / **already resolved**

Group findings into a clear report:

```
## PR #N Review Analysis: {title}

### CHANGES REQUESTED
- [File:Line] {issue} → {suggested fix}

### SUGGESTIONS
- [File:Line] {issue} → {recommended approach}

### QUESTIONS (need clarification)
- {question from reviewer}

### ALREADY RESOLVED
- {items that were raised but are already addressed in the diff}
```

### 5. Ask user what to do next

After showing the analysis, ask:

> Choose an action:
> 1. Apply all **CHANGES REQUESTED** fixes automatically (runs `/fix-issue`)
> 2. Post this analysis as a PR comment
> 3. Show me the specific code changes needed without applying them
> 4. Do nothing

Execute the chosen action:
- **Option 2**: `gh pr comment {number} --body "{analysis}"`
