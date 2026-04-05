package common.util;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * BCrypt工具类
 * @author: rj
 *
 * BCrypt加密解密 密码
 */
@Component // 添加组件
public class BCryptUtil {
    // 默认强度，值越大越安全但计算越慢
    private static final int DEFAULT_STRENGTH = 10;

    /**
     * 对明文密码进行BCrypt加密
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码哈希值
     */
    public static String encode(String rawPassword) {
        return encode(rawPassword, DEFAULT_STRENGTH);
    }

    /**
     * 对明文密码进行BCrypt加密（自定义强度，核心功能）
     *
     * @param rawPassword 明文密码
     * @param strength    加密强度（4-31），值越大越安全但计算越慢
     * @return 加密后的密码哈希值
     * @throws IllegalArgumentException 强度参数不合法时抛出
     */
    public static String encode(String rawPassword, int strength) {
        // 1. 参数校验
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (strength < 4 || strength > 31) {
            throw new IllegalArgumentException("加密强度必须在4-31之间");
        }
        // 2.生成盐 原理是生成随机数，然后根据随机数生成盐
        String salt = BCrypt.gensalt(strength);

        // 3.生成密文 采用BCrypt.hashpw方法, 参数是明文密码和盐
        return BCrypt.hashpw(rawPassword, salt);
    }

    /**
     * 验证明文密码是否与加密后的密码匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 加密后的密码哈希值
     * @return true-匹配，false-不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return false;
        }
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 检查加密密码是否需要升级（强度过低）
     *
     * @param encodedPassword 加密后的密码哈希值
     * @param minStrength     最低要求的强度
     * @return true-需要升级，false-不需要
     */
    public static boolean needsUpgrade(String encodedPassword, int minStrength) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return true;
        }
        // 解析密码
        try {
            String[] parts = encodedPassword.split("\\$");
            // 获取强度
            if (parts.length >= 3) {
                int currentStrength = Integer.parseInt(parts[2]);
                return currentStrength < minStrength;
            }
        } catch (Exception e) {
            return true;
        }

        return false;
    }

    /**
     * 检查加密密码是否使用默认强度
     *
     * @param encodedPassword 加密后的密码哈希值
     * @return true-是默认强度，false-不是
     */
    public static boolean isDefaultStrength(String encodedPassword) {
        return !needsUpgrade(encodedPassword, DEFAULT_STRENGTH + 1)
                && !needsUpgrade(encodedPassword, DEFAULT_STRENGTH - 1);
    }
}
