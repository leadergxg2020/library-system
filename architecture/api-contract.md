# API 接口契约文档

**项目**：图书馆管理系统
**基础路径**：`http://localhost:8080`
**版本**：1.0.0
**日期**：2026-03-05

---

## 统一规范

### 请求规范

- Content-Type：`application/json`（文件上传接口除外）
- 字符编码：UTF-8
- 分页参数：`pageNum`（从 1 开始）、`pageSize`（默认 10，最大 100）

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 错误码定义

| code | 含义 | 场景举例 |
|------|------|----------|
| 200 | 成功 | 操作正常完成 |
| 400 | 请求参数错误 | 字段校验失败、格式不正确 |
| 404 | 资源不存在 | 图书/读者/借阅记录找不到 |
| 409 | 业务冲突 | ISBN 重复、读者ID重复 |
| 422 | 业务规则拒绝 | 库存不足、禁借期内、超上限、有未还记录不可删除 |
| 500 | 系统错误 | 数据库异常、未预期异常 |

### 统一分页响应结构（data 字段）

```json
{
  "records": [...],
  "total": 100,
  "pageNum": 1,
  "pageSize": 10,
  "pages": 10
}
```

---

## 一、图书管理接口

### 1. POST /api/books — 新增图书

**描述**：新增一本图书。ISBN 不可重复。

**请求**

```http
POST /api/books
Content-Type: application/json

{
  "isbn": "9787111234567",
  "title": "Java编程思想",
  "author": "Bruce Eckel",
  "publisher": "机械工业出版社",
  "totalQuantity": 3
}
```

**字段校验规则**

| 字段 | 是否必填 | 规则 |
|------|----------|------|
| isbn | 是 | 10位或13位纯数字，不可重复 |
| title | 是 | 最长100字符，不可为空 |
| author | 是 | 最长50字符，不可为空 |
| publisher | 是 | 最长100字符，不可为空 |
| totalQuantity | 是 | 正整数，最小值1 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "isbn": "9787111234567",
    "title": "Java编程思想",
    "author": "Bruce Eckel",
    "publisher": "机械工业出版社",
    "totalQuantity": 3,
    "availableQuantity": 3,
    "createdAt": "2026-03-05T10:00:00",
    "updatedAt": "2026-03-05T10:00:00"
  }
}
```

**错误响应**

```json
// 400 - 字段校验失败
{
  "code": 400,
  "message": "ISBN格式不正确，应为10位或13位数字",
  "data": null
}

// 400 - totalQuantity 为零或负数
{
  "code": 400,
  "message": "总库存数量必须大于0",
  "data": null
}

// 409 - ISBN 已存在
{
  "code": 409,
  "message": "ISBN 9787111234567 已存在，如需增加库存请使用修改功能",
  "data": null
}
```

---

### 2. POST /api/books/import — 批量导入CSV

**描述**：通过上传 CSV 文件批量导入图书。ISBN 重复则累加库存，新 ISBN 则新增图书。

**请求**

```http
POST /api/books/import
Content-Type: multipart/form-data

file: [CSV文件]
```

**CSV 文件格式要求**

- 第一行为表头：`isbn,title,author,publisher,quantity`
- 表头名称严格匹配（不区分大小写）
- 每行数据对应一本书
- quantity 为正整数

**CSV 示例内容**

```csv
isbn,title,author,publisher,quantity
9787111234567,Java编程思想,Bruce Eckel,机械工业出版社,2
9787111580935,深入理解Java虚拟机,周志明,机械工业出版社,1
invalid_isbn,错误示例,,人民邮电出版社,1
```

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalRows": 3,
    "successCount": 2,
    "newCount": 1,
    "updatedCount": 1,
    "failedCount": 1,
    "failedDetails": [
      {
        "rowNumber": 3,
        "isbn": "invalid_isbn",
        "reason": "ISBN格式不正确，应为10位或13位数字"
      }
    ]
  }
}
```

**错误响应**

```json
// 400 - 未上传文件或文件为空
{
  "code": 400,
  "message": "请上传有效的CSV文件",
  "data": null
}

// 400 - 文件格式错误（表头缺失或列名不正确）
{
  "code": 400,
  "message": "CSV文件格式错误，请确认表头包含：isbn,title,author,publisher,quantity",
  "data": null
}

// 400 - 文件类型不是CSV
{
  "code": 400,
  "message": "只支持CSV格式文件",
  "data": null
}
```

---

### 3. GET /api/books — 查询图书列表

**描述**：分页查询图书列表，支持关键字搜索（书名或作者模糊匹配）。

**请求**

```http
GET /api/books?keyword=Java&pageNum=1&pageSize=10
```

