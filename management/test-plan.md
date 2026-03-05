# 图书借阅管理系统 — 测试计划

**项目：** 图书借阅管理系统
**文档版本：** v1.0
**编制日期：** 2026-03-05
**编制人：** QA 测试工程师
**后端地址：** http://localhost:8080
**技术栈：** Spring Boot 3.x + MySQL 8 / Vue 3 + Element Plus

---

## 一、测试范围与优先级

### 1.1 功能测试优先级矩阵

| 编号 | 功能模块 | 接口 | 优先级 | 测试重点 |
|------|----------|------|--------|----------|
| F01 | 新增图书 | POST /api/books | P0 | ISBN唯一约束、必填字段校验、成功写库 |
| F02 | 批量导入CSV | POST /api/books/import | P1 | 文件格式校验、表头校验、ISBN冲突累加库存、部分失败不中断、结果摘要 |
| F03 | 图书查询 | GET /api/books | P1 | 多维度模糊搜索、分页正确性、空结果处理 |
| F04 | 修改图书信息 | PUT /api/books/{isbn} | P1 | ISBN只读、库存不能小于已借出数 |
| F05 | 删除图书 | DELETE /api/books/{isbn} | P0 | 有未还记录禁止删除（R6）、历史记录保留 |
| F06 | 读者注册 | POST /api/readers | P1 | 读者ID唯一、联系方式可选、max_borrow_count默认值=5 |
| F07 | 读者查询/列表 | GET /api/readers | P1 | ID/姓名模糊搜索、禁借状态展示 |
| F08 | 修改读者信息 | PUT /api/readers/{readerId} | P1 | max_borrow_count不能小于当前借阅数 |
| F09 | 借书操作 | POST /api/borrows | P0 | 读者存在/禁借期/借阅上限/库存四重校验、事务原子性（R1/R4/R5） |
| F10 | 还书操作 | POST /api/returns | P0 | 超期判断、禁借处罚、禁借保留规则、事务原子性（R2/R3） |
| F11 | 逾期列表 | GET /api/borrows/overdue | P0 | 逾期定义精确（R7）、按逾期天数降序排列 |

### 1.2 核心业务规则覆盖矩阵

| 规则 | 描述 | 覆盖测试用例 |
|------|------|-------------|
| R1 | 借阅期30天，应还日期 = 借出日期 + 30天 | TC-BR-001, TC-IT-001 |
| R2 | 超期还书，禁借截止日期 = 还书日期 + 30天 | TC-RT-002 |
| R3 | 新禁借截止日期 ≤ 原禁借截止日期时保留原值 | TC-RT-005, TC-RT-006 |
| R4 | 当前未还数 ≥ max_borrow_count 时拒绝借书 | TC-BR-005, TC-IT-004 |
| R5 | 库存为0时禁止借出 | TC-BR-006 |
| R6 | 有未还记录时禁止删除图书 | TC-BK-014, TC-IT-005 |
| R7 | 逾期：当前日期 > 应还日期（严格大于，相等不算） | TC-OD-001, TC-OD-002 |

---

## 二、接口测试用例清单

### 2.1 图书管理接口

#### POST /api/books — 新增图书

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-BK-001 | 正常新增图书（全字段） | `{"isbn":"9787111111111","title":"Java编程思想","author":"Bruce Eckel","stock":10}` | code:200, data.isbn="9787111111111" |
| TC-BK-002 | 新增图书（省略可选字段） | `{"isbn":"9787222222222","title":"算法导论","author":"CLRS","stock":5}` | code:200 |
| TC-BK-003 | ISBN重复冲突 | 使用已存在ISBN再次POST | code:409, message含"ISBN已存在" |
| TC-BK-004 | ISBN字段为空 | `{"isbn":"","title":"测试书","author":"作者","stock":1}` | code:400 |
| TC-BK-005 | title字段缺失 | `{"isbn":"9787333333333","author":"作者","stock":1}` | code:400 |
| TC-BK-006 | stock为负数 | `{"isbn":"9787444444444","title":"负库存","author":"作者","stock":-1}` | code:400 |
| TC-BK-007 | stock为0（边界值） | `{"isbn":"9787555555555","title":"零库存书","author":"作者","stock":0}` | code:200, data.stock=0 |
| TC-BK-008 | ISBN格式不合法（非数字） | `{"isbn":"ISBN-INVALID","title":"书","author":"作者","stock":1}` | code:400 |
| TC-BK-009 | 数据库写入验证 | 新增成功后GET /api/books?keyword={isbn} | 查询结果包含新增图书，stock值正确 |

