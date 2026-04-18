package ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/**
 * 提示语配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.prompts")
public class PromptConfig {

    private String classify;
    private String batch;
    private String query;
    private String analyze;
    private String chat;
}
