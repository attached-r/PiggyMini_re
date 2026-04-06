package common.util;

import common.config.JWTConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * @author: rj
 * JWT工具类
 * 1. 生成JWT令牌
 * 2. 解析JWT令牌
 */
@Component
public class JWTUtil {

    private final SecretKey key;
    private final long defaultExpiration;

    @Autowired
    public JWTUtil(JWTConfig jwtConfig) {
        this.key = jwtConfig.jwtSecretKey();
        this.defaultExpiration = 3600000;
    }
    //  三个参数 传参生成
    public String createJWT(String id, String subject, long ttlMillis) {
        return createJWT(id, subject, null, ttlMillis);
    }

    /**
     * 生成JWT令牌
     *
     * @param id        JWT的唯一标识（jti）
     * @param subject   JWT的主题，通常存放用户ID或用户名
     * @param claims    自定义的Claims数据（可选）
     * @param ttlMillis 令牌有效期（毫秒）
     * @return JWT字符串
     */
    //设置令牌有效期
    public String createJWT(String id, String subject, Map<String, Object> claims, long ttlMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder()
                .id(id)
                .subject(subject)
                .issuedAt(now)
                .signWith(key, Jwts.SIG.HS256);
        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);
        builder.expiration(exp);
        return builder.compact();
    }
            /*修复：移除 ttlMillis > 0 的判断，确保始终设置过期时间
              原逻辑会导致 ttlMillis <= 0 时生成永久有效的令牌（安全隐患）
              现逻辑：正数=未来过期，0=立即过期，负数=已过期
            */

    /**
     * 解析JWT令牌
     *
     * @param jwt JWT字符串
     * @return Claims对象，包含JWT中的所有声明
     * @throws ExpiredJwtException 令牌已过期
     * @throws Exception           其他解析异常
     */
    public Claims parseJWT(String jwt) throws Exception {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    /**
     * 检查JWT是否过期
     *
     * @param jwt JWT字符串
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String jwt) throws Exception {
        try {
            Claims claims = parseJWT(jwt);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 从JWT中获取Subject（通常是用户ID）
     *
     * @param jwt JWT字符串
     * @return Subject内容
     */
    public String getSubject(String jwt) {
        try {
            Claims claims = parseJWT(jwt);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从JWT中获取自定义Claim值
     *
     * @param jwt       JWT字符串
     * @param claimName Claim名称
     * @return Claim值
     */
    public Object getClaim(String jwt, String claimName) {
        try {
            Claims claims = parseJWT(jwt);
            return claims.get(claimName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证JWT是否有效
     *
     * @param jwt JWT字符串
     * @return true-有效，false-无效
     */
    public boolean validateJWT(String jwt) {
        try {
            parseJWT(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