#### POST /api/books/import — 批量导入CSV

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-CSV-001 | 正常CSV全量导入 | multipart上传合法CSV文件（10行） | code:200, data.success=10, data.failed=0 |
| TC-CSV-002 | 表头错误（列名写错） | 上传表头为`bookIsbn,bookTitle,...`的文件 | code:400, message含"表头不正确" |
| TC-CSV-003 | 非CSV文件（上传.xlsx） | multipart上传Excel文件 | code:400, message含"文件格式" |
| TC-CSV-004 | 空文件（0字节） | 上传空CSV文件 | code:400 |
| TC-CSV-005 | ISBN冲突累加库存（TC-CSV-冲突） | CSV中包含已存在ISBN行，原库存5，CSV中stock=3 | code:200, 该ISBN库存变为8，title/author不变 |
| TC-CSV-006 | 同文件ISBN重复（TC-CSV-同文件重复） | CSV中同一ISBN出现2次，quantity均为2 | code:200, 该ISBN累加2次，库存+4 |
| TC-CSV-007 | 部分行格式错误（TC-CSV-部分失败） | CSV混合10合法行+3格式错误行 | code:200, data.success=10, data.failed=3, data.errors包含失败行明细 |
| TC-CSV-008 | 仅有表头无数据行 | 上传只有表头的CSV | code:200, data.success=0, data.failed=0 |
| TC-CSV-009 | stock列为非数字 | CSV某行stock="abc" | 该行跳过，其他行正常处理 |
| TC-CSV-010 | 导入后数据库一致性验证 | 导入完成后逐ISBN查询数据库 | 每条记录库存值与预期一致 |

**标准CSV表头格式：**
```
isbn,title,author,stock
```

#### GET /api/books — 图书列表查询

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-BK-010 | 无参数查询（默认分页） | `GET /api/books` | code:200, data.list非空，data.total>0 |
| TC-BK-011 | 按书名模糊搜索 | `GET /api/books?keyword=Java` | code:200, 所有结果title包含"Java" |
| TC-BK-012 | 按作者模糊搜索 | `GET /api/books?keyword=Bruce` | code:200, 所有结果author包含"Bruce" |
| TC-BK-013 | 按ISBN精确/模糊搜索 | `GET /api/books?keyword=978711` | code:200, 返回匹配ISBN的图书 |
| TC-BK-014 | 搜索无结果 | `GET /api/books?keyword=XYZNOTEXIST` | code:200, data.list=[], data.total=0 |
| TC-BK-015 | 分页参数验证 | `GET /api/books?pageNum=2&pageSize=5` | code:200, data.list长度≤5 |
| TC-BK-016 | pageNum超出范围 | `GET /api/books?pageNum=9999` | code:200, data.list=[] |
| TC-BK-017 | pageSize为0或负数 | `GET /api/books?pageSize=0` | code:400 |

#### PUT /api/books/{isbn} — 修改图书信息

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-BK-018 | 正常修改title和author | `PUT /api/books/9787111111111` body: `{"title":"新书名","author":"新作者","stock":10}` | code:200, 返回更新后图书信息 |
| TC-BK-019 | ISBN只读（body中传不同isbn不生效） | `PUT /api/books/9787111111111` body中isbn字段传`9789999999999` | code:200, 但isbn保持不变 |
| TC-BK-020 | 修改库存低于已借出数 | 已借出3本，尝试将stock改为2 | code:422, message含"库存不能小于已借出数量" |
| TC-BK-021 | 修改库存等于已借出数（边界值） | 已借出3本，将stock改为3 | code:200, stock=3 |
| TC-BK-022 | 图书不存在 | `PUT /api/books/9780000000000` | code:404 |
| TC-BK-023 | 修改后数据库一致性 | PUT成功后GET该ISBN | 返回字段与修改内容一致 |

