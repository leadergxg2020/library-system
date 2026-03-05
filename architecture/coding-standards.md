# 编码规范与目录结构

**项目**：图书馆管理系统
**版本**：1.0.0
**日期**：2026-03-05

---

## 1. 项目目录结构

```
library-system/
├── backend/                                    # Spring Boot 后端（单 Maven 模块）
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── library/
│       │   │           ├── LibraryApplication.java        # Spring Boot 启动类
│       │   │           ├── controller/                    # REST 控制器层
│       │   │           │   ├── BookController.java
│       │   │           │   ├── ReaderController.java
│       │   │           │   └── BorrowController.java
│       │   │           ├── service/                       # 业务逻辑层（直接使用具体类，无接口）
│       │   │           │   ├── BookService.java
│       │   │           │   ├── ReaderService.java
│       │   │           │   └── BorrowService.java
│       │   │           ├── mapper/                        # MyBatis-Plus Mapper 层（全注解）
│       │   │           │   ├── BookMapper.java
│       │   │           │   ├── ReaderMapper.java
│       │   │           │   └── BorrowRecordMapper.java
│       │   │           ├── entity/                        # 数据库实体（PO）
│       │   │           │   ├── BookPO.java
│       │   │           │   ├── ReaderPO.java
│       │   │           │   └── BorrowRecordPO.java
│       │   │           ├── dto/                           # 请求数据对象（入参）
│       │   │           │   ├── BookCreateDTO.java
│       │   │           │   ├── BookUpdateDTO.java
│       │   │           │   ├── ReaderCreateDTO.java
│       │   │           │   ├── ReaderUpdateDTO.java
│       │   │           │   └── BorrowRequest.java         # 借书/还书共用（字段相同：readerId + bookIsbn）
│       │   │           ├── vo/                            # 响应视图对象（出参，仅在与PO有实质差异时创建）
│       │   │           │   ├── BookVO.java
│       │   │           │   ├── ReaderVO.java
│       │   │           │   ├── BorrowSuccessVO.java       # 借书成功响应（含跨表 readerName/bookTitle）
│       │   │           │   ├── ReturnResultVO.java
│       │   │           │   ├── OverdueRecordVO.java
│       │   │           │   └── ImportResultVO.java
│       │   │           ├── common/                        # 公共组件
│       │   │           │   ├── Result.java                # 统一响应体
│       │   │           │   ├── PageResult.java            # 分页响应体
│       │   │           │   ├── exception/
│       │   │           │   │   ├── BusinessException.java # 业务异常
│       │   │           │   │   └── GlobalExceptionHandler.java # 全局异常处理
│       │   │           │   └── enums/
│       │   │           │       └── ErrorCode.java         # 错误码枚举
│       │   │           └── config/                        # 配置类
│       │   │               ├── MybatisPlusConfig.java     # MyBatis-Plus 配置（分页插件）
│       │   │               └── WebMvcConfig.java          # CORS 配置
│       │   └── resources/
│       │       ├── application.yml                        # 主配置文件
│       │       └── logback-spring.xml                     # 日志配置（可选）
│       └── test/
│           └── java/
│               └── com/
│                   └── library/
│                       └── service/                       # 单元测试
│                           ├── BookServiceTest.java
│                           ├── ReaderServiceTest.java
│                           └── BorrowServiceTest.java
│
└── frontend/                                   # Vue 3 前端
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    ├── index.html
    └── src/
        ├── main.ts                             # 入口文件
        ├── App.vue                             # 根组件
        ├── views/                              # 页面级组件（路由对应的页面）
        │   ├── BookList.vue                    # 图书列表页
        │   ├── ReaderList.vue                  # 读者列表页
        │   ├── BorrowManage.vue                # 借还管理页
        │   └── OverdueList.vue                 # 逾期列表页
        ├── components/                         # 可复用 UI 组件
        │   ├── BookForm.vue                    # 图书表单（新增/编辑）
        │   ├── ReaderForm.vue                  # 读者表单（新增/编辑）
        │   ├── BorrowForm.vue                  # 借书表单
        │   ├── ReturnForm.vue                  # 还书表单
        │   └── ImportDialog.vue                # CSV 导入对话框
        ├── api/                                # Axios 请求封装（禁止在组件内直接调用 axios）
        │   ├── request.ts                      # Axios 实例 + 拦截器
        │   ├── book.ts                         # 图书相关 API
        │   ├── reader.ts                       # 读者相关 API
        │   └── borrow.ts                       # 借还相关 API
        ├── stores/                             # Pinia 状态管理
        │   ├── book.ts
        │   └── reader.ts
        ├── router/                             # Vue Router 路由配置
        │   └── index.ts
        ├── types/                              # TypeScript 类型定义
        │   └── api.ts                          # API 请求/响应类型
        └── utils/                              # 工具函数
            └── format.ts                       # 日期格式化等工具
```