**查询参数**

| 参数 | 类型 | 是否必填 | 默认值 | 说明 |
|------|------|----------|--------|------|
| keyword | String | 否 | 无 | 模糊搜索书名或作者（LIKE '%keyword%'） |
| pageNum | Integer | 否 | 1 | 页码，从1开始 |
| pageSize | Integer | 否 | 10 | 每页数量，最大100 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "isbn": "9787111234567",
        "title": "Java编程思想",
        "author": "Bruce Eckel",
        "publisher": "机械工业出版社",
        "totalQuantity": 3,
        "availableQuantity": 2,
        "createdAt": "2026-03-05T10:00:00",
        "updatedAt": "2026-03-05T10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

**错误响应**

```json
// 400 - 分页参数不合法
{
  "code": 400,
  "message": "pageSize不能超过100",
  "data": null
}
```

---

### 4. PUT /api/books/{isbn} — 修改图书信息

**描述**：修改指定 ISBN 的图书信息。ISBN 本身不可修改。

**请求**

```http
PUT /api/books/9787111234567
Content-Type: application/json

{
  "title": "Java编程思想（第4版）",
  "author": "Bruce Eckel",
  "publisher": "机械工业出版社",
  "totalQuantity": 5
}
```

**字段校验规则**

| 字段 | 是否必填 | 规则 |
|------|----------|------|
| title | 是 | 最长100字符 |
| author | 是 | 最长50字符 |
| publisher | 是 | 最长100字符 |
| totalQuantity | 是 | 正整数，最小值1；不能小于当前未还数量 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "isbn": "9787111234567",
    "title": "Java编程思想（第4版）",
    "author": "Bruce Eckel",
    "publisher": "机械工业出版社",
    "totalQuantity": 5,
    "availableQuantity": 4,
    "createdAt": "2026-03-05T10:00:00",
    "updatedAt": "2026-03-05T11:00:00"
  }
}
```

**错误响应**

```json
// 404 - ISBN 不存在
{
  "code": 404,
  "message": "图书不存在：ISBN 9787111234567",
  "data": null
}

// 422 - totalQuantity 小于当前未还数量
{
  "code": 422,
  "message": "总库存不能小于当前未还借阅数量（当前未还：2本）",
  "data": null
}

// 400 - 字段校验失败
{
  "code": 400,
  "message": "书名不能为空",
  "data": null
}
```

---

### 5. DELETE /api/books/{isbn} — 删除图书

**描述**：删除指定 ISBN 的图书。若该图书有未还记录则禁止删除。

**请求**

```http
DELETE /api/books/9787111234567
```

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误响应**

```json
// 404 - ISBN 不存在
{
  "code": 404,
  "message": "图书不存在：ISBN 9787111234567",
  "data": null
}

// 422 - 有未还借阅记录，禁止删除
{
  "code": 422,
  "message": "该图书有2本未归还，无法删除。请待所有借阅归还后再操作。",
  "data": null
}
```

---

## 二、读者管理接口

### 6. POST /api/readers — 读者注册

**描述**：新增读者信息。reader_id 由管理员录入，不可重复。

**请求**

```http
POST /api/readers
Content-Type: application/json

{
  "readerId": "R2024001",
  "name": "张三",
  "contact": "13800138001",
  "maxBorrowCount": 5
}
```

**字段校验规则**

| 字段 | 是否必填 | 规则 |
|------|----------|------|
| readerId | 是 | 非空，唯一，最长50字符 |
| name | 是 | 最长30字符，不可为空 |
| contact | 否 | 11位手机号（正则：^1[3-9]\d{9}$），可为空 |
| maxBorrowCount | 否 | 整数，范围 1-20，默认 5 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "readerId": "R2024001",
    "name": "张三",
    "contact": "13800138001",
    "maxBorrowCount": 5,
    "banUntil": null,
    "banReasonDate": null,
    "createdAt": "2026-03-05T10:00:00",
    "updatedAt": "2026-03-05T10:00:00"
  }
}
```

**错误响应**

```json
// 409 - reader_id 已存在
{
  "code": 409,
  "message": "读者证号 R2024001 已存在",
  "data": null
}

// 400 - 手机号格式错误
{
  "code": 400,
  "message": "联系方式格式不正确，请输入11位手机号",
  "data": null
}

// 400 - maxBorrowCount 超出范围
{
  "code": 400,
  "message": "最大借阅数量范围为1-20",
  "data": null
}

// 400 - 姓名为空
{
  "code": 400,
  "message": "读者姓名不能为空",
  "data": null
}
```

---

### 7. GET /api/readers — 读者列表

