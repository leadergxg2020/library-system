-- ============================================================
-- 图书馆管理系统 - SQLite 数据库脚本
-- 数据库：SQLite 3.x
-- 注意：updated_at 由 MyMetaObjectHandler Java 层自动填充，无需 ON UPDATE 触发器
-- ============================================================

-- ============================================================
-- 表 1：t_book（图书信息表）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_book (
    id                 INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    isbn               TEXT    NOT NULL UNIQUE,
    title              TEXT    NOT NULL,
    author             TEXT    NOT NULL,
    publisher          TEXT    NOT NULL,
    total_quantity     INTEGER NOT NULL DEFAULT 1,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    created_at         TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at         TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE INDEX IF NOT EXISTS idx_title  ON t_book(title);
CREATE INDEX IF NOT EXISTS idx_author ON t_book(author);


-- ============================================================
-- 表 2：t_reader（读者信息表）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_reader (
    reader_id            TEXT    NOT NULL PRIMARY KEY,
    name                 TEXT    NOT NULL,
    contact              TEXT,
    max_borrow_count     INTEGER NOT NULL DEFAULT 5,
    current_borrow_count INTEGER NOT NULL DEFAULT 0,
    ban_until            TEXT,
    ban_reason_date      TEXT,
    created_at           TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at           TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE INDEX IF NOT EXISTS idx_reader_name ON t_reader(name);
CREATE INDEX IF NOT EXISTS idx_ban_until   ON t_reader(ban_until);


-- ============================================================
-- 表 3：t_borrow_record（借阅记录表）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_borrow_record (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    reader_id   TEXT    NOT NULL,
    book_isbn   TEXT    NOT NULL,
    reader_name TEXT    NOT NULL,
    book_title  TEXT    NOT NULL,
    borrow_date TEXT    NOT NULL,
    due_date    TEXT    NOT NULL,
    return_date TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),

    FOREIGN KEY (reader_id) REFERENCES t_reader(reader_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (book_isbn) REFERENCES t_book(isbn)        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_reader_book_return ON t_borrow_record(reader_id, book_isbn, return_date);
CREATE INDEX IF NOT EXISTS idx_return_due         ON t_borrow_record(return_date, due_date);
CREATE INDEX IF NOT EXISTS idx_book_isbn          ON t_borrow_record(book_isbn);
CREATE INDEX IF NOT EXISTS idx_reader_id          ON t_borrow_record(reader_id);


-- ============================================================
-- 表 4：t_admin（管理员账号表）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_admin (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    username    TEXT    NOT NULL UNIQUE,
    employee_id TEXT    UNIQUE,              -- 员工号，可空，唯一
    phone       TEXT,                        -- 联系电话（11位手机号）
    email       TEXT,                        -- 邮箱地址
    salt        TEXT    NOT NULL,
    password    TEXT    NOT NULL,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 默认管理员：用户名 admin，密码 admin123
-- salt = 'default_salt_2026', password = SHA-256(salt + raw_password)
-- 此哈希值对应 SHA-256("default_salt_2026" + "admin123")
INSERT OR IGNORE INTO t_admin (username, salt, password) VALUES
    ('admin', 'default_salt_2026', '5bc704e16221f2a7ea78e39691a454290b3d4dc064d0e73bdb68c10d3b2b87c6');


-- ============================================================
-- 初始化示例数据（INSERT OR IGNORE 保证重启不重复插入）
-- ============================================================
INSERT OR IGNORE INTO t_book (isbn, title, author, publisher, total_quantity, available_quantity) VALUES
    ('9787111234567', 'Java编程思想',             'Bruce Eckel',  '机械工业出版社', 3, 3),
    ('9787111580935', '深入理解Java虚拟机',        '周志明',        '机械工业出版社', 2, 2),
    ('9787115428028', 'Python编程：从入门到实践',  'Eric Matthes', '人民邮电出版社', 5, 5);

INSERT OR IGNORE INTO t_reader (reader_id, name, contact, max_borrow_count) VALUES
    ('R2024001', '张三', '13800138001', 5),
    ('R2024002', '李四', '13800138002', 3);
