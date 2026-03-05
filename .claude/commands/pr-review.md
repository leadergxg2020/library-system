请对指定的 GitHub Pull Request 进行完整的代码审查。

## 执行步骤

### 第一步：确定 PR 编号

- 如果用户提供了参数（$ARGUMENTS），使用该编号
- 否则运行以下命令获取最新的 open PR：
  ```bash
  gh pr list --state open --limit 1 --json number,title,headRefName
  ```
  如果没有 open PR，尝试获取最近 merged 的：
  ```bash
  gh pr list --state merged --limit 1 --json number,title,headRefName
  ```

### 第二步：获取仓库信息

```bash
gh repo view --json nameWithOwner
```

提取 `owner` 和 `repo` 用于后续 API 调用。

### 第三步：并行拉取 PR 全量上下文（用 Bash 并行执行）

```bash
# PR 基本信息
gh pr view {number} --json number,title,body,author,headRefName,baseRefName,additions,deletions,state,url,commits

# PR 代码 Diff（核心变更）
gh pr diff {number}

# PR 会话评论（普通 comment）
gh api repos/{owner}/{repo}/issues/{number}/comments \
  --jq '[.[] | {id: .id, user: .user.login, body: .body, created_at: .created_at}]'

# 行内 Review 评论（inline comment，带文件和行号）
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '[.[] | {id: .id, user: .user.login, path: .path, line: .line, side: .side, body: .body, diff_hunk: .diff_hunk}]'

# Review 总结（Approve / Request Changes / Comment）
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '[.[] | {id: .id, user: .user.login, state: .state, body: .body, submitted_at: .submitted_at}]'
```

### 第四步：深度分析

综合以上信息，按以下维度进行审查：

1. **代码质量**：命名规范、逻辑清晰度、重复代码
2. **潜在 Bug**：边界条件、空指针、并发问题
3. **安全性**：SQL 注入、XSS、敏感信息泄露
4. **性能**：N+1 查询、不必要的循环、内存泄漏
5. **已有评论的处理**：逐条分析现有 comment 是否已被解决，哪些仍需处理
6. **具体改进建议**：针对每个问题给出修改示例（精确到文件:行号）

### 第五步：输出审查结果后询问用户

展示完整审查报告后，询问：
> 是否将此 Review 发布为 PR 评论？（y/n）

如果用户确认，执行：
```bash
gh pr comment {number} --body "{review_content}"
```