#### DELETE /api/books/{isbn} — 删除图书

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-BK-024 | 正常删除无借阅记录的图书 | `DELETE /api/books/9787555555555` | code:200 |
| TC-BK-025 | 有未还记录禁止删除（TC-R6） | 图书有未还借阅记录时DELETE | code:422, message含"存在未归还记录" |
| TC-BK-026 | 图书不存在 | `DELETE /api/books/9780000000000` | code:404 |
| TC-BK-027 | 删除后历史记录保留验证 | DELETE成功后查询借阅历史表 | 历史借阅记录仍存在（软删除或历史保留） |
| TC-BK-028 | 已还清可正常删除 | 所有借阅记录均已还书 → DELETE | code:200 |

---

### 2.2 读者管理接口

#### POST /api/readers — 读者注册

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-RD-001 | 正常注册（全字段） | `{"readerId":"R001","name":"张三","contact":"13800138000","maxBorrowCount":5}` | code:200, data.readerId="R001" |
| TC-RD-002 | 省略联系方式（可选字段） | `{"readerId":"R002","name":"李四"}` | code:200, contact=null或空 |
| TC-RD-003 | 省略maxBorrowCount（默认5） | `{"readerId":"R003","name":"王五"}` | code:200, data.maxBorrowCount=5 |
| TC-RD-004 | 读者ID重复 | 使用已存在readerId再次POST | code:409, message含"读者ID已存在" |
| TC-RD-005 | readerId为空 | `{"readerId":"","name":"测试"}` | code:400 |
| TC-RD-006 | name为空 | `{"readerId":"R004","name":""}` | code:400 |
| TC-RD-007 | maxBorrowCount=0（边界值） | `{"readerId":"R005","name":"测试","maxBorrowCount":0}` | code:400（上限不能为0）或code:200视业务规则 |
| TC-RD-008 | maxBorrowCount为负数 | `{"readerId":"R006","name":"测试","maxBorrowCount":-1}` | code:400 |

#### GET /api/readers — 读者列表查询

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-RD-009 | 无参数全量查询 | `GET /api/readers` | code:200, data.list包含所有读者 |
| TC-RD-010 | 按读者ID模糊搜索 | `GET /api/readers?keyword=R00` | code:200, 所有结果readerId包含"R00" |
| TC-RD-011 | 按姓名模糊搜索 | `GET /api/readers?keyword=张` | code:200, 所有结果name包含"张" |
| TC-RD-012 | 禁借状态展示验证 | 查询在禁借期内的读者 | data.list中该读者isBanned=true, banEndDate有值 |
| TC-RD-013 | 禁借已过期的读者状态 | 查询禁借截止日期已过的读者 | isBanned=false 或 banEndDate<today |
| TC-RD-014 | 搜索无结果 | `GET /api/readers?keyword=XYZNOTEXIST` | code:200, data.list=[] |

#### PUT /api/readers/{readerId} — 修改读者信息

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-RD-015 | 正常修改联系方式 | `PUT /api/readers/R001` body: `{"contact":"13999999999"}` | code:200 |
| TC-RD-016 | 修改maxBorrowCount低于当前借阅数 | 当前借阅3本，尝试将maxBorrowCount改为2 | code:422, message含"不能小于当前借阅数量" |
| TC-RD-017 | 修改maxBorrowCount等于当前借阅数（边界值） | 当前借阅3本，将maxBorrowCount改为3 | code:200 |
| TC-RD-018 | 读者不存在 | `PUT /api/readers/R999` | code:404 |
| TC-RD-019 | 修改后数据库一致性 | PUT成功后GET该readerId | 返回字段与修改内容一致 |

---

### 2.3 借还管理接口