---

## 2. 后端命名规范

### 2.1 包名规范

| 包 | 规范 |
|----|------|
| 根包 | `com.library` |
| 控制器 | `com.library.controller` |
| 服务层 | `com.library.service` |
| Mapper | `com.library.mapper` |
| 实体 | `com.library.entity` |
| DTO | `com.library.dto` |
| VO | `com.library.vo` |
| 公共组件 | `com.library.common` |
| 配置 | `com.library.config` |

规则：**全小写**，用点分隔，禁止大写字母或下划线。

### 2.2 类名规范

| 分类 | 后缀 | 示例 |
|------|------|------|
| 数据库实体 | PO | `BookPO`、`ReaderPO`、`BorrowRecordPO` |
| Mapper 接口 | Mapper | `BookMapper`、`ReaderMapper`、`BorrowRecordMapper` |
| Service 类 | Service | `BookService`、`ReaderService`、`BorrowService` |
| Controller | Controller | `BookController`、`ReaderController`、`BorrowController` |
| 请求 DTO | DTO（语义前缀） | `BookCreateDTO`、`BookUpdateDTO`、`ReaderCreateDTO`、`ReaderUpdateDTO`、`BorrowRequest`（借书/还书共用） |
| 响应 VO | VO（语义前缀） | `BookVO`、`ReaderVO`、`BorrowSuccessVO`、`ReturnResultVO`、`OverdueRecordVO`、`ImportResultVO` |
| 异常类 | Exception | `BusinessException` |
| 全局处理 | Handler/Advice | `GlobalExceptionHandler` |
| 配置类 | Config | `MybatisPlusConfig`、`WebMvcConfig` |

### 2.3 方法名规范

| 操作 | 命名模式 | 示例 |
|------|----------|------|
| 新增 | `create` / `save` | `createBook`, `saveReader` |
| 查询单个 | `getBy` + 条件 | `getBookByIsbn`, `getReaderById` |
| 查询列表 | `list` + 条件 | `listBooks`, `listReaders` |
| 修改 | `update` + 目标 | `updateBook`, `updateReader` |
| 删除 | `delete` + 目标 | `deleteBook` |
| 业务操作 | 动词 + 名词 | `borrowBook`, `returnBook`, `importBooks` |

---

## 3. 实体类规范（PO）

实体类必须完整包含以下注解，格式如下：

