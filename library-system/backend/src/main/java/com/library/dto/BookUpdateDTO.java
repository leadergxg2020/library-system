package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookUpdateDTO {

    @Size(max = 100, message = "书名最长100字符")
    private String title;

    @Size(max = 50, message = "作者名最长50字符")
    private String author;

    @Size(max = 100, message = "出版社名最长100字符")
    private String publisher;

    @Min(value = 1, message = "总库存数量必须大于0")
    private Integer totalQuantity;
}