#### POST /api/borrows — 借书操作

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-BR-001 | 正常借书（应还日期验证R1） | `{"readerId":"R001","isbn":"9787111111111"}` | code:200, data.dueDate = borrowDate+30天, 库存-1 |
| TC-BR-002 | 读者不存在 | `{"readerId":"R999","isbn":"9787111111111"}` | code:404, message含"读者不存在" |
| TC-BR-003 | 图书不存在 | `{"readerId":"R001","isbn":"9780000000000"}` | code:404, message含"图书不存在" |
| TC-BR-004 | 读者在禁借期内（TC-BR-禁借期） | 读者banEndDate > 今天，尝试借书 | code:422, message含禁借截止日期 |
| TC-BR-005 | 读者达借阅上限（TC-BR-上限） | 读者已借5本(maxBorrowCount=5)，尝试借第6本 | code:422, message含"已达借阅上限" |
| TC-BR-006 | 图书库存为0（TC-BR-库存0） | 目标图书stock=0，尝试借出 | code:422, message含"库存不足" |
| TC-BR-007 | borrowDate不传时默认今天 | 请求体不含borrowDate | code:200, data.borrowDate=今天, data.dueDate=今天+30天 |
| TC-BR-008 | readerId字段缺失 | `{"isbn":"9787111111111"}` | code:400 |
| TC-BR-009 | isbn字段缺失 | `{"readerId":"R001"}` | code:400 |
| TC-BR-010 | 借书后库存一致性 | 借书成功后GET /api/books?keyword={isbn} | 该图书stock减1，availableStock减1 |
| TC-BR-011 | 事务原子性（TC-BR-事务） | 模拟借书中途数据库异常（可通过数据库kill连接等方式） | 事务回滚，库存不变，借阅记录不存在 |

#### POST /api/returns — 还书操作

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-RT-001 | 准时还书，无超期（TC-RT-不超期） | 还书日期 ≤ dueDate | code:200, 读者banEndDate不变，库存+1 |
| TC-RT-002 | 超期还书（TC-RT-超期） | 还书日期 > dueDate（已过期） | code:200, 读者banEndDate = returnDate+30天, 库存+1 |
| TC-RT-003 | 还书记录不存在 | `{"borrowId":99999}` | code:404 |
| TC-RT-004 | 已还过的记录再次还书 | 对已return_date非空的记录POST /api/returns | code:422, message含"该记录已归还" |
| TC-RT-005 | 禁借保留：新禁借期 ≤ 原禁借期（TC-RT-禁借保留） | 读者banEndDate=2026-06-01，超期还书触发新banEndDate=2026-04-01 | code:200, 读者banEndDate仍为2026-06-01（保留原值R3） |
| TC-RT-006 | 禁借更新：新禁借期 > 原禁借期（TC-RT-禁借更新） | 读者banEndDate=2026-04-01，超期还书触发新banEndDate=2026-06-01 | code:200, 读者banEndDate更新为2026-06-01 |
| TC-RT-007 | 多条未还记录选最早一条（TC-RT-多条记录） | 同一读者同一ISBN有2条未还借阅记录（borrowDate不同） | code:200, 还的是borrowDate最早那条记录 |
| TC-RT-008 | 还书后库存一致性 | 还书成功后GET /api/books?keyword={isbn} | 该图书stock加1 |
| TC-RT-009 | 还书后读者借阅数一致性 | 还书成功后GET /api/readers?keyword={readerId} | 读者currentBorrowCount减1 |

#### GET /api/borrows/overdue — 逾期列表

| 用例编号 | 测试描述 | 请求示例 | 期望响应 |
|----------|----------|----------|----------|
| TC-OD-001 | 应还日期=今天不出现（TC-OD-精确） | 构造dueDate=today的未还记录 | code:200, 该记录不在data.list中 |
| TC-OD-002 | 应还日期=昨天出现，逾期天数=1（TC-OD-逾期） | 构造dueDate=yesterday的未还记录 | code:200, 该记录在data.list中, overdueDays=1 |
| TC-OD-003 | 已还书记录不出现在逾期列表 | 有return_date的借阅记录 | code:200, 该记录不在data.list中 |
| TC-OD-004 | 按逾期天数降序排列（TC-OD-排序） | 构造逾期天数不同的多条记录（5天/3天/1天） | code:200, data.list按overdueDays降序，5>3>1 |
| TC-OD-005 | 无逾期记录时返回空列表 | 清空所有逾期记录后查询 | code:200, data.list=[] |
| TC-OD-006 | 逾期天数计算精确性 | 构造dueDate=3天前的记录 | code:200, overdueDays=3 |

---

## 三、集成测试场景

### 场景1：新书入库到借出的完整流程

