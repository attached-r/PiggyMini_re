package gateway.config;

import common.config.JWTConfig;
import common.util.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 模块配置类
 * 注册 Common 模块的工具类 Bean
 *
 * @author: rj
 */
@Configuration
public class GatewayConfig {

    /**
     * 注册 JWTUtil Bean
     */
    @Bean
    public JWTUtil jwtUtil(JWTConfig jwtConfig) {
        return new JWTUtil(jwtConfig);
    }

    /**
     * 注册 JWTConfig Bean（从配置文件读取密钥）
     */
    @Bean
    public JWTConfig jwtConfig() {
        return new JWTConfig();
    }
}
