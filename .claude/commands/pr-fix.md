根据 PR 的 Comments 和 Review 反馈，自动修复代码中的问题。

## 执行步骤

### 第一步：确定 PR 编号

- 如果用户提供了参数（$ARGUMENTS），使用该编号
- 否则运行以下命令获取最新 open PR：
  ```bash
  gh pr list --state open --limit 1 --json number,title,headRefName
  ```

### 第二步：获取仓库信息

```bash
gh repo view --json nameWithOwner
```

### 第三步：拉取 PR 全量上下文

```bash
# PR 基本信息（含分支名，checkout 时需要）
gh pr view {number} --json number,title,body,author,headRefName,baseRefName,additions,deletions,state,url

# PR 代码 Diff
gh pr diff {number}

# PR 会话评论
gh api repos/{owner}/{repo}/issues/{number}/comments \
  --jq '[.[] | {id: .id, user: .user.login, body: .body, created_at: .created_at}]'

# 行内 Review 评论（最关键，明确指向问题文件和行号）
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '[.[] | {id: .id, user: .user.login, path: .path, line: .line, body: .body, diff_hunk: .diff_hunk}]'

# Review 总结
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '[.[] | {id: .id, user: .user.login, state: .state, body: .body}]'
```

### 第四步：分析需要修复的问题

整理所有 comments 中明确要求修改的内容，归类为：
- **必须修复**：Request Changes 类型的 review、明确指出 bug 的 comment
- **建议优化**：代码风格、可读性改进
- **暂不处理**：讨论性问题、超出 PR 范围的建议

### 第五步：Checkout PR 分支

```bash
gh pr checkout {number}
```

确认当前所在分支正确后再进行修改。

### 第六步：读取并修改涉及的文件

针对每个需要修复的问题：
1. 用 Read 工具读取对应文件的完整内容
2. 理解上下文后用 Edit 工具精确修改
3. 不要修改 comment 未提及的不相关代码

### 第七步：验证修改

- 如果是后端改动，运行：
  ```bash
  cd library-system/backend && export JAVA_HOME=/opt/homebrew/opt/openjdk@17 && mvn compile -q 2>&1
  ```
- 如果是前端改动，运行：
  ```bash
  cd library-system/frontend && npx vue-tsc --noEmit 2>&1
  ```

### 第八步：提交并推送

```bash
git add -p   # 逐块确认变更（或 git add <具体文件>）
git commit -m "fix: address PR #{number} review comments

- {改动点1}
- {改动点2}"
git push
```

### 第九步：回复 PR 说明修复内容

```bash
gh pr comment {number} --body "已根据 Review 反馈完成以下修复：

{每条修复的说明，对应原始 comment}

请重新 Review。"
```

## 注意事项

- 修改前必须先 `gh pr checkout`，确保在正确分支上操作
- 只修复 comments 明确指出的问题，不擅自扩大改动范围
- 编译/类型检查失败时，先修复错误再提交
- 对于无法自动修复的问题（如需要讨论的架构决策），在 PR comment 中说明原因并跳过
