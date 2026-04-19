package ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService  // 自动装配
public interface AnalyzeAssistant {

    @SystemMessage("{{prompt}}")
    String analyze(
            @UserMessage String userMessage,
            @V("prompt") String prompt,
            @V("userId") String userId,
            @V("period") String period,
            @V("data") String data
    );
}
