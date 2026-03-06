package com.library.controller;

import com.library.common.Result;
import com.library.dto.LoginDTO;
import com.library.dto.RegisterDTO;
import com.library.service.AuthService;
import com.library.vo.AdminVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录 */
    @PostMapping("/login")
    public Result<AdminVO> login(@Valid @RequestBody LoginDTO dto, HttpSession session) {
        AdminVO admin = authService.login(dto);
        session.setAttribute(AuthService.SESSION_KEY, admin.getUsername());
        return Result.success(admin);
    }

    /** 登出 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success(null);
    }

    /** 注册新管理员账号 */
    @PostMapping("/register")
    public Result<AdminVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public Result<AdminVO> me(HttpSession session) {
        String username = (String) session.getAttribute(AuthService.SESSION_KEY);
        return Result.success(authService.getByUsername(username));
    }
}
