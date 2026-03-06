基于项目需求和设计决策，审查并回应 GitHub Pull Request 上的评论。

## 输入参数

`$ARGUMENTS` 为 PR 编号（如 `#42` 或 `42`）。若为空，自动获取最新的开放 PR。

---

## 执行步骤

### 第一步：优先加载项目上下文（在查看 PR 之前）

按顺序读取以下文档，它们定义了本项目"正确"的标准，作为评审 PR 的基准依据：

```
docs/PROJECT_CHARTER.md          — 项目目标、范围及明确排除项
docs/PRD.md                      — 产品需求与验收标准
architecture/ARCH.md             — 技术选型、系统设计及架构决策记录（ADR）
architecture/schema.sql          — 数据库规范 Schema（表结构、字段、索引）
architecture/api-contract.md     — 所有接口契约（方法、路径、请求/响应格式）
architecture/coding-standards.md — 命名规范、分层规则、禁止模式
```

同时读取项目目录结构，了解已有文件：
```bash
find library-system -type f \( -name "*.java" -o -name "*.vue" -o -name "*.ts" \) | sort
```

### 第二步：确定 PR 编号

若 `$ARGUMENTS` 为空：
```bash
gh pr list --state open --limit 1 --json number,title,headRefName
```
使用返回的编号。若无开放 PR，告知用户并停止。

若 `$ARGUMENTS` 以 `#` 开头，去掉前缀后使用。

### 第三步：获取仓库 owner/name

```bash
gh repo view --json nameWithOwner --jq '.nameWithOwner'
```

### 第四步：获取所有 PR 上下文

```bash
# PR 基本信息
gh pr view {number} --json number,title,body,author,headRefName,baseRefName,additions,deletions,state,url

# 完整代码 diff
gh pr diff {number}

# 普通对话评论
gh api repos/{owner}/{repo}/issues/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, body:.body, created_at:.created_at}]'

# 行内 Review 评论（含文件 + 行号）
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '[.[] | {id:.id, user:.user.login, path:.path, line:.line, body:.body, diff_hunk:.diff_hunk, in_reply_to_id:.in_reply_to_id}]'

# Review 汇总
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '[.[] | {id:.id, user:.user.login, state:.state, body:.body, submitted_at:.submitted_at}]'
```

对 diff 中涉及的每个文件，使用 Read 工具读取其完整当前内容，理解上下文。

### 第五步：基于项目上下文进行分析

从以下四个维度评审 PR：

**维度一 — 需求对齐**（来源：PRD + PROJECT_CHARTER）
- PR 是否实现了真正需要的功能？
- 是否包含范围外内容？（标记，不一定阻塞）
- 相关功能的所有验收标准是否满足？

**维度二 — 设计合规**（来源：ARCH + schema + api-contract + coding-standards）
- API 是否与契约一致（路径、方法、请求/响应格式）？
- Schema 变更是否与 `schema.sql` 对齐？
- ADR 决策是否被遵守？（如：禁止 XML mapper、禁止 Redis、禁止 Spring Security）
- 编码规范是否被遵守？（如：Service 层无 impl 子包、无 interface+impl 模式）
- 禁止模式是否缺席？（如：组件中直接调用 axios、Controller 中含业务逻辑）

**维度三 — 代码质量**
- Bug、边界情况、空值处理
- 安全隐患
- 性能问题（N+1、冗余查询）
- 可读性与命名规范

**维度四 — 已有审查意见**
- 对每条评论分类：必须修复 / 建议 / 疑问 / 已解决
- 标注与 ADR 或项目决策冲突的评论（重要）

### 第六步：输出结构化审查报告

```
## PR #{number} 审查报告：{title}

### 上下文检查
- 需求对齐：{符合 PRD 第 X 节 / 偏差原因：...}
- 设计合规：{合规 / 违反 ADR-N，原因：...}

### 必须修复（阻塞合并）
- [{file}:{line}] {问题描述} — 与 {PRD/ARCH/编码规范 参考} 冲突
  修复方案（列出所有可行方案，并标注推荐项）：
  - 方案 A：{描述} — {优点} / {缺点}
  - 方案 B：{描述} — {优点} / {缺点}
  - ★ 推荐方案：{A 或 B}，原因：{简要说明}

### 建议改进（可选）
- [{file}:{line}] {问题描述}
  修复方案：
  - 方案 A：{描述} — {优点} / {缺点}
  - ★ 推荐方案：{A}，原因：{简要说明}

### 审查意见状态
- @{user} 的评论："{摘要}" → {必须修复 / 建议 / 已解决 / 需讨论}

### 最终结论
{批准合并 / 请求修改 / 需要讨论}
原因：...
```

