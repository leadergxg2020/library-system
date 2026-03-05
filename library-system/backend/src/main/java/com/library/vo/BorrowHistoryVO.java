package com.library.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowHistoryVO {
    private Long recordId;
    private String readerId;
    private String readerName;
    private String bookIsbn;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    /** 实际归还日期，null 表示未还 */
    private LocalDate returnDate;
    /** 是否已归还 */
    private boolean returned;
    /** 超期天数，未超期或未还时为 null */
    private Integer overdueDays;
    private LocalDateTime createdAt;
}
