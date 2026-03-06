package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_admin")
public class AdminPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 管理员用户名，唯一 */
    private String username;

    /** 员工号，可空，唯一 */
    private String employeeId;

    /** 联系电话（11位手机号），可空 */
    private String phone;

    /** 邮箱地址，可空 */
    private String email;

    /** 密码盐值（每用户独立随机生成） */
    private String salt;

    /** SHA-256(salt + password) 哈希值 */
    private String password;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
