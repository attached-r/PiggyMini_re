package auth.service;

import auth.dto.*;
import auth.entity.User;
import auth.mapper.UserMapper;
import common.exception.GlobalException;
import common.util.BCryptUtil;
import common.util.JWTUtil;
import common.util.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    private static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000;
    private static final long CACHE_EXPIRE_SECONDS = 300;

    @Override
    public AuthResponse register(RegisterRequest request) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);

        if (existUser != null) {
            throw GlobalException.businessError("用户名已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(BCryptUtil.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userMapper.insert(user);

        String cacheKey = "user:" + user.getId();
        redisUtil.set(cacheKey, user, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername();
        User user = getUserByUsernameFromCacheOrDB(username);

        if (user == null) {
            throw GlobalException.businessError("用户不存在");
        }

        if (!BCryptUtil.matches(request.getPassword(), user.getPassword())) {
            throw GlobalException.businessError("密码错误");
        }

        if (user.getStatus() == 0) {
            throw GlobalException.businessError("账号已被禁用");
        }

        return generateAuthResponse(user);
    }

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
        User user = getUserByIdFromCacheOrDB(userId);

        if (user == null) {
            throw GlobalException.businessError("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw GlobalException.businessError("账号已被禁用");
        }

        return generateAuthResponse(user);
    }

    private User getUserByUsernameFromCacheOrDB(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null) {
            String cacheKey = "user:" + user.getId();
            redisUtil.set(cacheKey, user, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return user;
    }

    private User getUserByIdFromCacheOrDB(Long userId) {
        String cacheKey = "user:" + userId;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            return (User) cached;
        }

        User user = userMapper.selectById(userId);
        if (user != null) {
            redisUtil.set(cacheKey, user, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return user;
    }

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
