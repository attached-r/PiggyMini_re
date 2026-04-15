package ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PromptBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
     * 构建自然查询提示词（带真实数据）
     */
    public static String buildQueryPrompt(String template, Long userId, String query, Map<String, Object> summaryData) {
        try {
            String dataJson = objectMapper.writeValueAsString(summaryData);
            return template
                    .replace("{userId}", String.valueOf(userId))
                    .replace("{query}", query)
                    .replace("{summaryData}", dataJson);
        } catch (Exception e) {
            log.error("构建查询提示词失败", e);
            return template
                    .replace("{userId}", String.valueOf(userId))
                    .replace("{query}", query)
                    .replace("{summaryData}", "{}");
        }
    }

    /**
     * 构建消费分析提示词（带真实数据）
     */
    public static String buildAnalyzePrompt(String template, Long userId, String period, Map<String, Object> data) {
        try {
            String dataJson = objectMapper.writeValueAsString(data);
            return template
                    .replace("{userId}", String.valueOf(userId))
                    .replace("{period}", period)
                    .replace("{data}", dataJson);
        } catch (Exception e) {
            log.error("构建分析提示词失败", e);
            return template
                    .replace("{userId}", String.valueOf(userId))
                    .replace("{period}", period)
                    .replace("{data}", "{}");
        }
    }
}