**前置条件：** 数据库干净环境，无冲突数据

**操作步骤：**
1. `POST /api/books` 新增图书（isbn=TC-IT-001-BOOK，stock=5）
2. `GET /api/books?keyword=TC-IT-001-BOOK` 验证入库成功，stock=5
3. `POST /api/readers` 注册读者（readerId=IT-READER-001）
4. `POST /api/borrows` 读者IT-READER-001借阅TC-IT-001-BOOK
5. `GET /api/books?keyword=TC-IT-001-BOOK` 验证库存

**验证点：**
- Step 1：code=200，图书入库成功
- Step 2：stock=5，availableStock=5
- Step 3：code=200，读者注册成功，maxBorrowCount=5
- Step 4：code=200，dueDate = 今天 + 30天（R1验证）
- Step 5：stock=5，availableStock=4（库存可用数-1）

---

### 场景2：超期还书触发禁借，禁借期内无法借书

**前置条件：** 读者IT-READER-002已注册，图书IT-BOOK-002库存≥1

**操作步骤：**
1. `POST /api/borrows` 读者IT-READER-002借阅IT-BOOK-002（正常借书）
2. 数据库直接修改该借阅记录的 `due_date` 为今天-1（模拟已逾期）
3. `POST /api/returns` 还书
4. `GET /api/readers?keyword=IT-READER-002` 查看禁借截止日期
5. `POST /api/borrows` 读者IT-READER-002尝试再借任意图书

**验证点：**
- Step 3：code=200，还书成功
- Step 4：读者 banEndDate = 还书日期 + 30天（R2验证）
- Step 5：code=422，message含禁借截止日期（禁借期内拒绝，R4补充验证）

---

### 场景3：CSV批量导入后借书

**前置条件：** 准备标准测试CSV文件（10行合法数据，isbn均不存在）

**操作步骤：**
1. `POST /api/books/import` 上传CSV文件（10本书，每本stock=3）
2. 验证导入结果摘要（success=10, failed=0）
3. 随机取CSV中一个ISBN，`GET /api/books?keyword={isbn}` 确认库存=3
4. `POST /api/readers` 注册读者IT-READER-003（若不存在）
5. `POST /api/borrows` 读者IT-READER-003借阅该ISBN图书
6. `GET /api/books?keyword={isbn}` 验证库存

**验证点：**
- Step 2：data.success=10, data.failed=0
- Step 3：stock=3，availableStock=3
- Step 5：code=200，借书成功
- Step 6：stock=3，availableStock=2（减1）

---

### 场景4：读者达借书上限后无法借书，还书后恢复

**前置条件：** 准备4本不同图书（库存各≥1），读者IT-READER-004（maxBorrowCount=3）

**操作步骤：**
1. `POST /api/readers` 注册读者IT-READER-004（maxBorrowCount=3）
2. `POST /api/borrows` 读者借第1本书
3. `POST /api/borrows` 读者借第2本书
4. `POST /api/borrows` 读者借第3本书（达上限）
5. `POST /api/borrows` 读者尝试借第4本书
6. `GET /api/readers?keyword=IT-READER-004` 查看当前借阅数
7. `POST /api/returns` 还第1本书
8. `POST /api/borrows` 读者再次尝试借第4本书

**验证点：**
- Step 2-4：均code=200，成功借书
- Step 5：code=422，message含"已达借阅上限"（R4验证）
- Step 6：currentBorrowCount=3
- Step 7：code=200，还书成功
- Step 8：code=200，成功借书（解除上限限制）

---

### 场景5：删除保护验证（有借阅记录时禁止删除）

**前置条件：** 无

**操作步骤：**
1. `POST /api/books` 新增图书IT-BOOK-005（stock=2）
2. `POST /api/readers` 注册读者IT-READER-005（若不存在）
3. `POST /api/borrows` 读者IT-READER-005借阅IT-BOOK-005
4. `DELETE /api/books/{IT-BOOK-005-isbn}` 尝试删除图书
5. `GET /api/books?keyword={IT-BOOK-005-isbn}` 确认图书仍存在
6. `POST /api/returns` 还书
7. `DELETE /api/books/{IT-BOOK-005-isbn}` 再次尝试删除图书
8. `GET /api/books?keyword={IT-BOOK-005-isbn}` 确认图书已删除
9. 查询数据库 borrow_records 表确认历史记录保留

