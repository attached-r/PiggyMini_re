package ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface ClassifyAssistant {

    @SystemMessage("{{prompt}}")
    String classify(
            @UserMessage String input,
            @V("prompt") String prompt
    );
}
