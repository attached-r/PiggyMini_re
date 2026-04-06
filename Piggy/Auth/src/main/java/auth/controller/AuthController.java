package auth.controller;

import auth.dto.*;
import auth.service.AuthService;
import common.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/**
 * 认证控制器
 *
 * @author: rj
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @return 统一响应结果
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 统一响应结果
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 刷新 Token
     *
     * @param request 刷新 Token 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/refresh")
    public Result refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return Result.success("Token 刷新成功", response);
    }
}
