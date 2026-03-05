package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotBlank(message = "读者证号不能为空")
    private String readerId;

    @NotBlank(message = "图书ISBN不能为空")
    private String bookIsbn;
}
