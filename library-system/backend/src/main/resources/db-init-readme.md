# 数据库初始化说明

## 前置条件
- MySQL 8.0 已安装并运行
- 默认端口 3306

## 初始化步骤

1. 连接 MySQL：
   mysql -u root -p

2. 执行建表脚本：
   source /path/to/schema.sql

   或者：
   mysql -u root -p < schema.sql

3. 验证建表结果：
   USE library_system;
   SHOW TABLES;
   -- 应显示：t_book, t_reader, t_borrow_record

## 表结构说明

| 表名 | 说明 |
|------|------|
| t_book | 图书信息表 |
| t_reader | 读者信息表 |
| t_borrow_record | 借阅记录表 |

## 注意事项
- schema.sql 包含 DROP TABLE IF EXISTS，会清空已有数据，谨慎在生产环境执行
- 测试数据已包含在 schema.sql 末尾（示例图书和读者），如不需要可注释掉
