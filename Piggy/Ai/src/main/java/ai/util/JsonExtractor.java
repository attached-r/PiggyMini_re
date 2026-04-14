package ai.util;

import lombok.extern.slf4j.Slf4j;

/**
 * AI 返回文本 JSON 提取工具
 * 处理：带前缀说明、带```json、带markdown、纯JSON、混合文本等各种格式
 */
@Slf4j
public class JsonExtractor {

    /**
     * 从任意文本中安全提取 JSON 字符串
     * 优先找 {} 包裹的完整 JSON
     */
    public static String extract(String text) {
        // 1. 空值处理
        if (text == null || text.isBlank()) {
            log.warn("待提取的文本为空，返回默认空JSON");
            return "{}";
        }

        // 2. 去除无用格式：markdown代码块、json标记、换行、多余空格
        String cleanText = text.trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("json", "")
                .replace("\n", "")
                .replace("\r", "");

        try {
            // 3. 查找第一个 { 和最后一个 }
            int start = cleanText.indexOf("{");
            int end = cleanText.lastIndexOf("}");

            // 4. 边界判断
            if (start == -1 || end == -1 || start >= end) {
                log.warn("未找到有效的JSON结构，文本：{}", text);
                return "{}";
            }

            // 5. 截取JSON
            String json = cleanText.substring(start, end + 1).trim();
            log.debug("成功提取JSON：{}", json);
            return json;

        } catch (Exception e) {
            log.error("提取JSON发生异常", e);
            return "{}";
        }
    }
}