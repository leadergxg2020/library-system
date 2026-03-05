package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_book")
public class BookPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** ISBN，唯一，10位或13位数字 */
    private String isbn;

    /** 书名，最长100字符 */
    private String title;

    /** 作者，最长50字符 */
    private String author;

    /** 出版社，最长100字符 */
    private String publisher;

    /** 总库存数量 */
    private Integer totalQuantity;

    /** 可借数量（随借还操作维护，= totalQuantity - 未还借阅数） */
    private Integer availableQuantity;

    /** 创建时间，由 MyBatis-Plus 自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，由 MyBatis-Plus 自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