```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_book")
public class BookPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** ISBN，唯一，10位或13位数字 */
    private String isbn;

    /** 书名，最长100字符 */
    private String title;

    /** 作者，最长50字符 */
    private String author;

    /** 出版社，最长100字符 */
    private String publisher;

    /** 总库存数量（可借数量为计算值） */
    private Integer totalQuantity;

    /** 创建时间，由 MyBatis-Plus 自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，由 MyBatis-Plus 自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_reader")
public class ReaderPO {

    @TableId(type = IdType.INPUT)   // reader_id 由外部输入，非自增
    private String readerId;

    /** 读者姓名，最长30字符 */
    private String name;

    /** 联系方式（手机号），可为空 */
    private String contact;

    /** 最大借阅数量，默认5 */
    private Integer maxBorrowCount;

    /** 禁借截止日期，NULL 表示无禁借 */
    private LocalDate banUntil;

    /** 导致禁借的超期还书日期（用于展示原因） */
    private LocalDate banReasonDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_borrow_record")
public class BorrowRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 读者证号，外键关联 t_reader.reader_id */
    private String readerId;

    /** ISBN，外键关联 t_book.isbn */
    private String bookIsbn;

    /** 借出日期 */
    private LocalDate borrowDate;

    /** 应还日期 = borrowDate + 30天 */
    private LocalDate dueDate;

    /** 实际归还日期，NULL 表示未还 */
    private LocalDate returnDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

**MyBatis-Plus 自动填充处理器（必须实现）：**

```java
package com.library.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }
}
```

---

## 4. Mapper 规范

**强制要求：全注解模式，禁止 XML 文件。**

```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BookPO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface BookMapper extends BaseMapper<BookPO> {

    /**
     * 按关键字分页查询（书名或作者模糊匹配）
     * 同时统计每本书的可借数量（total_quantity - 未还数）
     */
    @Select("""
        SELECT b.*,
               (b.total_quantity - COALESCE(
                   (SELECT COUNT(*) FROM t_borrow_record br
                    WHERE br.book_isbn = b.isbn AND br.return_date IS NULL), 0
               )) AS available_quantity
        FROM t_book b
        WHERE (#{keyword} IS NULL OR #{keyword} = ''
               OR b.title LIKE CONCAT('%', #{keyword}, '%')
               OR b.author LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY b.created_at DESC
        """)
    Page<BookVO> selectPageWithAvailable(Page<?> page, @Param("keyword") String keyword);

    /**
     * 统计某书的未还借阅数量（用于库存计算和删除前校验）
     */
    @Select("SELECT COUNT(*) FROM t_borrow_record WHERE book_isbn = #{isbn} AND return_date IS NULL")
    int countUnreturned(@Param("isbn") String isbn);
}
```

```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowRecordPO;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecordPO> {

    /**
     * 查询某读者借某书的未还记录（还书操作使用）
     */
    @Select("""
        SELECT * FROM t_borrow_record
        WHERE reader_id = #{readerId}
          AND book_isbn = #{bookIsbn}
          AND return_date IS NULL
        LIMIT 1
        """)
    BorrowRecordPO findUnreturnedRecord(@Param("readerId") String readerId,
                                        @Param("bookIsbn") String bookIsbn);

    /**
     * 统计某读者当前未还借阅数量（借书上限校验）
     */
    @Select("SELECT COUNT(*) FROM t_borrow_record WHERE reader_id = #{readerId} AND return_date IS NULL")
    int countUnreturnedByReader(@Param("readerId") String readerId);

    /**
     * 设置还书日期
     */
    @Update("UPDATE t_borrow_record SET return_date = #{returnDate} WHERE id = #{id}")
    int setReturnDate(@Param("id") Long id, @Param("returnDate") LocalDate returnDate);
}
```

```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.ReaderPO;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;

@Mapper
public interface ReaderMapper extends BaseMapper<ReaderPO> {

    /**
     * 更新读者禁借信息（还书超期时调用）
     * 业务规则：若新禁借截止日期比原来更晚，才更新；否则保留原值
     */
    @Update("""
        UPDATE t_reader
        SET ban_until = GREATEST(COALESCE(ban_until, '0001-01-01'), #{newBanUntil}),
            ban_reason_date = CASE
                WHEN ban_until IS NULL OR ban_until < #{newBanUntil} THEN #{banReasonDate}
                ELSE ban_reason_date
            END,
            updated_at = NOW()
        WHERE reader_id = #{readerId}
        """)
    int updateBanInfo(@Param("readerId") String readerId,
                      @Param("newBanUntil") LocalDate newBanUntil,
                      @Param("banReasonDate") LocalDate banReasonDate);
}
```

**Mapper 规范要点：**
- 所有自定义 SQL 使用 `#{param}` 占位符，**禁止 `${param}` 字符串拼接**
- 复杂 SQL 使用 Java Text Block（`"""`）提高可读性
- BaseMapper 已提供 CRUD 基础方法（selectById、insert、updateById、deleteById），不重复定义

---

## 5. Service 规范

> **决策（ADR-006）：** 本项目 Service 层直接使用 `@Service` 具体类，不引入接口层。
> 理由：本项目各业务服务只有单一实现，接口无多态需求；`@Transactional` 通过 CGLIB 代理在具体类上同样生效。

```java
package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.common.exception.BusinessException;
import com.library.dto.BookCreateDTO;
import com.library.dto.BookUpdateDTO;
import com.library.entity.BookPO;
import com.library.mapper.BookMapper;
import com.library.vo.BookVO;
import com.library.vo.ImportResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor   // 构造器注入（对 final 字段生成构造器）
public class BookService {

    private final BookMapper bookMapper;

    @Transactional(rollbackFor = Exception.class)
    public BookVO createBook(BookCreateDTO dto) {
        // 校验 ISBN 唯一性
        if (bookMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BookPO>()
                    .eq(BookPO::getIsbn, dto.getIsbn())) != null) {
            throw new BusinessException(409, "ISBN " + dto.getIsbn() + " 已存在，如需增加库存请使用修改功能");
        }
        BookPO book = BookPO.builder()
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .publisher(dto.getPublisher())
                .totalQuantity(dto.getTotalQuantity())
                .build();
        bookMapper.insert(book);
        log.info("[BOOK_CREATE] isbn={}, title={} - SUCCESS", dto.getIsbn(), dto.getTitle());
        return toVO(book, 0);
    }

    @Transactional(readOnly = true)
    public PageResult<BookVO> listBooks(String keyword, int pageNum, int pageSize) {
        // 查询逻辑
        Page<BookVO> page = bookMapper.selectPageWithAvailable(new Page<>(pageNum, pageSize), keyword);
        return PageResult.of(page);
    }

    // ... 其他方法
}
```

