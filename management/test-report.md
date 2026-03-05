# 集成测试报告

**项目**：图书借阅管理系统
**测试日期**：2026-03-05
**测试方法**：代码静态分析 + 逻辑完整性验证
**测试人员**：QA Agent

---

## 执行摘要

- 验收项目总数：22 项
- 通过：21 项
- 不通过：1 项
- 通过率：95.5%
- 结论：**有条件通过**（1 项 P2 缺陷，不影响核心流程，建议修复后上线）

---

## 详细验收结果

### 图书管理模块（5 项）

| 验收项 | 描述 | 结果 | 备注 |
|--------|------|------|------|
| M1 | 新增图书：ISBN唯一校验（409）、字段校验（400）、返回BookVO含availableQuantity | **通过** | BookService.createBook 先查 ISBN 存在则抛 BusinessException(409)；Controller 使用 @Valid 触发 400；createBook 末尾调用 selectByIsbnWithAvailable 返回含 availableQuantity 的 BookVO |
| M2 | CSV批量导入：文件格式校验、ISBN重复累加库存、逐行错误记录 | **通过** | importBooks 校验文件非空、扩展名 .csv、表头完整性；ISBN 存在则累加 totalQuantity；每行格式错误单独记入 failDetails，不中断整体导入 |
| M3 | 图书列表查询：关键字模糊搜索（书名/作者/ISBN）、分页、可借数量计算 | **通过** | BookMapper.selectPageWithAvailable 的 SQL 对 title / author / isbn 三字段做 LIKE 模糊搜索；可借数量通过子查询 total_quantity - COUNT(未还) 实时计算 |
| M4 | 修改图书：totalQuantity不能小于未还数（422）、ISBN不可改 | **通过** | updateBook 先调用 countUnreturned 校验，不满足则抛 BusinessException(422)；更新语句仅更新 title/author/publisher/totalQuantity，路径参数 isbn 不可被覆盖 |
| M5 | 删除图书：有未还记录禁删（422） | **通过** | deleteBook 调用 countUnreturned，unreturned > 0 时抛 BusinessException(422) |

### 读者管理模块（4 项）

| 验收项 | 描述 | 结果 | 备注 |
|--------|------|------|------|
| M6 | 注册读者：reader_id唯一（409）、maxBorrowCount默认5 | **通过** | createReader 先 selectById 查重，存在则抛 BusinessException(409)；`int maxBorrow = dto.getMaxBorrowCount() != null ? dto.getMaxBorrowCount() : 5` 实现默认值逻辑 |
| M7 | 读者列表：含currentBorrowCount和banned状态 | **通过** | ReaderMapper.selectPageWithStats SQL 子查询计算 current_borrow_count；`(r.ban_until IS NOT NULL AND r.ban_until >= CURDATE()) AS banned` 正确计算禁借状态 |
| M8 | 修改读者：reader_id不可改 | **通过** | updateReader 仅允许修改 name / contact / maxBorrowCount，路径参数 readerId 不进入更新条件以外的字段 |
| M9 | 删除读者：有未还记录禁删（422） | **通过** | deleteReader 调用 borrowRecordMapper.countUnreturnedByReader，unreturned > 0 则抛 BusinessException(422) |

### 借还管理模块（3 项）

| 验收项 | 描述 | 结果 | 备注 |
|--------|------|------|------|
| M10 | 借书：校验顺序（读者→禁借期→借阅上限→图书→库存）、dueDate=today+30天 | **通过** | BorrowService.borrowBook 严格按顺序：① selectById 读者存在 → ② !today.isAfter(banUntil) → ③ countUnreturnedByReader >= maxBorrowCount → ④ selectOne 图书存在 → ⑤ available <= 0；dueDate = today.plusDays(30) |
| M11 | 还书：找不到未还记录404、超期判断（today > dueDate严格大于）、超期惩罚（禁借30天） | **通过** | findUnreturnedRecord 为 null 时抛 BusinessException(404)；`returnDate.isAfter(record.getDueDate())` 为严格大于；超期时 newBanUntil = returnDate.plusDays(30) |
| M12 | 超期惩罚保留逻辑：新禁借日期 vs 原禁借日期取较大值（GREATEST） | **通过** | ReaderMapper.updateBanInfo 使用 `GREATEST(COALESCE(ban_until, '0001-01-01'), #{newBanUntil})` 保证取较大值；ban_reason_date 仅当新禁日期更晚时才更新 |

