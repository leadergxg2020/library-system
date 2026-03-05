package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReaderUpdateDTO {

    @Size(max = 30, message = "姓名最长30字符")
    private String name;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "联系方式格式不正确，请输入11位手机号")
    private String contact;

    @Min(value = 1, message = "最大借阅数量最小为1")
    @Max(value = 20, message = "最大借阅数量最大为20")
    private Integer maxBorrowCount;
}