**Service 规范要点：**
- 依赖注入：使用构造器注入（`@RequiredArgsConstructor` + `final` 字段），**禁止 `@Autowired` 字段注入**
- 事务：所有写操作（借书、还书、CSV 导入、新增/修改/删除图书/读者）加 `@Transactional(rollbackFor = Exception.class)`
- 只读操作加 `@Transactional(readOnly = true)` 优化性能
- 业务异常：统一抛出 `BusinessException(code, message)`，不在 Service 层 catch 后吞掉
- 日志：关键业务操作（借书/还书/CSV 导入/图书删除）记录 INFO 日志

## 6. Controller 规范

```java
package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.BookCreateDTO;
import com.library.dto.BookUpdateDTO;
import com.library.service.BookService;  // 直接注入具体类
import com.library.vo.BookVO;
import com.library.vo.ImportResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public Result<BookVO> createBook(@Valid @RequestBody BookCreateDTO dto) {
        return Result.success(bookService.createBook(dto));
    }

    @PostMapping("/import")
    public Result<ImportResultVO> importBooks(@RequestParam("file") MultipartFile file) {
        return Result.success(bookService.importBooks(file));
    }

    @GetMapping
    public Result<PageResult<BookVO>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookService.listBooks(keyword, pageNum, pageSize));
    }

    @PutMapping("/{isbn}")
    public Result<BookVO> updateBook(@PathVariable String isbn,
                                     @Valid @RequestBody BookUpdateDTO dto) {
        return Result.success(bookService.updateBook(isbn, dto));
    }

    @DeleteMapping("/{isbn}")
    public Result<Void> deleteBook(@PathVariable String isbn) {
        bookService.deleteBook(isbn);
        return Result.success(null);
    }
}
```

**Controller 规范要点：**
- **禁止在 Controller 层写任何业务逻辑**（判断、计算、数据库操作）
- 职责：接收请求 → @Valid 校验 → 调用 Service → 包装 Result 返回
- 所有方法返回 `Result<T>` 统一响应体
- 路径参数、查询参数、请求体分别使用 `@PathVariable`、`@RequestParam`、`@RequestBody`

---

## 7. 统一响应体（Result）

```java
package com.library.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> Result<T> fail(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
```

---

## 8. 分页响应体（PageResult）

```java
package com.library.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private Long pages;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}
```

---

## 9. 统一异常处理

### 9.1 业务异常类

```java
package com.library.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

### 9.2 全局异常处理器

```java
package com.library.common.exception;

