package ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    private Model model = new Model();

    private Prompts prompts = new Prompts();

    private Memory memory = new Memory();

    @Data
    public static class Model {
        private String name = "qwen-plus";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
        private Double topP = 0.8;
        private Integer topK = 50;
    }

    @Data
    public static class Prompts {
        private String classify;
        private String batch;
        private String query;
        private String analyze;
        private String chat = "你是一个友好的财务助手，可以回答用户的财务相关问题。请用简洁、专业的中文回答。";
    }

    @Data
    public static class Memory {
        private Integer maxMessages = 10;
        private Long maxTokenCount = 4000L;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public ChatMemory chatMemory() {
        log.info("创建 ChatMemory，最大消息数: {}, 最大Token数: {}",
                memory.getMaxMessages(), memory.getMaxTokenCount());

        return MessageWindowChatMemory.builder()
                .maxMessages(memory.getMaxMessages())
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        log.info("创建 ChatClient 实例，启用对话记忆功能");
        // 采用默认的会话记忆存储
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultSystem(prompts.getChat())
                .build();
    }
}
