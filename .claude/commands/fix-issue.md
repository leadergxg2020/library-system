Fix code based on a GitHub Issue description or PR review comments, grounded in the project's requirements and design decisions.

## Input

`$ARGUMENTS` can be:
- `#42` or `42`    — fix GitHub **Issue** #42
- `PR#42` or `pr42` — fix based on **PR** #42's review comments
- empty             — fix based on the latest open PR's review comments

---

## Steps

### 1. Load project context FIRST (mandatory before writing any code)

Read these documents before touching any code. They define the constraints that all fixes must respect.

```
docs/PROJECT_CHARTER.md       — project goals and explicit out-of-scope items
docs/PRD.md                   — feature requirements and acceptance criteria
architecture/ARCH.md          — tech stack, ADRs, and architectural rules
architecture/schema.sql       — canonical DB schema; any schema change must match this
architecture/api-contract.md  — all API contracts; response shapes must match exactly
architecture/coding-standards.md — layer rules, naming conventions, forbidden patterns
```

Key rules to internalize (from the ADRs in ARCH.md):
- **No XML mappers** — all MyBatis-Plus queries use annotations or BaseMapper built-ins
- **No Spring Security** — no auth layer in this iteration
- **No Redis / cache** — all queries go directly to SQLite
- **Service layer** — concrete `@Service` classes only, no interface+impl pattern
- **No axios in components** — all API calls go through `src/api/`
- **available_quantity / current_borrow_count** — stored fields, maintained transactionally in BorrowService
- **readerName / bookTitle in BorrowRecordPO** — denormalized, set at insert time

Also scan the current directory structure:
```bash
find library-system -type f \( -name "*.java" -o -name "*.vue" -o -name "*.ts" \) | sort
```

### 2. Determine target type and number

Parse `$ARGUMENTS`:
- Starts with `PR` or `pr` → target is a **Pull Request**
- Starts with `#` or is a plain number → target is an **Issue**
- Empty → fetch latest open PR:
  ```bash
  gh pr list --state open --limit 1 --json number,title,headRefName
  ```

### 3. Get repo info

```bash
gh repo view --json nameWithOwner --jq '.nameWithOwner'
```

### 4. Fetch context based on target type

**If Issue:**
```bash
gh issue view {number} --json number,title,body,labels,comments,url
```
From the issue content, identify keywords (class names, field names, API paths) and read the relevant source files using the Read tool.

**If PR:**
```bash
# PR metadata and branch info
gh pr view {number} --json number,title,body,headRefName,baseRefName,state,url

# Code diff — understand what was already changed
gh pr diff {number}

# Inline review comments (most actionable — have file + line)
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, path:.path, line:.line, body:.body, diff_hunk:.diff_hunk}]'

# Review summaries
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '[.[] | {id:.id, user:.user.login, state:.state, body:.body}]'

# General comments
gh api repos/{owner}/{repo}/issues/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, body:.body}]'
```

### 5. Plan the fixes

Before writing any code, list all required changes and validate each one:

For each fix item:
1. **Identify the file and location** — read the full file first
2. **Check against design docs** — does the fix comply with the API contract, schema, and coding standards?
3. **Determine minimal change** — only fix what the issue/comment requests; do not refactor adjacent code
4. **Flag conflicts** — if a comment requests something that contradicts an ADR, explain why and propose an alternative

Do not proceed to editing until the plan is clear.

### 6. Checkout the right branch

**If PR target:**
```bash
gh pr checkout {number}
```

**If Issue target:** work on the current branch, or ask the user:
```bash
git checkout -b fix/issue-{number}
```

### 7. Apply fixes using Edit tool

For each planned change:
- Read the full file with the Read tool
- Apply a precise, minimal edit with the Edit tool
- Do not touch code unrelated to the fix

### 8. Verify — do not skip this step

**Backend (Java) changes:**
```bash
cd library-system/backend \
  && export JAVA_HOME=/opt/homebrew/opt/openjdk@17 \
  && export PATH="$JAVA_HOME/bin:$PATH" \
  && mvn compile -q 2>&1
```

**Frontend (Vue/TS) changes:**
```bash
cd library-system/frontend && npx vue-tsc --noEmit 2>&1
```

If verification fails, fix all errors before committing. Do not commit broken code.

### 9. Commit with traceability

```bash
git add {changed files only — no git add .}
git commit -m "fix: {concise description}

Closes #{number}

- {change 1 and why}
- {change 2 and why}"
```

### 10. Push and notify

```bash
git push
```

Post a closing comment on the Issue or PR:

```bash
# For Issue:
gh issue comment {number} --body "Fixed in $(git rev-parse --short HEAD).

Changes made:
- {change 1}
- {change 2}

Verified: mvn compile / vue-tsc passed."

# For PR:
gh pr comment {number} --body "All review comments addressed:

| Comment | Resolution |
|---------|------------|
| @{user}: \"{excerpt}\" | {what was done} |

Verified: mvn compile / vue-tsc passed. Ready for re-review."
```

---

## Hard constraints (never violate)

- Never push directly to `main` — always work on a feature/fix branch
- Never add dependencies not already in `pom.xml` or `package.json` without asking
- Never introduce patterns the ADRs explicitly rejected (XML mappers, Spring Security, Redis, impl subpackage, etc.)
- Never make changes to files unrelated to the fix
