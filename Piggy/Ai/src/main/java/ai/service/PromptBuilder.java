package ai.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromptBuilder {

    /**
     * 构建单条流水分类提示词
     */
    public static String buildClassifyPrompt(String template, String userInput) {
        return template.replace("{input}", userInput);
    }

    /**
     * 构建批量解析提示词
     */
    public static String buildBatchPrompt(String template, String batchText) {
        return template.replace("{input}", batchText);
    }

    /**
     * 构建自然查询提示词
     */
    public static String buildQueryPrompt(String template, Long userId, String query) {
        return template
                .replace("{userId}", String.valueOf(userId))
                .replace("{query}", query);
    }

    /**
     * 构建消费分析提示词
     */
    public static String buildAnalyzePrompt(String template, Long userId, String period, String data) {
        return template
                .replace("{userId}", String.valueOf(userId))
                .replace("{period}", period)
                .replace("{data}", data);
    }
}
