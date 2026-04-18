package ai.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * 聊天记忆配置类
 */
@Configuration
public class ChatMemoryConfig {

    @Value("${ai.memory.max-messages:10}")
    private int maxMessages;
    /**
     * 创建聊天记忆提供者
     * @return 聊天记忆提供者
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(maxMessages)
                .build();
    }
}
