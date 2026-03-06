Fix code based on a GitHub Issue, grounded in the project's requirements and design decisions.

## Usage

```
/fix-issue <number>
/fix-issue 42
/fix-issue #42
```

`$ARGUMENTS` — the Issue number to fix (with or without `#`).

---

## Steps

### 1. Load project context (mandatory before writing any code)

Read these documents to understand the constraints all fixes must respect:

```
docs/PROJECT_CHARTER.md
docs/PRD.md
architecture/ARCH.md
architecture/schema.sql
architecture/api-contract.md
architecture/coding-standards.md
```

Also scan the directory structure to understand what files exist:

```bash
find library-system -type f \( -name "*.java" -o -name "*.vue" -o -name "*.ts" \) | sort
```

### 2. Parse the issue number

Strip any leading `#` from `$ARGUMENTS` to get the numeric issue number.

### 3. Get repository info

```bash
gh repo view --json nameWithOwner --jq '.nameWithOwner'
```

### 4. Fetch the issue

```bash
gh issue view $ARGUMENTS --json number,title,body,labels,comments,url
```

Read the issue carefully:
- What is the reported bug or feature request?
- Which module / file / API endpoint is involved?
- Are there reproduction steps, expected vs actual behavior, or acceptance criteria?
- Are there reviewer comments in the issue thread that clarify intent?

From the issue content, identify affected source files by keyword (class names, field names, API paths) and read them with the Read tool.

### 5. Plan the fix

Before writing any code, list every change required:

For each change:
1. **Identify the exact file and line range** — read the full file first
2. **Verify against design docs** — does the fix comply with the API contract, schema, and coding standards?
3. **Minimal change only** — fix exactly what the issue describes; do not refactor adjacent code
4. **Flag conflicts** — if the issue requests something that contradicts an ADR, explain why and propose a compliant alternative

Do not proceed to editing until the plan is clear.

### 6. Create a fix branch

```bash
git checkout -b fix/issue-$ARGUMENTS
```

If a branch for this issue already exists, check it out instead.

### 7. Apply the fix

For each planned change:
- Read the full file with the Read tool
- Apply a precise, minimal edit with the Edit tool
- Do not touch code unrelated to the fix

### 8. Verify — do not skip

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

Fix all errors before committing. Do not commit broken code.

### 9. Commit

```bash
git add <changed files only — no git add .>
git commit -m "fix: <concise description>

Closes #$ARGUMENTS

- <change 1 and why>
- <change 2 and why>"
```

### 10. Push and close the issue

```bash
git push -u origin fix/issue-$ARGUMENTS
```

Post a closing comment on the issue:

```bash
gh issue comment $ARGUMENTS --body "Fixed in $(git rev-parse --short HEAD) on branch \`fix/issue-$ARGUMENTS\`.

Changes made:
- <change 1>
- <change 2>

Verified: mvn compile / vue-tsc passed."
```

---

## Hard constraints (never violate)

- Never push directly to `main` — always work on a `fix/issue-N` branch
- Never add dependencies not already in `pom.xml` or `package.json` without asking
- Never introduce patterns the ADRs explicitly rejected (XML mappers, Spring Security, Redis, interface+impl pattern, axios in components, etc.)
- Never change files unrelated to the fix
- Never commit if compilation or type-check fails
