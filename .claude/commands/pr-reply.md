Review and respond to comments on a GitHub Pull Request, grounded in the project's requirements and design decisions.

## Input

`$ARGUMENTS` is the PR number (e.g. `#42` or `42`). If empty, fetch the latest open PR automatically.

---

## Steps

### 1. Load project context FIRST (before looking at any PR)

Read the following documents in order. These define what "correct" means for this project — use them as the ground truth when evaluating the PR.

```
docs/PROJECT_CHARTER.md       — project goals, scope, and explicit out-of-scope items
docs/PRD.md                   — product requirements and acceptance criteria
architecture/ARCH.md          — tech stack, system design, and Architecture Decision Records (ADRs)
architecture/schema.sql       — canonical database schema (tables, columns, indexes)
architecture/api-contract.md  — all API endpoint contracts (method, path, request, response)
architecture/coding-standards.md — naming conventions, layer rules, forbidden patterns
```

Also read the project directory structure to understand what files exist:
```bash
find library-system -type f \( -name "*.java" -o -name "*.vue" -o -name "*.ts" \) | sort
```

### 2. Resolve PR number

If `$ARGUMENTS` is empty:
```bash
gh pr list --state open --limit 1 --json number,title,headRefName
```
Use the returned number. If no open PR exists, tell the user and stop.

Strip any leading `#` from `$ARGUMENTS` if provided.

### 3. Get repo owner/name

```bash
gh repo view --json nameWithOwner --jq '.nameWithOwner'
```

### 4. Fetch all PR context

```bash
# PR metadata
gh pr view {number} --json number,title,body,author,headRefName,baseRefName,additions,deletions,state,url

# Full code diff
gh pr diff {number}

# General conversation comments
gh api repos/{owner}/{repo}/issues/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, body:.body, created_at:.created_at}]'

# Inline review comments (file + line)
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, path:.path, line:.line, body:.body, diff_hunk:.diff_hunk, in_reply_to_id:.in_reply_to_id}]'

# Review summaries
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '[.[] | {id:.id, user:.user.login, state:.state, body:.body, submitted_at:.submitted_at}]'
```

For each file mentioned in the diff, read its full current content using the Read tool so you understand the surrounding context.

### 5. Analyze — grounded in project context

Evaluate the PR against **three layers**:

**Layer 1 — Requirement alignment** (from PRD + PROJECT_CHARTER)
- Does the PR implement what was actually required?
- Does it add anything out of scope? (flag, don't necessarily block)
- Are all acceptance criteria for the relevant feature met?

**Layer 2 — Design compliance** (from ARCH + schema + api-contract + coding-standards)
- Does the API match the contract (path, method, request/response shape)?
- Does the schema change align with `schema.sql`?
- Are ADR decisions respected? (e.g., no XML mappers, no Redis, no Spring Security)
- Are coding standards followed? (e.g., Service layer has no impl subpackage, no interface+impl pattern)
- Are forbidden patterns absent? (e.g., direct axios calls in components, business logic in Controller)

**Layer 3 — Code quality**
- Bugs, edge cases, null handling
- Security concerns
- Performance issues (N+1, unnecessary queries)
- Readability and naming

**Layer 4 — Existing reviewer comments**
- Categorize each comment: Must Fix / Suggestion / Question / Already Resolved
- Note if any comment conflicts with an ADR or project decision (important to flag)

### 6. Output structured report

```
## PR #{number} Review: {title}

### Context Check
- Requirement: {matches PRD section X / deviates because...}
- Design compliance: {compliant / violates ADR-N because...}

### MUST FIX (blocks merge)
- [{file}:{line}] {issue} — conflicts with {PRD/ARCH/coding-standard reference}
  Suggested fix: ...

### SUGGESTIONS (optional improvements)
- [{file}:{line}] {issue}
  Suggested fix: ...

### REVIEWER COMMENTS STATUS
- Comment by @{user}: "{excerpt}" → {Must Fix / Suggestion / Already Resolved / Needs Discussion}

### VERDICT
{APPROVE / REQUEST CHANGES / NEEDS DISCUSSION}
Reason: ...
```

### 7. Ask the user what to do next

> Choose an action:
> 1. Apply all **MUST FIX** items automatically → runs `/fix-issue PR#{number}`
> 2. Post this review as a PR comment
> 3. Show detailed code suggestions only
> 4. Do nothing

For option 2:
```bash
gh pr comment {number} --body "{report}"
```
