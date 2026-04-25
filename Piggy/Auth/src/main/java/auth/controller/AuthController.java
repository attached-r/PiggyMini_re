package auth.controller;

import auth.dto.*;
import auth.service.AuthService;
import common.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户注册、登录、Token刷新、头像更新等认证相关接口
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

    /**
     * 更新用户头像
     * 从请求头 X-User-Id 获取当前登录用户ID
     *
     * @param userId  用户ID（由网关从Token解析并传递）
     * @param request 头像更新请求参数
     * @return 统一响应结果，包含更新后的用户信息
     */
    @PutMapping("/avatar")
    public Result updateAvatar(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateAvatarRequest request) {
        AuthResponse response = authService.updateAvatar(userId, request);
        return Result.success("头像更新成功", response);
    }
}
