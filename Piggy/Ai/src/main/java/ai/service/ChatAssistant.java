package ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService
public interface ChatAssistant {

    @SystemMessage("{{prompt}}")
    Flux<String> streamChat(
            @UserMessage String message,
            @V("prompt") String prompt
    );
}
