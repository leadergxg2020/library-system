package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_reader")
public class ReaderPO {

    @TableId(type = IdType.INPUT)
    private String readerId;

    /** 读者姓名，最长30字符 */
    private String name;

    /** 联系方式（手机号），可为空 */
    private String contact;

    /** 最大借阅数量，默认5 */
    private Integer maxBorrowCount;

    /** 当前借阅数量（随借还操作维护） */
    private Integer currentBorrowCount;

    /** 禁借截止日期，NULL 表示无禁借 */
    private LocalDate banUntil;

    /** 导致禁借的超期还书日期（用于展示原因） */
    private LocalDate banReasonDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
