package auth.service;

import auth.dto.AuthResponse;
import auth.dto.LoginRequest;
import auth.dto.RefreshTokenRequest;
import auth.dto.RegisterRequest;
import auth.dto.UpdateAvatarRequest;

/**
 * 认证服务接口
 *
 * @author: rj
 */
public interface AuthService {
    /**
     * 登录
     *
     * @param loginRequest 登录请求参数
     * @return 登录响应
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * 注册
     *
     * @param registerRequest 注册请求参数
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * 刷新Token
     *
     * @param refreshTokenRequest 刷新Token请求参数
     * @return 刷新Token响应
     */
    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param request 头像更新请求参数
     * @return 更新后的认证响应（包含新头像）
     */
    AuthResponse updateAvatar(Long userId, UpdateAvatarRequest request);
}
