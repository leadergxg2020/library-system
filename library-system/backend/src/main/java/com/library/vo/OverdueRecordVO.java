package com.library.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OverdueRecordVO {
    private Long recordId;
    private String readerId;
    private String readerName;
    private String bookIsbn;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    /** 超期天数（今天 - dueDate，今天日期 > dueDate则视为超期） */
    private Integer overdueDays;
    private LocalDateTime createdAt;
}
