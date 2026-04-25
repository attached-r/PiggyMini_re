package ai.service;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AnalyzeAssistant {

    /**
     * 消费分析 - 系统消息包含完整上下文
     * @param userMessage 用户消息（固定为"生成分析报告"）
     * @param prompt 系统提示词模板
     * @param userId 用户ID
     * @param period 分析周期
     * @param data 交易数据JSON
     */
    @dev.langchain4j.service.SystemMessage("你是一个专业的财务顾问。请根据用户的消费数据生成分析报告。\n\n" +
            "【重要】你必须基于以下用户提供的数据进行分析，不要询问用户任何信息。\n\n" +
            "用户ID：{{userId}}\n" +
            "分析周期：{{period}}\n\n" +
            "【用户交易数据】：\n" +
            "{{data}}\n\n" +
            "【分析要求】：\n" +
            "1. 基于上述数据进行分析，如果数据为空也要如实说明\n" +
            "2. 包含总收支情况（总收入、总支出、结余）\n" +
            "3. 分析最大支出类别和占比\n" +
            "4. 给出简洁的理财建议\n" +
            "5. 使用表情符号美化报告\n\n" +
            "请直接生成分析报告：")
    String analyze(
            @UserMessage String userMessage,
            @V("prompt") String prompt,
            @V("userId") String userId,
            @V("period") String period,
            @V("data") String data
    );
}