**验证点：**
- Step 4：code=422，message含"存在未归还记录"（R6验证）
- Step 5：图书仍存在，stock未变
- Step 6：code=200，还书成功
- Step 7：code=200，删除成功
- Step 8：图书不再出现在查询结果
- Step 9：历史借阅记录完整保留

---

## 四、测试数据准备

### 4.1 测试图书数据（预置5条）

```sql
-- 测试图书数据初始化
INSERT INTO books (isbn, title, author, stock, created_at) VALUES
('9787111596615', 'Java编程思想（第4版）', 'Bruce Eckel', 10, NOW()),
('9787115428028', 'Spring Boot实战', 'Craig Walls', 5, NOW()),
('9787121362217', '算法导论（第3版）', 'CLRS', 3, NOW()),
('9787115550118', 'MySQL必知必会', 'Ben Forta', 8, NOW()),
('9787121327223', 'Vue.js实战', '梁灏', 0, NOW());  -- 库存为0，用于测试R5
```

### 4.2 测试读者数据（预置3个）

```sql
-- 正常读者（无限制）
INSERT INTO readers (reader_id, name, contact, max_borrow_count, ban_end_date, created_at) VALUES
('TEST-READER-001', '正常读者张三', '13800138001', 5, NULL, NOW()),

-- 达借阅上限的读者（当前借阅数=上限）
('TEST-READER-002', '上限读者李四', '13800138002', 3, NULL, NOW()),

-- 在禁借期内的读者
('TEST-READER-003', '禁借读者王五', '13800138003', 5, DATE_ADD(NOW(), INTERVAL 20 DAY), NOW());
```

### 4.3 测试借阅记录（覆盖各种状态）

```sql
-- 为TEST-READER-002创建3条未还记录（使其达到借阅上限）
INSERT INTO borrow_records (reader_id, isbn, borrow_date, due_date, return_date) VALUES
('TEST-READER-002', '9787111596615', DATE_SUB(NOW(), INTERVAL 5 DAY),
    DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 30 DAY), NULL),
('TEST-READER-002', '9787115428028', DATE_SUB(NOW(), INTERVAL 3 DAY),
    DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 30 DAY), NULL),
('TEST-READER-002', '9787121362217', DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 30 DAY), NULL),

-- 正常借阅记录（未逾期，用于基础验证）
('TEST-READER-001', '9787115550118', DATE_SUB(NOW(), INTERVAL 10 DAY),
    DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 30 DAY), NULL),

-- 已还书记录（历史记录）
('TEST-READER-001', '9787111596615', DATE_SUB(NOW(), INTERVAL 60 DAY),
    DATE_ADD(DATE_SUB(NOW(), INTERVAL 60 DAY), INTERVAL 30 DAY),
    DATE_SUB(NOW(), INTERVAL 35 DAY)),

-- 逾期未还记录（dueDate=昨天，用于TC-OD-002）
('TEST-READER-001', '9787121327223', DATE_SUB(NOW(), INTERVAL 31 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY), NULL);

-- 同步更新books表中可用库存（根据实际未还数量）
-- 9787111596615: stock=10, 被借1本未还 → 可用9
-- 9787115428028: stock=5, 被借1本未还 → 可用4
-- 9787121362217: stock=3, 被借1本未还 → 可用2
-- 9787115550118: stock=8, 被借1本未还 → 可用7
-- 9787121327223: stock=0（初始库存0，被借1本 → 异常，此条需调整stock为1）
UPDATE books SET stock = 1 WHERE isbn = '9787121327223';
```

### 4.4 测试CSV文件样本

**正常CSV文件（test-import-normal.csv）：**
```csv
isbn,title,author,stock
9787302423287,深入理解Java虚拟机,周志明,5
9787111641247,Kubernetes权威指南,龚正,3
9787121388965,Redis设计与实现,黄健宏,8
9787115583864,网络是怎样连接的,户根勤,4
9787040484984,操作系统概念,Abraham,6
```

