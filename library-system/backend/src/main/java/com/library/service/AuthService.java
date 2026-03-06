package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.exception.BusinessException;
import com.library.dto.LoginDTO;
import com.library.dto.RegisterDTO;
import com.library.entity.AdminPO;
import com.library.mapper.AdminMapper;
import com.library.vo.AdminVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminMapper adminMapper;

    /** 会话中存储当前登录用户名的 key */
    public static final String SESSION_KEY = "ADMIN_USERNAME";

    /**
     * 登录：校验用户名密码，返回 AdminVO
     */
    public AdminVO login(LoginDTO dto) {
        AdminPO admin = adminMapper.selectOne(
                new LambdaQueryWrapper<AdminPO>().eq(AdminPO::getUsername, dto.getUsername()));
        if (admin == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String hashed = hashPassword(admin.getSalt(), dto.getPassword());
        if (!hashed.equals(admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        log.info("[AUTH_LOGIN] username={} - SUCCESS", dto.getUsername());
        return AdminVO.from(admin);
    }

    /**
     * 注册新管理员账号（需要已登录的管理员操作）
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminVO register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
        boolean exists = adminMapper.exists(
                new LambdaQueryWrapper<AdminPO>().eq(AdminPO::getUsername, dto.getUsername()));
        if (exists) {
            throw new BusinessException(409, "用户名 " + dto.getUsername() + " 已存在");
        }
        String salt = UUID.randomUUID().toString().replace("-", "");
        String hashed = hashPassword(salt, dto.getPassword());
        AdminPO admin = AdminPO.builder()
                .username(dto.getUsername())
                .employeeId(dto.getEmployeeId())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .salt(salt)
                .password(hashed)
                .build();
        adminMapper.insert(admin);
        log.info("[AUTH_REGISTER] username={} - SUCCESS", dto.getUsername());
        return AdminVO.from(admin);
    }

    /**
     * 根据用户名获取管理员信息（用于 /me 接口）
     */
    public AdminVO getByUsername(String username) {
        AdminPO admin = adminMapper.selectOne(
                new LambdaQueryWrapper<AdminPO>().eq(AdminPO::getUsername, username));
        if (admin == null) {
            throw new BusinessException(401, "用户不存在，请重新登录");
        }
        return AdminVO.from(admin);
    }

    // ===== 私有工具方法 =====

    /** SHA-256(salt + password) */
    private String hashPassword(String salt, String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((salt + rawPassword).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
}
