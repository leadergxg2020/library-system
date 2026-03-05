# 系统架构设计文档

**项目**：图书馆管理系统
**版本**：1.0.0
**日期**：2026-03-05
**架构师**：AI Architect

---

## 1. 技术选型

### 1.1 最终技术栈清单

| 层次 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.2.x | 当前主流 LTS 版本 |
| ORM 框架 | MyBatis-Plus | 3.5.x | 注解模式，减少 XML |
| Java 版本 | Java | 17 (LTS) | Spring Boot 3.x 最低要求 |
| 构建工具 | Maven | 3.9.x | Java 生态标准构建工具 |
| 代码简化 | Lombok | 1.18.x | 减少样板代码 |
| 数据库 | MySQL | 8.0 | 项目约束，成熟稳定 |
| 前端框架 | Vue | 3.4.x | 当前稳定版，Composition API |
| 构建工具 | Vite | 5.x | 极速冷启动，HMR 体验优秀 |
| 语言 | TypeScript | 5.x | 类型安全，减少运行时错误 |
| UI 组件库 | Element Plus | 2.x | 专为 Vue 3 设计，管理后台首选 |
| 状态管理 | Pinia | 2.x | Vue 3 官方推荐，替代 Vuex |
| 路由 | Vue Router | 4.x | Vue 3 配套路由 |
| HTTP 客户端 | Axios | 1.x | 成熟稳定，支持拦截器 |
| 数据库连接池 | HikariCP | 内置于 Spring Boot | Spring Boot 默认，性能最优 |
| 日志 | SLF4J + Logback | 内置于 Spring Boot | 行业标准日志框架 |
| CSV 解析 | Apache Commons CSV | 1.10.x | Apache 出品，成熟稳定 |

### 1.2 前端选型理由

**Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router 4 + Axios**

- **Vue 3**：Composition API 使逻辑复用更清晰；相比 React 上手成本更低；适合中小型管理后台
- **Vite**：基于 ES Module 的原生 HMR，开发启动速度远超 Webpack；本地开发体验极佳
- **TypeScript**：在编译期捕获类型错误，减少运行时 bug；与 Vue 3 的类型支持深度集成
- **Element Plus**：专为 Vue 3 重写，组件丰富（Table、Form、Dialog、Upload 等均有）；适合数据密集型管理界面；文档完善
- **Pinia**：Vue 3 官方推荐状态管理，比 Vuex 4 API 更简洁；支持 TypeScript 自动推断
- **Axios**：请求/响应拦截器支持统一处理认证、错误码；浏览器兼容性好

**放弃的选项：**
- React + Ant Design Pro：学习曲线更陡，对单开发者维护成本更高
- Nuxt.js：SSR 对本项目无必要，增加不必要复杂度

### 1.3 后端选型理由

**Spring Boot 3.x + MyBatis-Plus（注解模式）+ Lombok + Maven**

- **Spring Boot 3.x**：用户明确要求 Java 后端；Spring Boot 3 基于 Spring 6，原生 GraalVM 支持；3.2 为当前主流稳定版
- **MyBatis-Plus（注解模式）**：相比 JPA/Hibernate，对复杂 SQL 控制更精准；注解模式避免 XML 文件分散维护；内置分页、CRUD 能力减少样板代码
- **Lombok**：@Data、@Builder、@RequiredArgsConstructor 大幅减少实体类和构造器样板代码
- **Maven**：Java 生态最成熟的构建工具，依赖管理稳定可靠

**放弃的选项：**
- JPA/Hibernate：对本项目的复杂业务查询（逾期查询、库存计算）灵活性不足；HQL 调试困难
- Gradle：对本项目无额外收益，团队熟悉度不如 Maven

### 1.4 为什么不引入缓存（Redis）

本项目为**本地部署、单管理员、无高并发**场景：
- 并发用户数：1
- 数据集规模：图书馆规模（预计数千条记录）
- MySQL 8.0 的查询性能对本项目完全足够（千条记录的分页查询 < 10ms）

引入 Redis 会带来：
- 额外的数据一致性维护成本（缓存失效逻辑）
- 本地环境需额外启动 Redis 进程
- 代码复杂度提升，维护成本增加

**结论：不引入 Redis，过度设计。**

### 1.5 为什么不引入 Spring Security

PRD 明确本期**不做登录认证**，系统直接进入管理界面（本地部署，单管理员）：
- 引入 Spring Security 但不配置认证，反而需要额外配置"放行所有请求"，增加配置复杂度
- 本期需求无认证、无授权、无多角色，引入毫无收益

**结论：不引入 Spring Security。若未来需要认证，作为独立迭代加入。**

---

## 2. 系统架构

### 2.1 整体分层架构

