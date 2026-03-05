package com.library.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookVO {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer totalQuantity;
    /** 可借数量（计算值 = totalQuantity - 未还借阅数） */
    private Integer availableQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