### 逾期管理模块（1 项）

| 验收项 | 描述 | 结果 | 备注 |
|--------|------|------|------|
| M13 | 逾期列表：return_date IS NULL AND CURDATE() > due_date，含overdueDays计算 | **通过** | BorrowRecordMapper.selectOverdueRecords SQL 条件：`br.return_date IS NULL AND CURDATE() > br.due_date`（严格大于）；`DATEDIFF(CURDATE(), br.due_date) AS overdue_days` 正确计算超期天数 |

### 前端界面（4 项）

| 验收项 | 描述 | 结果 | 备注 |
|--------|------|------|------|
| M14 | 图书管理页：列表/搜索/新增/编辑/删除/CSV导入全功能 | **通过** | BookList.vue 实现：搜索框触发 onSearch、fetchBooks 分页列表、openCreate 新增对话框、openEdit 编辑对话框（ISBN 禁用）、handleDelete 含确认框、openImport CSV 上传对话框含结果展示 |
| M15 | 读者管理页：列表/搜索/新增/编辑/删除/禁借状态展示 | **通过** | ReaderList.vue 实现：关键字搜索、分页列表、currentBorrowCount 以 el-tag 标色、banned 字段控制禁借状态展示（含 tooltip 显示禁借截止日和原因日期）、编辑时 readerId disabled |
| M16 | 借还管理页：借书操作/还书操作/结果展示（超期警告） | **通过** | BorrowManage.vue 左右两栏：借书区展示借阅成功结果（含 dueDate 高亮）；还书区区分正常还书（success alert）和超期还书（warning alert），超期时展示 overdueDays 和 banUntil |
| M17 | 逾期列表页：逾期记录展示/超期天数标色 | **通过** | OverdueList.vue 展示所有逾期字段；overdueTagType 函数：>30天返回 'danger'，>7天返回 'warning'，其余返回 'info'，以 el-tag effect="dark" 差异化标色 |

### 架构合规（5 项）

| 验收项 | 描述 | 结果 | 备注 |
|--------|------|------|------|
| A1 | 全注解 Mapper（无XML文件） | **通过** | 全局搜索 *Mapper.xml 无结果；BookMapper、ReaderMapper、BorrowRecordMapper 均使用 @Select / @Update 注解编写 SQL |
| A2 | Service无接口层（直接具体类） | **通过** | BookService、ReaderService、BorrowService 均为直接 @Service 类，无对应 IBookService 等接口 |
| A3 | 统一响应体 Result&lt;T&gt; | **通过** | Result&lt;T&gt; 包含 code / message / data 三字段；所有 Controller 方法均返回 Result.success(...) 或由全局异常处理器返回 Result.fail(...) |
| A4 | 全局异常处理（400/404/409/422/500） | **不通过（P2）** | GlobalExceptionHandler 实现了 MethodArgumentNotValidException(400)、BusinessException（透传业务 code）、Exception(500)。但 @ResponseStatus(HttpStatus.OK) 导致**所有异常响应的 HTTP 状态码均为 200**，仅在响应体 JSON 的 code 字段中携带业务错误码。若前端或外部调用方依赖 HTTP 状态码判断错误，将无法正确感知。当前前端通过响应体 code 字段判断，实际功能可用，但不符合 RESTful 规范及接口契约定义的 HTTP 状态码语义。 |
| A5 | CORS配置允许localhost:5173 | **通过** | WebMvcConfig.addCorsMappings 对 `/api/**` 路径允许 `http://localhost:5173` 的 GET/POST/PUT/DELETE/OPTIONS 请求 |

---

## 集成场景验证

以下 5 个端到端场景的代码实现路径分析：

### 场景 1：完整借书流程（注册读者 → 新增图书 → 借书 → 成功返回借阅记录）

**路径**：
1. `POST /api/readers` → ReaderService.createReader → 校验 reader_id 唯一 → 插入 t_reader → 返回 ReaderVO
2. `POST /api/books` → BookService.createBook → 校验 ISBN 唯一 → 插入 t_book → 返回含 availableQuantity 的 BookVO
3. `POST /api/borrows` → BorrowService.borrowBook → 按序校验 → 插入 t_borrow_record → selectBorrowSuccessVO 联表查询返回读者姓名、书名、借出日期、dueDate