```
┌─────────────────────────────────────────────┐
│              浏览器（Chrome）                 │
│         Vue 3 SPA (Vite + TypeScript)        │
│   Pages → Components → API层 → Store(Pinia) │
└───────────────────┬─────────────────────────┘
                    │ HTTP/JSON (REST API)
                    │ 端口: 8080
┌───────────────────▼─────────────────────────┐
│         Spring Boot REST API 服务            │
│  Controller → Service → Mapper → DB         │
│              端口: 8080                      │
└───────────────────┬─────────────────────────┘
                    │ JDBC (HikariCP 连接池)
┌───────────────────▼─────────────────────────┐
│              MySQL 8.0                       │
│         端口: 3306 (本地)                    │
│  t_book | t_reader | t_borrow_record        │
└─────────────────────────────────────────────┘
```

### 2.2 后端分层架构

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────────┐
│  Controller 层（com.library.controller）     │
│  职责：                                      │
│  - 接收 HTTP 请求，解析路径参数、请求体       │
│  - 调用 @Valid 触发参数校验                  │
│  - 调用 Service 层方法                       │
│  - 将 Service 返回值包装为 Result<T> 响应    │
│  禁止：写业务逻辑、直接操作数据库            │
└───────────────────┬─────────────────────────┘
                    │ 调用 Service 接口
                    ▼
┌─────────────────────────────────────────────┐
│  Service 层（com.library.service）           │
│  职责：                                      │
│  - 业务逻辑的唯一实现层                      │
│  - 业务规则校验（禁借期、库存、上限）         │
│  - 事务边界（@Transactional）               │
│  - 协调多个 Mapper 完成复合操作              │
│  - 抛出 BusinessException 传递业务错误       │
│  禁止：处理 HTTP 细节、直接返回 HTTP 状态码  │
└───────────────────┬─────────────────────────┘
                    │ 调用 Mapper 接口
                    ▼
┌─────────────────────────────────────────────┐
│  Mapper 层（com.library.mapper）             │
│  职责：                                      │
│  - 数据库 CRUD 操作（全注解，无 XML）         │
│  - 使用 MyBatis-Plus 内置方法或 @Select 注解 │
│  - 参数化查询，防 SQL 注入                   │
│  禁止：写业务逻辑、事务控制                  │
└───────────────────┬─────────────────────────┘
                    │ JDBC
                    ▼
              MySQL 8.0 数据库
```

### 2.3 前端分层架构

```
用户操作
     │
     ▼
┌─────────────────────────────────────────────┐
│  Pages（src/views/）                         │
│  职责：页面级组件，处理路由、页面状态         │
│  BookList.vue / ReaderList.vue 等            │
└───────────────────┬─────────────────────────┘
                    │ 使用
                    ▼
┌─────────────────────────────────────────────┐
│  Components（src/components/）               │
│  职责：可复用 UI 组件，不含业务逻辑           │
│  BookForm.vue / OverdueTable.vue 等          │
└───────────────────┬─────────────────────────┘
                    │ 调用
                    ▼
┌─────────────────────────────────────────────┐
│  API 层（src/api/）                          │
│  职责：封装所有 axios 请求，定义 TS 类型      │
│  book.ts / reader.ts / borrow.ts             │
│  禁止：在组件内直接调用 axios                │
└───────────────────┬─────────────────────────┘
                    │ 更新/读取
                    ▼
┌─────────────────────────────────────────────┐
│  Store（src/stores/ - Pinia）                │
│  职责：跨组件共享状态，如分页参数、筛选条件   │
└─────────────────────────────────────────────┘
```

### 2.4 核心业务数据流

#### 借书操作完整流程

```
前端 BorrowCreate.vue
  │  POST /api/borrows { readerId, bookIsbn }
  ▼
BorrowController.createBorrow()
  │  @Valid 校验 readerId/bookIsbn 非空
  ▼
BorrowService.borrowBook(readerId, bookIsbn)  ← @Transactional
  │
  ├─ 1. ReaderMapper.selectById(readerId)
  │     └─ 若不存在 → throw BusinessException(404, "读者不存在")
  │
  ├─ 2. 校验禁借期
  │     └─ reader.banUntil != null && reader.banUntil >= today
  │        → throw BusinessException(422, "读者在禁借期内，截止 {date}")
  │
  ├─ 3. BookMapper.selectByIsbn(bookIsbn)
  │     └─ 若不存在 → throw BusinessException(404, "图书不存在")
  │
  ├─ 4. 计算可借数量
  │     └─ BorrowRecordMapper.countUnreturned(bookIsbn)
  │        availableQty = book.totalQuantity - unreturnedCount
  │        若 availableQty <= 0 → throw BusinessException(422, "库存不足")
  │
  ├─ 5. 校验借阅上限
  │     └─ BorrowRecordMapper.countUnreturnedByReader(readerId)
  │        若 count >= reader.maxBorrowCount
  │        → throw BusinessException(422, "已达借阅上限")
  │
  └─ 6. BorrowRecordMapper.insert(new BorrowRecord {
           readerId, bookIsbn,
           borrowDate = today,
           dueDate = today + 30天
         })
         → 返回 BorrowRecordVO

  ← 事务提交
  ▼
