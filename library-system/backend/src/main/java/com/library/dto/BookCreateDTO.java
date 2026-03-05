package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookCreateDTO {

    @NotBlank(message = "ISBN不能为空")
    @Pattern(regexp = "^\\d{10}(\\d{3})?$", message = "ISBN格式不正确，应为10位或13位数字")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(max = 100, message = "书名最长100字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名最长50字符")
    private String author;

    @NotBlank(message = "出版社不能为空")
    @Size(max = 100, message = "出版社名最长100字符")
    private String publisher;

    @NotNull(message = "总库存数量不能为空")
    @Min(value = 1, message = "总库存数量必须大于0")
    private Integer totalQuantity;
}