**描述**：分页查询读者列表，支持关键字搜索（按姓名或读者证号模糊匹配）。

**请求**

```http
GET /api/readers?keyword=张&pageNum=1&pageSize=10
```

**查询参数**

| 参数 | 类型 | 是否必填 | 默认值 | 说明 |
|------|------|----------|--------|------|
| keyword | String | 否 | 无 | 模糊搜索姓名或读者证号 |
| pageNum | Integer | 否 | 1 | 页码，从1开始 |
| pageSize | Integer | 否 | 10 | 每页数量，最大100 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "readerId": "R2024001",
        "name": "张三",
        "contact": "13800138001",
        "maxBorrowCount": 5,
        "banUntil": null,
        "banReasonDate": null,
        "currentBorrowCount": 2,
        "createdAt": "2026-03-05T10:00:00",
        "updatedAt": "2026-03-05T10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

> 注：`currentBorrowCount` 为当前未还借阅数，实时计算。

**错误响应**

```json
// 400 - 分页参数不合法
{
  "code": 400,
  "message": "pageNum必须大于0",
  "data": null
}
```

---

### 8. PUT /api/readers/{readerId} — 修改读者信息

**描述**：修改指定读者的信息。reader_id 不可修改。

**请求**

```http
PUT /api/readers/R2024001
Content-Type: application/json

{
  "name": "张三丰",
  "contact": "13900139001",
  "maxBorrowCount": 3
}
```

**字段校验规则**

| 字段 | 是否必填 | 规则 |
|------|----------|------|
| name | 是 | 最长30字符，不可为空 |
| contact | 否 | 11位手机号，可为 null 或空字符串（表示清除） |
| maxBorrowCount | 是 | 整数，范围 1-20 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "readerId": "R2024001",
    "name": "张三丰",
    "contact": "13900139001",
    "maxBorrowCount": 3,
    "banUntil": null,
    "banReasonDate": null,
    "createdAt": "2026-03-05T10:00:00",
    "updatedAt": "2026-03-05T11:00:00"
  }
}
```

**错误响应**

```json
// 404 - 读者不存在
{
  "code": 404,
  "message": "读者不存在：R2024001",
  "data": null
}

// 400 - 字段校验失败
{
  "code": 400,
  "message": "联系方式格式不正确，请输入11位手机号",
  "data": null
}
```

---

## 三、借还管理接口

### 9. POST /api/borrows — 借书

**描述**：为读者借出一本图书。依次校验读者禁借期、当前借阅上限、图书库存。所有校验通过后在事务中创建借阅记录。

**请求**

```http
POST /api/borrows
Content-Type: application/json

{
  "readerId": "R2024001",
  "bookIsbn": "9787111234567"
}
```

**字段校验规则**

| 字段 | 是否必填 | 规则 |
|------|----------|------|
| readerId | 是 | 非空 |
| bookIsbn | 是 | 非空 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "readerId": "R2024001",
    "readerName": "张三",
    "bookIsbn": "9787111234567",
    "bookTitle": "Java编程思想",
    "borrowDate": "2026-03-05",
    "dueDate": "2026-04-04",
    "returnDate": null,
    "createdAt": "2026-03-05T10:00:00"
  }
}
```

**错误响应**

```json
// 404 - 读者不存在
{
  "code": 404,
  "message": "读者不存在：R2024001",
  "data": null
}

// 404 - 图书不存在
{
  "code": 404,
  "message": "图书不存在：ISBN 9787111234567",
  "data": null
}

// 422 - 读者在禁借期内
{
  "code": 422,
  "message": "读者张三在禁借期内，禁借截止日期：2026-04-04（因2026-03-05超期还书）",
  "data": null
}

// 422 - 达到借阅上限
{
  "code": 422,
  "message": "读者张三已借阅5本，已达最大借阅上限（5本），请先归还后再借",
  "data": null
}

// 422 - 库存不足
{
  "code": 422,
  "message": "《Java编程思想》库存不足，当前可借数量：0",
  "data": null
}

// 400 - 参数为空
{
  "code": 400,
  "message": "读者证号不能为空",
  "data": null
}
```

---

### 10. POST /api/returns — 还书

**描述**：处理读者归还图书。若超期，自动更新读者禁借截止日期（还书日期 + 30天，若原禁借截止日期更晚则保留原值）。整个操作在事务中执行。

**请求**

```http
POST /api/returns
Content-Type: application/json

{
  "readerId": "R2024001",
  "bookIsbn": "9787111234567"
}
```

**字段校验规则**

| 字段 | 是否必填 | 规则 |
|------|----------|------|
| readerId | 是 | 非空 |
| bookIsbn | 是 | 非空 |