**含ISBN冲突的CSV文件（test-import-conflict.csv）：**
```csv
isbn,title,author,stock
9787111596615,Java编程思想（第4版）,Bruce Eckel,3
9787302999999,新书不冲突,新作者,5
```
说明：第1行isbn已存在，预期stock累加3；第2行正常新增。

**含格式错误的CSV文件（test-import-mixed.csv）：**
```csv
isbn,title,author,stock
9787302111111,合法图书一,合法作者,5
,空ISBN图书,某作者,3
9787302222222,合法图书二,合法作者,abc
9787302333333,合法图书三,合法作者,2
```
说明：第2行isbn为空（格式错误），第3行stock非数字（格式错误），第1/4行合法。预期success=2, failed=2。

**表头错误的CSV文件（test-import-wrong-header.csv）：**
```csv
bookIsbn,bookTitle,bookAuthor,bookStock
9787302444444,测试书名,测试作者,5
```
说明：表头列名与规范不符，预期导入终止并返回400。

---

## 五、测试执行说明

### 5.1 环境准备

```bash
# 确认后端服务已启动
curl -s http://localhost:8080/actuator/health | jq '.status'
# 期望输出："UP"

# 确认数据库连接
mysql -u root -p library_db -e "SELECT COUNT(*) FROM books;"
```

### 5.2 测试数据初始化

```bash
# 执行初始化SQL脚本
mysql -u root -p library_db < test-data-init.sql

# 验证数据初始化结果
mysql -u root -p library_db -e "
  SELECT 'books' as table_name, COUNT(*) as count FROM books
  UNION ALL
  SELECT 'readers', COUNT(*) FROM readers
  UNION ALL
  SELECT 'borrow_records', COUNT(*) FROM borrow_records;
"
```

### 5.3 借书接口完整测试执行序列

以下演示对借书接口 `POST /api/borrows` 的完整测试序列：

#### 步骤1：确认前置数据存在

```bash
# 确认读者存在
curl -s "http://localhost:8080/api/readers?keyword=TEST-READER-001" | jq '.data.list[0]'

# 确认图书存在且有库存
curl -s "http://localhost:8080/api/books?keyword=9787111596615" | jq '.data.list[0] | {isbn, title, stock}'
```

#### 步骤2：执行正常借书（TC-BR-001）

```bash
curl -s -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"readerId":"TEST-READER-001","isbn":"9787111596615"}' | jq '.'

# 期望响应：
# {
#   "code": 200,
#   "message": "success",
#   "data": {
#     "borrowId": 1,
#     "readerId": "TEST-READER-001",
#     "isbn": "9787111596615",
#     "borrowDate": "2026-03-05",
#     "dueDate": "2026-04-04"   ← 验证R1: borrowDate + 30天
#   }
# }
```

#### 步骤3：验证借书后库存变化

```bash
curl -s "http://localhost:8080/api/books?keyword=9787111596615" \
  | jq '.data.list[0] | {isbn, stock, availableStock}'

# 验证stock可用数已减1
mysql -u root -p library_db -e \
  "SELECT isbn, stock, (stock - COUNT(br.id)) as available_stock
   FROM books b
   LEFT JOIN borrow_records br ON b.isbn = br.isbn AND br.return_date IS NULL
   WHERE b.isbn = '9787111596615'
   GROUP BY b.isbn, b.stock;"
```

#### 步骤4：测试禁借期内拒绝借书（TC-BR-004）

```bash
# 读者TEST-READER-003在禁借期内（见测试数据）
curl -s -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"readerId":"TEST-READER-003","isbn":"9787115428028"}' | jq '.'

# 期望响应：
# {
#   "code": 422,
#   "message": "读者处于禁借期，禁借截止日期：XXXX-XX-XX",
#   "data": null
# }
```

#### 步骤5：测试借阅上限拒绝（TC-BR-005）

```bash
# 读者TEST-READER-002已借3本（maxBorrowCount=3，已达上限）
curl -s -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"readerId":"TEST-READER-002","isbn":"9787115550118"}' | jq '.'

# 期望响应：
# {
#   "code": 422,
#   "message": "已达借阅上限，当前借阅数：3/3",
#   "data": null
# }
```

