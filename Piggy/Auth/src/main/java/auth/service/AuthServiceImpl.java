package auth.service;

import auth.dto.*;
import auth.entity.User;
import auth.mapper.UserMapper;
import common.exception.GlobalException;
import common.util.BCryptUtil;
import common.util.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/**
 * 认证服务实现类
 *
 * @author: rj
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper; // 用户数据访问接口
    private final JWTUtil jwtUtil;  // JWT 工具类

    private static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000; // 令牌过期时间 2 小时
    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @return 统一响应结果
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        // 1. 判断用户名是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>(); // 创建查询条件
        queryWrapper.eq(User::getUsername, request.getUsername());  // 设置查询条件
        User existUser = userMapper.selectOne(queryWrapper);        // 执行查询

        if (existUser != null) {
            throw GlobalException.businessError("用户名已存在");

        }
        // 2. 不存在创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(BCryptUtil.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 3. 保存用户
        userMapper.insert(user);

        return generateAuthResponse(user);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 统一响应结果
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. 判断用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw GlobalException.businessError("用户不存在");
        }
        // 2. 验证密码 比较 明文加密之后 和 密文 是否匹配
        if (!BCryptUtil.matches(request.getPassword(), user.getPassword())) {
            throw GlobalException.businessError("密码错误");
        }
        // 3. 验证账号状态
        if (user.getStatus() == 0) {
            throw GlobalException.businessError("账号已被禁用");
        }
        // 4. 生成Token
        return generateAuthResponse(user);
    }
    /**
     * 刷新Token
     *
     * @param request 刷新Token请求参数
     * @return 统一响应结果
     */
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateJWT(refreshToken)) {
            throw GlobalException.businessError("Refresh Token 无效");
        }

        String subject = jwtUtil.getSubject(refreshToken);
        if (subject == null || !subject.startsWith("refresh:")) {
            throw GlobalException.businessError("Token 类型错误");
        }

        Long userId = Long.parseLong(subject.replace("refresh:", ""));

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw GlobalException.businessError("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw GlobalException.businessError("账号已被禁用");
        }

        return generateAuthResponse(user);
    }

    /**
     * 生成认证响应结果
     *
     * @param user 用户信息
     * @return 认证响应结果
     */
    private AuthResponse generateAuthResponse(User user) {
        String jwtId = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());

        String accessToken = jwtUtil.createJWT(jwtId, user.getId().toString(), claims, ACCESS_TOKEN_EXPIRE);

        String refreshJwtId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put("userId", user.getId());
        String refreshToken = jwtUtil.createJWT(refreshJwtId, "refresh:" + user.getId(), refreshClaims, ACCESS_TOKEN_EXPIRE * 7);

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
