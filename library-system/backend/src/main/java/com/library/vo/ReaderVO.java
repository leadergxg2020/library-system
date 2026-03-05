package com.library.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReaderVO {
    private String readerId;
    private String name;
    private String contact;
    private Integer maxBorrowCount;
    /** 当前借阅数量（计算值） */
    private Integer currentBorrowCount;
    /** 禁借截止日期，null 表示无禁借 */
    private LocalDate banUntil;
    /** 导致禁借的还书日期（用于展示原因） */
    private LocalDate banReasonDate;
    /** 是否禁借（banUntil != null && banUntil >= today） */
    private Boolean banned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
