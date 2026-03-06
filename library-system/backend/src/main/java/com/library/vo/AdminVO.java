package com.library.vo;

import com.library.entity.AdminPO;
import lombok.Data;

@Data
public class AdminVO {

    private Long id;
    private String username;
    private String employeeId;
    private String phone;
    private String email;

    /** 从 AdminPO 构建 AdminVO（不暴露 salt/password） */
    public static AdminVO from(AdminPO po) {
        AdminVO vo = new AdminVO();
        vo.setId(po.getId());
        vo.setUsername(po.getUsername());
        vo.setEmployeeId(po.getEmployeeId());
        vo.setPhone(po.getPhone());
        vo.setEmail(po.getEmail());
        return vo;
    }
}
