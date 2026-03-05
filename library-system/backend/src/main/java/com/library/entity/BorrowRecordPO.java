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

    /** 借阅时冗余存储的读者姓名（避免 JOIN 查询） */
    private String readerName;

    /** 借阅时冗余存储的书名（避免 JOIN 查询） */
    private String bookTitle;

    /** 借出日期 */
    private LocalDate borrowDate;

    /** 应还日期 = borrowDate + 30天 */
    private LocalDate dueDate;

    /** 实际归还日期，NULL 表示未还 */
    private LocalDate returnDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