import com.library.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 校验失败（400）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)   // HTTP 状态码统一 200，业务码在 body 中
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(400, message);
    }

    /**
     * 处理业务异常（对应各种 4xx 业务码）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("[BUSINESS_ERROR] code={}, message={}", ex.getCode(), ex.getMessage());
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理未预期异常（500）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleException(Exception ex) {
        log.error("[SYSTEM_ERROR] 未预期异常", ex);
        return Result.fail(500, "系统错误，请联系管理员");
    }
}
```

---

## 10. DTO 规范（示例）

```java
package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookCreateDTO {

    @NotBlank(message = "ISBN不能为空")
    @Pattern(regexp = "^\\d{10}(\\d{3})?$", message = "ISBN格式不正确，应为10位或13位数字")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(max = 100, message = "书名最长100字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名最长50字符")
    private String author;

    @NotBlank(message = "出版社不能为空")
    @Size(max = 100, message = "出版社名最长100字符")
    private String publisher;

    @NotNull(message = "总库存数量不能为空")
    @Min(value = 1, message = "总库存数量必须大于0")
    private Integer totalQuantity;
}
```

```java
package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BorrowRequest {  // 借书（POST /api/borrows）和还书（POST /api/returns）共用

    @NotBlank(message = "读者证号不能为空")
    private String readerId;

    @NotBlank(message = "图书ISBN不能为空")
    private String bookIsbn;
}
```

```java
package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReaderCreateDTO {

    @NotBlank(message = "读者证号不能为空")
    @Size(max = 50, message = "读者证号最长50字符")
    private String readerId;

    @NotBlank(message = "读者姓名不能为空")
    @Size(max = 30, message = "姓名最长30字符")
    private String name;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "联系方式格式不正确，请输入11位手机号")
    private String contact;

    @Min(value = 1, message = "最大借阅数量最小为1")
    @Max(value = 20, message = "最大借阅数量最大为20")
    private Integer maxBorrowCount = 5;
}
```

---

## 11. MyBatis-Plus 配置

```java
package com.library.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件，指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

---

## 12. CORS 配置（开发环境）

```java
package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")  // Vite 开发服务器默认端口
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
```

---

## 13. application.yml 配置规范

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_system?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}   # 从环境变量读取，禁止硬编码密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 5     # 单用户本地部署，5个连接足够
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000

  servlet:
    multipart:
      max-file-size: 10MB      # CSV 文件上传大小限制
      max-request-size: 10MB

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true    # 下划线转驼峰（数据库 total_quantity → Java totalQuantity）
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl   # 开发环境打印 SQL
  global-config:
    db-config:
      logic-delete-field: deleted         # 如有软删除字段（本项目暂不使用）
      id-type: auto

logging:
  level:
    com.library: DEBUG          # 开发环境 DEBUG
    com.library.mapper: DEBUG   # 打印 SQL
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 14. 前端规范

### 14.1 API 封装规范

**request.ts — Axios 实例与拦截器**

```typescript
// src/api/request.ts
import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 响应拦截器：统一处理业务错误码
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

**book.ts — 图书 API 封装**

```typescript
// src/api/book.ts
import request from './request'
import type { BookVO, BookCreateDTO, BookUpdateDTO, ImportResultVO } from '@/types/api'
import type { PageResult, Result } from '@/types/api'

export const bookApi = {
  create: (data: BookCreateDTO): Promise<Result<BookVO>> =>
    request.post('/api/books', data),

  importCsv: (file: File): Promise<Result<ImportResultVO>> => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/books/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  list: (params: { keyword?: string; pageNum?: number; pageSize?: number }): Promise<Result<PageResult<BookVO>>> =>
    request.get('/api/books', { params }),

  update: (isbn: string, data: BookUpdateDTO): Promise<Result<BookVO>> =>
    request.put(`/api/books/${isbn}`, data),

  delete: (isbn: string): Promise<Result<void>> =>
    request.delete(`/api/books/${isbn}`)
}
```

### 14.2 文件命名规范

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| 页面组件（views/） | PascalCase + .vue | `BookList.vue`、`ReaderList.vue`、`BorrowManage.vue` |
| 可复用组件（components/） | PascalCase + .vue | `BookForm.vue`、`ImportDialog.vue` |
| API 文件（api/） | camelCase + .ts | `book.ts`、`reader.ts`、`borrow.ts` |
| Store 文件（stores/） | camelCase + .ts | `book.ts`、`reader.ts` |
| 工具函数（utils/） | camelCase + .ts | `format.ts`、`validate.ts` |
| 类型定义（types/） | camelCase + .ts | `api.ts` |

### 14.3 Vue 组件规范

```typescript
// BookList.vue 结构示例（Composition API + <script setup>）
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { bookApi } from '@/api/book'       // 正确：通过 api 层调用
import type { BookVO } from '@/types/api'

// 分页状态
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const books = ref<BookVO[]>([])
const loading = ref(false)

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await bookApi.list({ keyword: keyword.value, pageNum: pageNum.value, pageSize: pageSize.value })
    books.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(fetchBooks)
</script>
```

---

## 15. 禁止事项清单

| 类别 | 禁止行为 | 原因 |
|------|----------|------|
| Controller | 在 Controller 层写业务逻辑判断 | 违反分层原则，导致代码难以测试 |
| Mapper | 在 Mapper 层写业务逻辑 | Mapper 只负责 SQL，业务在 Service |
| Mapper | 使用 XML 文件定义 SQL | 项目约定全注解模式 |
| SQL | 使用 `${}` 拼接 SQL 参数 | SQL 注入风险，必须用 `#{}` |
| 异常 | catch 异常后不处理（空 catch） | 掩盖错误，导致问题难以排查 |
| 配置 | 硬编码数据库密码在代码或 yml 中 | 安全风险，密码必须通过环境变量注入 |
| 前端 | 在组件内直接调用 axios | 绕过 API 层，响应拦截器失效 |
| 依赖注入 | 使用 @Autowired 字段注入 | 不利于单元测试，使用构造器注入 |
| 日志 | 使用 System.out.println 输出日志 | 无法控制日志级别，必须用 SLF4J |

---

*文档版本：1.0.0 | 最后更新：2026-03-05*