#### 步骤6：测试库存为0拒绝（TC-BR-006）

```bash
# 图书9787121327223 stock=0
curl -s -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"readerId":"TEST-READER-001","isbn":"9787121327223"}' | jq '.'

# 期望响应：
# {
#   "code": 422,
#   "message": "图书库存不足",
#   "data": null
# }
```

#### 步骤7：验证数据库最终状态

```bash
mysql -u root -p library_db -e "
  -- 查看借阅记录
  SELECT br.id, br.reader_id, br.isbn, br.borrow_date, br.due_date, br.return_date
  FROM borrow_records br
  WHERE br.reader_id = 'TEST-READER-001'
  ORDER BY br.borrow_date DESC LIMIT 5;

  -- 查看读者当前借阅数
  SELECT r.reader_id, r.name, COUNT(br.id) as current_borrow_count, r.max_borrow_count
  FROM readers r
  LEFT JOIN borrow_records br ON r.reader_id = br.reader_id AND br.return_date IS NULL
  WHERE r.reader_id = 'TEST-READER-001'
  GROUP BY r.reader_id, r.name, r.max_borrow_count;
"
```

### 5.4 还书超期测试执行序列（TC-RT-002 & R2/R3）

```bash
# 1. 先借一本书
BORROW_RESULT=$(curl -s -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"readerId":"TEST-READER-001","isbn":"9787115428028"}')
BORROW_ID=$(echo $BORROW_RESULT | jq '.data.borrowId')
echo "借阅记录ID: $BORROW_ID"

# 2. 数据库直接修改due_date为昨天（模拟超期）
mysql -u root -p library_db -e \
  "UPDATE borrow_records SET due_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY) WHERE id = $BORROW_ID;"

# 3. 还书
curl -s -X POST http://localhost:8080/api/returns \
  -H "Content-Type: application/json" \
  -d "{\"borrowId\":$BORROW_ID}" | jq '.'

# 4. 验证禁借截止日期 = 还书日期(今天) + 30天
mysql -u root -p library_db -e \
  "SELECT reader_id, ban_end_date, DATE_ADD(CURDATE(), INTERVAL 30 DAY) as expected_ban_end
   FROM readers WHERE reader_id = 'TEST-READER-001';"

# 5. 尝试在禁借期内借书（应被拒绝）
curl -s -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"readerId":"TEST-READER-001","isbn":"9787121362217"}' | jq '.code, .message'
# 期望：422, "读者处于禁借期..."
```

### 5.5 逾期列表精确边界测试（TC-OD-001 & TC-OD-002）

```bash
# 准备边界数据
mysql -u root -p library_db -e "
  -- 创建due_date=今天的未还记录（不应出现在逾期列表）
  INSERT INTO borrow_records (reader_id, isbn, borrow_date, due_date, return_date)
  VALUES ('TEST-READER-001', '9787302423287', DATE_SUB(CURDATE(), INTERVAL 30 DAY), CURDATE(), NULL);

  -- 创建due_date=昨天的未还记录（应出现在逾期列表，overdueDays=1）
  INSERT INTO borrow_records (reader_id, isbn, borrow_date, due_date, return_date)
  VALUES ('TEST-READER-001', '9787111641247', DATE_SUB(CURDATE(), INTERVAL 31 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NULL);
"

# 查询逾期列表
curl -s "http://localhost:8080/api/borrows/overdue" | jq '.data.list[] | {isbn, dueDate, overdueDays}'

# 验证：
# - due_date=今天的记录不出现
# - due_date=昨天的记录出现，overdueDays=1
```

---

## 六、测试完成标准

| 类型 | 通过标准 |
|------|----------|
| P0 用例 | 100% 通过，零失败 |
| P1 用例 | 95% 以上通过 |
| P2 用例 | 80% 以上通过 |
| 集成场景 | 5个场景全部通过 |
| 业务规则 | R1-R7 全部覆盖且通过 |
| 响应格式 | 所有接口遵循统一 `{code, message, data}` 格式 |
| 错误码 | 400/404/409/422/500 各类错误码使用正确 |

---

*文档生成时间：2026-03-05*
*下一步：Phase 4 开发阶段启动，各任务按 todo.json 依赖顺序执行*