BorrowController → Result.success(borrowRecordVO)
  ▼
前端收到 200，刷新借阅列表
```

#### 还书操作完整流程

```
前端 ReturnCreate.vue
  │  POST /api/returns { readerId, bookIsbn }
  ▼
BorrowController.returnBook()
  │  @Valid 校验参数非空
  ▼
BorrowService.returnBook(readerId, bookIsbn)  ← @Transactional
  │
  ├─ 1. 查找未还借阅记录
  │     BorrowRecordMapper.findUnreturnedRecord(readerId, bookIsbn)
  │     └─ 若不存在 → throw BusinessException(404, "未找到未还借阅记录")
  │
  ├─ 2. 设置还书日期
  │     returnDate = today
  │     BorrowRecordMapper.setReturnDate(recordId, returnDate)
  │
  ├─ 3. 判断是否超期
  │     若 returnDate > record.dueDate（超期）：
  │     │
  │     ├─ 计算新禁借截止日 newBanUntil = returnDate + 30天
  │     │
  │     └─ ReaderMapper.updateBanUntil(readerId, newBanUntil, returnDate)
  │          -- 业务规则：若原 ban_until 更晚则保留原值
  │          UPDATE t_reader SET
  │            ban_until = GREATEST(COALESCE(ban_until, '0000-01-01'), newBanUntil),
  │            ban_reason_date = returnDate
  │          WHERE reader_id = ?
  │          AND (ban_until IS NULL OR ban_until < newBanUntil)
  │
  └─ 4. 返回还书结果（含是否超期、是否触发禁借）

  ← 事务提交
  ▼
BorrowController → Result.success(returnResultVO)
  ▼