**结论**：路径完整，逻辑闭合。

### 场景 2：超期还书 + 禁借（借书 → 模拟超期 → 还书 → 禁借期更新）

**路径**：
1. `POST /api/borrows` → 生成借阅记录，due_date = 借出日 + 30
2. 还书时 returnDate > dueDate（严格大于）→ `returnDate.isAfter(record.getDueDate())` 为 true
3. 计算 overdueDays = returnDate - dueDate（epoch day 差值）
4. newBanUntil = returnDate + 30 天
5. `readerMapper.updateBanInfo` 执行 `GREATEST(COALESCE(ban_until, '0001-01-01'), newBanUntil)` 更新
6. 返回 ReturnResultVO 含 overdue=true、overdueDays、banUntil

**结论**：路径完整，超期判断和禁借写入逻辑正确。

### 场景 3：禁借期内借书（验证禁借期判断拦截）

**路径**：
1. `POST /api/borrows` → BorrowService.borrowBook
2. 读者查到后：`reader.getBanUntil() != null && !today.isAfter(reader.getBanUntil())` → today <= banUntil 则禁借
3. 抛出 BusinessException(422，含禁借截止日期和原因日期信息)

**结论**：路径完整，边界语义正确（today == banUntil 当天仍被禁借）。

### 场景 4：图书库存保护（借光所有库存 → 再借提示库存不足）

**路径**：
1. BorrowService.borrowBook 步骤 5：`int unreturned = bookMapper.countUnreturned(isbn)`
2. `int available = book.getTotalQuantity() - unreturned`
3. `if (available <= 0)` → 抛出 BusinessException(422，"库存不足，当前可借数量：0")

**结论**：路径完整，可借数量实时计算（不存储），不存在缓存失效问题。

### 场景 5：CSV批量导入（含重复ISBN和格式错误行的混合场景）

**路径**：
1. BookService.importBooks 逐行处理：
   - ISBN 格式非 10/13 位数字 → failCount++，failDetails 记录行号和原因
   - title/author/publisher 为空 → failCount++，failDetails 记录
   - quantity 非正整数 → failCount++，failDetails 记录
   - ISBN 已存在 → `UPDATE t_book SET total_quantity = existing + quantity`，accumulatedCount++
   - 全新 ISBN → INSERT，successCount++
2. 事务包裹整体导入（@Transactional），若 CSV 解析本身失败则回滚；行级错误跳过不影响事务

**结论**：路径完整，混合场景处理正确。注意：整体在同一事务中，若服务器异常（非行级错误）会导致已成功行一并回滚，属于合理设计。

---

## 遗留问题

| 级别 | 描述 | 影响范围 |
|------|------|----------|
| P2 | GlobalExceptionHandler 所有异常处理方法均标注 @ResponseStatus(HttpStatus.OK)，导致 HTTP 响应状态码始终为 200，业务错误码（400/404/409/422/500）仅体现在响应体 JSON 的 code 字段中，不符合 RESTful 标准和接口契约中对 HTTP 状态码的定义 | 外部系统集成、API 测试工具（Postman）、监控系统若依赖 HTTP 状态码判断请求成功/失败将产生误判；当前前端通过响应体 code 字段判断，功能不受影响 |

---

## 结论

代码静态分析共验收 22 项，**21 项通过，1 项不通过（P2）**。

所有核心业务逻辑（图书管理、读者管理、借还管理、逾期管理）实现完整且正确：
- 校验顺序、错误码、业务规则（GREATEST 禁借保留、严格大于超期判断、可借数量实时计算）均与 PRD 规格完全吻合
- 前端四个页面功能全覆盖，超期标色、禁借状态展示、CSV 导入结果反馈均已实现
- 架构合规 4/5 通过，唯一问题为全局异常处理 HTTP 状态码语义不规范（P2 级，不影响当前前端功能）

**建议**：修复 GlobalExceptionHandler 中的 @ResponseStatus 注解（将 400/404/409/422/500 等 BusinessException 对应的 HTTP 状态码调整为正确值），修复后可直接上线。当前版本属于**有条件通过**。
