package com.library.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowSuccessVO {
    private Long recordId;
    private String readerId;
    private String readerName;
    private String bookIsbn;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
}