**正常响应（200）— 按时归还**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "recordId": 101,
    "readerId": "R2024001",
    "readerName": "张三",
    "bookIsbn": "9787111234567",
    "bookTitle": "Java编程思想",
    "borrowDate": "2026-03-05",
    "dueDate": "2026-04-04",
    "returnDate": "2026-03-20",
    "isOverdue": false,
    "banUntil": null,
    "message": "归还成功"
  }
}
```

**正常响应（200）— 超期归还**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "recordId": 101,
    "readerId": "R2024001",
    "readerName": "张三",
    "bookIsbn": "9787111234567",
    "bookTitle": "Java编程思想",
    "borrowDate": "2026-03-05",
    "dueDate": "2026-04-04",
    "returnDate": "2026-04-15",
    "isOverdue": true,
    "overdueDays": 11,
    "banUntil": "2026-05-15",
    "message": "归还成功，但已超期11天，禁借截止日期：2026-05-15"
  }
}
```

**错误响应**

```json
// 404 - 未找到该读者对该书的未还借阅记录
{
  "code": 404,
  "message": "未找到读者R2024001借阅ISBN 9787111234567的未还记录",
  "data": null
}

// 400 - 参数为空
{
  "code": 400,
  "message": "图书ISBN不能为空",
  "data": null
}
```

---

### 11. GET /api/borrows/overdue — 逾期列表

**描述**：查询所有当前逾期未还的借阅记录（return_date IS NULL AND due_date < 今天）。支持分页。

**请求**

```http
GET /api/borrows/overdue?pageNum=1&pageSize=10
```

**查询参数**

| 参数 | 类型 | 是否必填 | 默认值 | 说明 |
|------|------|----------|--------|------|
| pageNum | Integer | 否 | 1 | 页码，从1开始 |
| pageSize | Integer | 否 | 10 | 每页数量，最大100 |

**正常响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "recordId": 101,
        "readerId": "R2024001",
        "readerName": "张三",
        "readerContact": "13800138001",
        "bookIsbn": "9787111234567",
        "bookTitle": "Java编程思想",
        "borrowDate": "2026-01-01",
        "dueDate": "2026-01-31",
        "overdueDays": 33,
        "returnDate": null
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

> 注：`overdueDays` = 今天 - due_date（天数，实时计算）

**错误响应**

```json
// 400 - 分页参数不合法
{
  "code": 400,
  "message": "pageNum必须大于0",
  "data": null
}
```

---

## 附录：VO 类型定义汇总

### BookVO

```typescript
interface BookVO {
  id: number;
  isbn: string;
  title: string;
  author: string;
  publisher: string;
  totalQuantity: number;
  availableQuantity: number;   // 计算值：totalQuantity - 未还记录数
  createdAt: string;           // ISO 8601 格式
  updatedAt: string;
}
```

### ReaderVO

```typescript
interface ReaderVO {
  readerId: string;
  name: string;
  contact: string | null;
  maxBorrowCount: number;
  banUntil: string | null;        // 格式：yyyy-MM-dd
  banReasonDate: string | null;   // 格式：yyyy-MM-dd
  currentBorrowCount: number;     // 计算值：当前未还借阅数
  createdAt: string;
  updatedAt: string;
}
```

### BorrowRecordVO

```typescript
interface BorrowRecordVO {
  id: number;
  readerId: string;
  readerName: string;
  bookIsbn: string;
  bookTitle: string;
  borrowDate: string;    // 格式：yyyy-MM-dd
  dueDate: string;       // 格式：yyyy-MM-dd
  returnDate: string | null;
  createdAt: string;
}
```

### ReturnResultVO

```typescript
interface ReturnResultVO {
  recordId: number;
  readerId: string;
  readerName: string;
  bookIsbn: string;
  bookTitle: string;
  borrowDate: string;
  dueDate: string;
  returnDate: string;
  isOverdue: boolean;
  overdueDays?: number;   // 仅 isOverdue=true 时有值
  banUntil: string | null;
  message: string;
}
```

### ImportResultVO

```typescript
interface ImportResultVO {
  totalRows: number;
  successCount: number;
  newCount: number;
  updatedCount: number;
  failedCount: number;
  failedDetails: Array<{
    rowNumber: number;
    isbn: string;
    reason: string;
  }>;
}
```

### OverdueRecordVO

```typescript
interface OverdueRecordVO {
  recordId: number;
  readerId: string;
  readerName: string;
  readerContact: string | null;
  bookIsbn: string;
  bookTitle: string;
  borrowDate: string;
  dueDate: string;
  overdueDays: number;
  returnDate: null;
}
```

---

*文档版本：1.0.0 | 最后更新：2026-03-05*
