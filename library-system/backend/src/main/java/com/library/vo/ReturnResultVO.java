package com.library.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReturnResultVO {
    private Long recordId;
    private String readerId;
    private String readerName;
    private String bookIsbn;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    /** 是否超期 */
    private Boolean overdue;
    /** 超期天数（overdue=true时有值） */
    private Integer overdueDays;
    /** 禁借截止日期（超期时，禁借到此日期） */
    private LocalDate banUntil;
}