### 第七步：若存在"必须修复"项，进入修复确认流程

如果报告中有**必须修复**项，必须执行以下流程，**不可跳过**：

**7.1 逐项展示修复方案，等待用户确认**

对每个必须修复项，输出并等待用户响应：

```
[WAITING_FOR_USER]
问题 {序号}：[{file}:{line}] {问题描述}

可选修复方案：
  A. {方案 A 描述}
  B. {方案 B 描述}
  （如有更多方案继续列出）

★ 推荐：方案 {X}
   理由：{为何推荐此方案，结合项目上下文说明}

请输入方案字母（A/B/...）确认，或输入自定义修复说明，或输入"跳过"处理下一项。
```

收到用户确认后，**立即执行**对应方案的代码修改，然后继续展示下一个问题。

**7.2 所有问题处理完毕后，汇总并请求 commit 确认**

```
[WAITING_FOR_USER]
所有修复已应用，变更汇总：

  ✓ [{file}:{line}] {问题描述} → 已按方案 {X} 修复
  ✓ [{file}:{line}] {问题描述} → 已按方案 {X} 修复
  ⊘ [{file}:{line}] {问题描述} → 已跳过

输入 "confirm" 提交并推送代码，或输入 "cancel" 放弃所有修改。
```

**7.3 收到 "confirm" 后执行 git 操作**

```bash
git add .
git commit -m "fix: PR#{number} 代码审查问题修复

{逐条列出本次修复的问题摘要}"
git push
```

提交完成后输出：
```
代码已提交并推送。
Commit：{commit hash}
修复项：{N} 个
```

**7.4 回复每条原始 PR 评论**

提交完成后，对第四步中获取的每一条行内 Review 评论，根据处理结果自动回复：

- **已修复**：说明做了什么改动，引用 commit hash
- **已跳过**：说明不修改的原因（如：符合 ADR 要求、属于建议非必须等）
- **已解决（无需改动）**：确认现有实现已满足该评论要求

```bash
# 对每条评论（使用第四步获取的 id 字段）逐条回复
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --method POST \
  --field body="{回复内容}" \
  --field in_reply_to={comment_id}
```

回复内容模板：

已修复：
```
已在 {commit hash} 中修复。

变更：{具体改动描述，与提交信息保持一致}
```

已跳过：
```
暂不修改，原因：{具体说明，如"当前实现符合 ADR-N 规定的 XX 约束"或"属于可选优化，不影响功能正确性"}
```

已解决（无需改动）：
```
已确认：现有实现满足此要求，无需改动。{可选：补充说明}
```

**7.5 Resolve 已修复的评论线程**

对每条已修复的评论，在回复后立即通过 GraphQL API 将其线程标记为 Resolved。

第一步：获取所有 Review Thread 的 GraphQL node ID（用于与第四步的 REST comment id 对应）：

```bash
gh api graphql -f query='
{
  repository(owner: "{owner}", name: "{repo}") {
    pullRequest(number: {number}) {
      reviewThreads(first: 100) {
        nodes {
          id
          isResolved
          comments(first: 1) {
            nodes {
              databaseId
            }
          }
        }
      }
    }
  }
}'
```

通过 `comments.nodes[0].databaseId` 与第四步 REST 返回的 `id` 字段匹配，找到每条已修复评论对应的 thread `id`。

第二步：逐条 Resolve：

```bash
gh api graphql -f query='
mutation {
  resolveReviewThread(input: {threadId: "{threadId}"}) {
    thread {
      isResolved
    }
  }
}'
```

只对**已修复**的评论执行 Resolve；已跳过或已解决（无需改动）的评论不 Resolve，保留线程供后续讨论。

### 第八步：若无"必须修复"项，询问后续操作

```
[WAITING_FOR_USER]
本 PR 无必须修复项，请选择后续操作：
  1. 将本审查报告发布为 PR 评论
  2. 仅展示详细代码修改建议
  3. 不做任何操作
```

选择 1 时执行：
```bash
gh pr comment {number} --body "{report}"
```