前端收到 200，展示还书结果提示（超期警告/禁借通知）
```

---

## 3. 安全架构

### 3.1 认证与授权

本期**不做登录认证**，系统直接进入管理界面。

理由：
- 本地部署，系统仅在本机运行，无外网暴露
- 单管理员操作，无多用户场景
- PRD 明确本期不做认证

未来扩展路径：若需认证，增加 Spring Security + JWT，作为独立迭代。

### 3.2 SQL 注入防护

- MyBatis-Plus 所有查询均使用**参数化查询**（PreparedStatement）
- 自定义 @Select 注解 SQL 使用 #{param} 占位符，禁止 ${} 字符串拼接
- 禁止在代码中拼接 SQL 字符串

### 3.3 输入校验

**后端校验（权威层）：**
- 使用 Spring Validation（@Valid + @NotBlank、@Size、@Pattern 等）
- Controller 入参 DTO 全部加注解校验
- @RestControllerAdvice 统一捕获 MethodArgumentNotValidException，返回 400

**前端校验（用户体验层）：**
- Element Plus Form 表单校验，减少无效请求
- 前端校验不作为安全防线，所有安全决策在后端执行

### 3.4 数据安全

- 联系方式（手机号）不脱敏展示（内部系统，管理员可见全部信息）
- 不存储任何密码或敏感密钥
- 数据库连接信息存放在 application.yml（不提交到版本控制中含密码的配置）

---

## 4. 非功能设计

### 4.1 分页规范

所有列表接口统一分页参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| pageNum | Integer | 1 | 页码，从 1 开始 |
| pageSize | Integer | 10 | 每页数量，最大 100 |
| keyword | String | - | 关键字搜索（可选） |

统一分页响应结构：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

### 4.2 日志规范

- 日志框架：SLF4J + Logback（Spring Boot 内置）
- 日志级别：开发环境 DEBUG，生产环境 INFO
- 关键业务操作必须记录 INFO 日志：
  - 借书成功/失败
  - 还书成功/失败（含超期判断结果）
  - CSV 导入完成（含成功/失败数量）
  - 图书删除
- 日志格式：`[操作类型] [关键标识] 操作结果 - 详情`
- 示例：`[BORROW] readerId=R001, isbn=9787111234567 - SUCCESS, dueDate=2026-04-04`

### 4.3 CSV 导入设计

- 使用 Apache Commons CSV 解析 CSV 文件
- 采用**逐行解析 + 批量入库**策略：
  - 批大小：100 行
  - 每批使用 MyBatis-Plus `saveBatch()` 方法
- 处理逻辑：
  - 读取 CSV 头部，校验列名（isbn、title、author、publisher、quantity）
  - 逐行解析，跳过格式错误行，记录错误信息
  - ISBN 已存在：`UPDATE t_book SET total_quantity = total_quantity + ? WHERE isbn = ?`
  - ISBN 不存在：新增记录
  - 返回 ImportResultVO（成功数、新增数、更新数、失败数、失败原因列表）
- 性能目标：1000 行 CSV ≤ 30 秒（单批 100 行，10 批，预计 < 5 秒）

### 4.4 性能设计

| 指标 | 目标 | 设计保障 |
|------|------|----------|
| 页面加载 | ≤ 3 秒 | 分页查询 + 合理索引 + HikariCP 连接池 |
| 借还操作响应 | ≤ 2 秒 | 事务内操作 ≤ 3 次 SQL，均走索引 |
| CSV 导入 1000 行 | ≤ 30 秒 | 批量入库，批大小 100 |

---

## 5. 架构决策记录（ADR）

### ADR-001：后端技术栈选型

**日期**：2026-03-05
**状态**：已确定

**决策**：采用 Spring Boot 3.2.x + MyBatis-Plus 3.5.x（注解模式）+ Lombok + Maven

**背景**：用户明确要求 Java 后端，需选择具体框架组合。

**理由**：
1. Spring Boot 3.x 为当前主流 LTS 版本，社区活跃，生态完整
2. MyBatis-Plus 注解模式减少 XML 文件维护成本，对本项目中等复杂度 SQL 完全够用
3. Lombok 减少实体类样板代码，提升开发效率
4. Maven 为 Java 生态最成熟的构建工具

**放弃的选项**：
- JPA/Hibernate：复杂查询（逾期统计、库存计算）灵活性不足，HQL 调试困难
- Gradle：无额外收益，对本项目过于复杂

**影响**：Mapper 层全注解，禁止 XML 文件；实体类必须加 Lombok 注解

---

### ADR-002：前端框架选型

**日期**：2026-03-05
**状态**：已确定

**决策**：采用 Vue 3.4 + Vite 5 + TypeScript 5 + Element Plus 2 + Pinia 2 + Vue Router 4 + Axios 1

**背景**：项目约束推荐此组合，需确认并记录理由。

**理由**：
1. Vue 3 Composition API 逻辑复用清晰，适合中小型管理后台
2. Vite 开发体验极佳，HMR 近乎即时
3. Element Plus 组件覆盖所有需求场景（Table、Form、Upload、DatePicker）
4. TypeScript 提供类型安全，与 Vue 3 深度集成
5. Pinia 为 Vue 3 官方推荐，API 比 Vuex 更简洁

**放弃的选项**：
- React + Ant Design Pro：学习成本更高，对单开发者维护性更差
- Nuxt.js：SSR 对本项目无必要，增加不必要复杂度

**影响**：所有 API 调用封装在 src/api/，禁止组件内直接 axios 调用

---

### ADR-003：不引入缓存（Redis）

**日期**：2026-03-05
**状态**：已确定

**决策**：不引入 Redis 或任何缓存层

**背景**：评估本项目是否需要缓存加速。

**理由**：
1. 本地部署，单用户，无并发压力
2. 数据集规模小（图书馆级别，数千条记录），MySQL 直查足够
3. 引入缓存需要处理缓存失效、数据一致性问题，成本远超收益
4. 本地环境需额外维护 Redis 进程，增加运维复杂度

**影响**：无缓存层，所有查询直接走 MySQL；依赖合理索引保障性能

---

### ADR-004：不引入登录认证

**日期**：2026-03-05
**状态**：已确定

**决策**：本期不引入 Spring Security，不实现登录认证

**背景**：PRD 明确本期不做登录认证。

**理由**：
1. PRD 明确要求本期不做认证，系统直接进入管理界面
2. 本地部署，无外网暴露，安全风险可接受
3. 单管理员操作，无多角色需求
4. 引入 Spring Security 但全部放行，反而增加无效配置复杂度

**未来路径**：若需认证，以 Spring Security + JWT Token 方式在新迭代加入

**影响**：所有 API 无需鉴权；不引入 spring-boot-starter-security 依赖

---

### ADR-005：可借数量作为计算值，不单独存储

**日期**：2026-03-05
**状态**：已确定

**决策**：可借数量 = total_quantity - 未还记录数，实时计算，不在 t_book 中存储 available_quantity 字段

**背景**：评估是否需要在图书表中维护 available_quantity 字段。

**理由**：
1. 存储冗余字段需要在每次借书/还书时同步更新，引入数据一致性风险
2. 单用户、无高并发，实时计算不存在性能问题
3. 借还操作已在事务中，计算值始终准确
4. 减少了一个需要维护的字段，代码更简洁

**影响**：
- 图书列表 API 需要 JOIN 或子查询计算可借数量
- 借书操作需要先查询未还记录数再判断库存
- `SELECT COUNT(*) FROM t_borrow_record WHERE book_isbn = ? AND return_date IS NULL` 需要有索引

---

*文档版本：1.0.0 | 最后更新：2026-03-05*
