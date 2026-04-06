import common.config.JWTConfig;
import common.util.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWTUtil 单元测试类
 */
@DisplayName("JWT工具类测试")
class JWTUtilTest {

    private JWTUtil jwtUtil;
    private JWTConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JWTConfig();
        ReflectionTestUtils.setField(jwtConfig, "secret", "TestSecretKeyForJWTSigning2026SecureString");
        jwtUtil = new JWTUtil(jwtConfig);
    }

    @Test
    @DisplayName("测试生成JWT令牌 - 基础参数")
    void testCreateJWT_BasicParameters() {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, ttlMillis);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("测试生成JWT令牌 - 包含自定义Claims")
    void testCreateJWT_WithCustomClaims() {
        String id = UUID.randomUUID().toString();
        String subject = "user456";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        claims.put("username", "testUser");
        claims.put("email", "test@example.com");
        long ttlMillis = 7200000;

        String token = jwtUtil.createJWT(id, subject, claims, ttlMillis);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("测试生成JWT令牌 - Claims为空")
    void testCreateJWT_WithNullClaims() {
        String id = UUID.randomUUID().toString();
        String subject = "user789";
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, null, ttlMillis);

        assertNotNull(token);
        String extractedSubject = jwtUtil.getSubject(token);
        assertEquals(subject, extractedSubject);
    }

    @Test
    @DisplayName("测试解析JWT令牌")
    void testParseJWT() throws Exception {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, claims, ttlMillis);
        Claims parsedClaims = jwtUtil.parseJWT(token);

        assertNotNull(parsedClaims);
        assertEquals(subject, parsedClaims.getSubject());
        assertEquals(id, parsedClaims.getId());
        assertEquals("admin", parsedClaims.get("role"));
        assertNotNull(parsedClaims.getIssuedAt());
        assertNotNull(parsedClaims.getExpiration());
    }

    @Test
    @DisplayName("测试解析过期的JWT令牌")
    void testParseJWT_ExpiredToken() {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long expiredTtl = -1000;

        String token = jwtUtil.createJWT(id, subject, expiredTtl);

        assertThrows(Exception.class, () -> {
            jwtUtil.parseJWT(token);
        });
    }

    @Test
    @DisplayName("测试检查JWT是否过期 - 未过期")
    void testIsTokenExpired_NotExpired() throws Exception {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, ttlMillis);

        boolean expired = jwtUtil.isTokenExpired(token);
        assertFalse(expired);
    }

    @Test
    @DisplayName("测试检查JWT是否过期 - 已过期")
    void testIsTokenExpired_Expired() throws Exception {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long expiredTtl = -1000;

        String token = jwtUtil.createJWT(id, subject, expiredTtl);

        boolean expired = jwtUtil.isTokenExpired(token);
        assertTrue(expired);
    }

    @Test
    @DisplayName("测试从JWT获取Subject")
    void testGetSubject() {
        String id = UUID.randomUUID().toString();
        String subject = "testUser123";
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, ttlMillis);
        String extractedSubject = jwtUtil.getSubject(token);

        assertEquals(subject, extractedSubject);
    }

    @Test
    @DisplayName("测试从JWT获取无效的Subject")
    void testGetSubject_InvalidToken() {
        String invalidToken = "invalid.token.here";
        String subject = jwtUtil.getSubject(invalidToken);

        assertNull(subject);
    }

    @Test
    @DisplayName("测试从JWT获取自定义Claim")
    void testGetClaim() {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        claims.put("level", 5);
        claims.put("active", true);
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, claims, ttlMillis);

        assertEquals("admin", jwtUtil.getClaim(token, "role"));
        assertEquals(5, jwtUtil.getClaim(token, "level"));
        assertEquals(true, jwtUtil.getClaim(token, "active"));
    }

    @Test
    @DisplayName("测试从JWT获取不存在的Claim")
    void testGetClaim_NonExistent() {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, ttlMillis);
        Object claim = jwtUtil.getClaim(token, "nonExistent");

        assertNull(claim);
    }

    @Test
    @DisplayName("测试验证有效的JWT")
    void testValidateJWT_Valid() {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, ttlMillis);

        boolean isValid = jwtUtil.validateJWT(token);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("测试验证无效的JWT")
    void testValidateJWT_Invalid() {
        String invalidToken = "invalid.token.string";
        boolean isValid = jwtUtil.validateJWT(invalidToken);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试验证过期的JWT")
    void testValidateJWT_Expired() {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long expiredTtl = -1000;

        String token = jwtUtil.createJWT(id, subject, expiredTtl);

        boolean isValid = jwtUtil.validateJWT(token);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试验证被篡改的JWT")
    void testValidateJWT_Tampered() {
        String id = UUID.randomUUID().toString();
        String subject = "user123";
        long ttlMillis = 3600000;

        String token = jwtUtil.createJWT(id, subject, ttlMillis);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        boolean isValid = jwtUtil.validateJWT(tamperedToken);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试JWT令牌的完整性 - 完整流程")
    void testJWT_CompleteFlow() throws Exception {
        String userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", "user");
        customClaims.put("department", "engineering");
        customClaims.put("permissions", new String[]{"read", "write"});
        long ttlMillis = 3600000;

        String tokenId = UUID.randomUUID().toString();
        String token = jwtUtil.createJWT(tokenId, userId, customClaims, ttlMillis);

        assertTrue(jwtUtil.validateJWT(token));
        assertFalse(jwtUtil.isTokenExpired(token));
        assertEquals(userId, jwtUtil.getSubject(token));
        assertEquals("user", jwtUtil.getClaim(token, "role"));
        assertEquals("engineering", jwtUtil.getClaim(token, "department"));

        Claims claims = jwtUtil.parseJWT(token);
        assertEquals(tokenId, claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("测试不同有效期的JWT")
    void testJWT_DifferentTTLs() throws Exception {
        String id = UUID.randomUUID().toString();
        String subject = "user123";

        String token1Hour = jwtUtil.createJWT(id, subject, 3600000);
        String token24Hours = jwtUtil.createJWT(id, subject, 86400000);
        String token7Days = jwtUtil.createJWT(id, subject, 604800000L);

        assertNotNull(token1Hour);
        assertNotNull(token24Hours);
        assertNotNull(token7Days);

        Claims claims1 = jwtUtil.parseJWT(token1Hour);
        Claims claims24 = jwtUtil.parseJWT(token24Hours);
        Claims claims7 = jwtUtil.parseJWT(token7Days);

        long diff1 = claims1.getExpiration().getTime() - claims1.getIssuedAt().getTime();
        long diff24 = claims24.getExpiration().getTime() - claims24.getIssuedAt().getTime();
        long diff7 = claims7.getExpiration().getTime() - claims7.getIssuedAt().getTime();

        assertEquals(3600000L, diff1);
        assertEquals(86400000L, diff24);
        assertEquals(604800000L, diff7);
    }

    @Test
    @DisplayName("测试空字符串JWT")
    void testJWT_EmptyString() {
        assertFalse(jwtUtil.validateJWT(""));
        assertNull(jwtUtil.getSubject(""));
        assertNull(jwtUtil.getClaim("", "role"));
    }

    @Test
    @DisplayName("测试null JWT")
    void testJWT_Null() {
        assertFalse(jwtUtil.validateJWT(null));
        assertNull(jwtUtil.getSubject(null));
        assertNull(jwtUtil.getClaim(null, "role"));
    }
}
