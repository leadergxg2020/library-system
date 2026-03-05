-- ============================================================
-- 图书馆管理系统 - 数据库脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4 (支持完整 Unicode，含 emoji)
-- 排序规则：utf8mb4_unicode_ci (不区分大小写)
-- 日期：2026-03-05
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `library_system`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `library_system`;

-- ============================================================
-- 表 1：t_book（图书信息表）
-- ============================================================
DROP TABLE IF EXISTS `t_book`;

CREATE TABLE `t_book` (
    `id`             BIGINT          NOT NULL AUTO_INCREMENT         COMMENT '系统内部自增主键',
    `isbn`           VARCHAR(20)     NOT NULL                        COMMENT 'ISBN，唯一标识，10位或13位数字',
    `title`          VARCHAR(100)    NOT NULL                        COMMENT '书名，最长100字符',
    `author`         VARCHAR(50)     NOT NULL                        COMMENT '作者，最长50字符',
    `publisher`      VARCHAR(100)    NOT NULL                        COMMENT '出版社，最长100字符',
    `total_quantity` INT             NOT NULL DEFAULT 1              COMMENT '总库存数量（可借数量为计算值：total_quantity - 未还记录数）',
    `available_quantity` INT         NOT NULL DEFAULT 0              COMMENT '可借数量（随借还操作维护，= total_quantity - 未还借阅数）',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
    `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (`id`),

    -- ISBN 唯一索引：按 ISBN 查询/更新/删除图书
    UNIQUE KEY `uk_isbn` (`isbn`),

    -- title 普通索引：关键字搜索时按书名查询
    KEY `idx_title` (`title`),

    -- author 普通索引：关键字搜索时按作者查询
    KEY `idx_author` (`author`)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='图书信息表';


-- ============================================================
-- 表 2：t_reader（读者信息表）
-- ============================================================
DROP TABLE IF EXISTS `t_reader`;

CREATE TABLE `t_reader` (
    `reader_id`        VARCHAR(50)     NOT NULL                        COMMENT '读者证号，由管理员录入，唯一标识（主键）',
    `name`             VARCHAR(30)     NOT NULL                        COMMENT '读者姓名，最长30字符',
    `contact`          VARCHAR(20)     NULL     DEFAULT NULL           COMMENT '联系方式（手机号，11位），可为空',
    `max_borrow_count` INT             NOT NULL DEFAULT 5              COMMENT '最大借阅数量，默认5，范围1-20',
    `current_borrow_count` INT         NOT NULL DEFAULT 0              COMMENT '当前借阅数量（随借还操作维护）',
    `ban_until`        DATE            NULL     DEFAULT NULL           COMMENT '禁借截止日期（含当天），为空表示无禁借；借书时检查 ban_until >= 当前日期',
    `ban_reason_date`  DATE            NULL     DEFAULT NULL           COMMENT '导致禁借的超期还书日期（用于向管理员展示禁借原因）',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (`reader_id`),

    -- 姓名索引：关键字搜索时按姓名查询
    KEY `idx_name` (`name`),

    -- 禁借截止日期索引：逾期管理、借书前校验时用到
    KEY `idx_ban_until` (`ban_until`)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='读者信息表';


-- ============================================================
-- 表 3：t_borrow_record（借阅记录表）
-- ============================================================
DROP TABLE IF EXISTS `t_borrow_record`;

CREATE TABLE `t_borrow_record` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '借阅记录自增主键',
    `reader_id`   VARCHAR(50)  NOT NULL                             COMMENT '读者证号，外键关联 t_reader.reader_id',
    `book_isbn`   VARCHAR(20)  NOT NULL                             COMMENT 'ISBN，外键关联 t_book.isbn',
    `reader_name` VARCHAR(100) NOT NULL                             COMMENT '借阅时冗余存储的读者姓名（避免 JOIN）',
    `book_title`  VARCHAR(200) NOT NULL                             COMMENT '借阅时冗余存储的书名（避免 JOIN）',
    `borrow_date` DATE         NOT NULL                             COMMENT '借出日期',
    `due_date`    DATE         NOT NULL                             COMMENT '应还日期 = borrow_date + 30天',
    `return_date` DATE         NULL     DEFAULT NULL                COMMENT '实际归还日期，NULL 表示未还',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '记录创建时间',

    PRIMARY KEY (`id`),

    -- 复合索引1：查询某读者借某书的未还记录（借书重复检查 + 还书操作）
    -- 查询：WHERE reader_id = ? AND book_isbn = ? AND return_date IS NULL
    KEY `idx_reader_book_return` (`reader_id`, `book_isbn`, `return_date`),

    -- 复合索引2：逾期查询（WHERE return_date IS NULL AND due_date < ?）
    KEY `idx_return_due` (`return_date`, `due_date`),

    -- 单列索引：按 book_isbn 查询该书未还数量（库存计算）
    -- 查询：WHERE book_isbn = ? AND return_date IS NULL
    KEY `idx_book_isbn` (`book_isbn`),

    -- 单列索引：按 reader_id 查询该读者当前借阅数（上限校验）
    -- 查询：WHERE reader_id = ? AND return_date IS NULL
    -- 注意：reader_id 已在 idx_reader_book_return 最左列，此处可复用，但单列索引更高效
    KEY `idx_reader_id` (`reader_id`),

    -- 外键约束
    CONSTRAINT `fk_borrow_reader` FOREIGN KEY (`reader_id`)
        REFERENCES `t_reader` (`reader_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT `fk_borrow_book` FOREIGN KEY (`book_isbn`)
        REFERENCES `t_book` (`isbn`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='借阅记录表（return_date 为 NULL 表示未归还）';


-- ============================================================
-- 初始化示例数据（可选，仅供开发测试使用）
-- ============================================================

-- 示例图书
INSERT INTO `t_book` (`isbn`, `title`, `author`, `publisher`, `total_quantity`, `available_quantity`) VALUES
('9787111234567', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', 3, 3),
('9787111580935', '深入理解Java虚拟机', '周志明', '机械工业出版社', 2, 2),
('9787115428028', 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', 5, 5);

-- 示例读者
INSERT INTO `t_reader` (`reader_id`, `name`, `contact`, `max_borrow_count`) VALUES
('R2024001', '张三', '13800138001', 5),
('R2024002', '李四', '13800138002', 3);

-- ============================================================
-- 索引说明
-- ============================================================
-- t_book:
--   uk_isbn          → PUT/DELETE /api/books/{isbn} 路径参数查询
--   idx_title        → GET /api/books?keyword= 模糊搜索（LIKE '%keyword%'）
--   idx_author       → GET /api/books?keyword= 模糊搜索（LIKE '%keyword%'）
--
-- t_reader:
--   reader_id (PK)   → GET/PUT /api/readers/{readerId} 路径参数查询
--   idx_name         → GET /api/readers?keyword= 姓名模糊搜索
--   idx_ban_until    → 借书时禁借期校验；逾期读者统计
--
-- t_borrow_record:
--   idx_reader_book_return → 还书操作查找未还记录；借书时检查同书未还记录
--   idx_return_due         → GET /api/borrows/overdue 逾期查询
--   idx_book_isbn          → 计算某书可借数量（WHERE book_isbn=? AND return_date IS NULL）
--   idx_reader_id          → 计算读者当前借阅数（WHERE reader_id=? AND return_date IS NULL）
-- ============================================================
