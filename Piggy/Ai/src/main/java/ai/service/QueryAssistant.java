package ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface QueryAssistant {

    @SystemMessage("{{prompt}}")
    String query(
            @UserMessage String query,
            @V("prompt") String prompt,
            @V("summaryData") String summaryData
    );
}
