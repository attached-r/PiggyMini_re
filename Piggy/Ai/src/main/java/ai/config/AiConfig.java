package ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /**
     * AI 模型配置
     */
    private Model model = new Model();

    /**
     * 提示词模板配置
     */
    private Prompts prompts = new Prompts();

    @Data
    public static class Model {
        private String name = "qwen-plus";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
    }

    @Data
    public static class Prompts {
        private String classify;
        private String batch;
        private String query;
        private String analyze;
    }

    /**
     * 配置 ObjectMapper 支持 Java 8 时间类型
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
