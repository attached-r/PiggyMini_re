package ai.service;

import ai.config.AiConfig;
import ai.dto.*;
import ai.util.JsonExtractor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final DashScopeChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final AiConfig aiConfig;

    /**
     * 智能记账 - 单条流水分类
     */
    public ClassifyResponse classify(ClassifyRequest request) {
        log.info("AI 流水分类，userId: {}, input: {}", request.getUserId(), request.getInput());

        try {
            String prompt = PromptBuilder.buildClassifyPrompt(
                    aiConfig.getPrompts().getClassify(),
                    request.getInput()
            );

            ChatResponse response = chatModel.call(
                    new Prompt(
                            new SystemMessage("你是一个专业的财务流水分析助手，只返回 JSON 格式数据。"),
                            new UserMessage(prompt)
                    )
            );

            String aiOutput = response.getResult().getOutput().getText();
            String jsonStr = JsonExtractor.extract(aiOutput);

            log.info("AI 返回：{}", jsonStr);

            ClassifyResponse result = objectMapper.readValue(jsonStr, ClassifyResponse.class);

            // 设置默认值
            if (result.getConfidence() == null) {
                result.setConfidence(0.8);
            }
            if (result.getMerchant() == null) {
                result.setMerchant("未知商户");
            }

            return result;

        } catch (JsonProcessingException e) {
            log.error("AI 返回结果解析失败", e);
            throw GlobalException.businessError("AI 解析失败，请重试");
        } catch (Exception e) {
            log.error("AI 调用异常", e);
            throw GlobalException.businessError("AI 服务异常");
        }
    }

    /**
     * 批量流水解析
     */
    public List<ClassifyResponse> batchClassify(BatchClassifyRequest request) {
        log.info("AI 批量解析，userId: {}, 数量: {}", request.getUserId(), request.getInputs().size());

        try {
            String batchText = String.join("\n", request.getInputs());
            String prompt = PromptBuilder.buildBatchPrompt(
                    aiConfig.getPrompts().getBatch(),
                    batchText
            );

            ChatResponse response = chatModel.call(
                    new Prompt(
                            new SystemMessage("你是一个专业的财务流水分析助手，返回 JSON 数组格式。"),
                            new UserMessage(prompt)
                    )
            );

            String aiOutput = response.getResult().getOutput().getText();
            String jsonStr = JsonExtractor.extract(aiOutput);

            JsonNode arrayNode = objectMapper.readTree(jsonStr);
            List<ClassifyResponse> results = new ArrayList<>();

            for (JsonNode node : arrayNode) {
                ClassifyResponse item = objectMapper.treeToValue(node, ClassifyResponse.class);
                if (item.getConfidence() == null) {
                    item.setConfidence(0.8);
                }
                results.add(item);
            }

            log.info("批量解析完成，共 {} 条", results.size());
            return results;

        } catch (Exception e) {
            log.error("批量解析失败", e);
            throw GlobalException.businessError("批量解析失败");
        }
    }

    /**
     * 自然语言查询
     */
    public String query(QueryRequest request) {
        log.info("AI 自然查询，userId: {}, query: {}", request.getUserId(), request.getQuery());

        try {
            String prompt = PromptBuilder.buildQueryPrompt(
                    aiConfig.getPrompts().getQuery(),
                    request.getUserId(),
                    request.getQuery()
            );

            ChatResponse response = chatModel.call(
                    new Prompt(
                            new SystemMessage("你是一个财务分析助手，根据用户数据回答问题。"),
                            new UserMessage(prompt)
                    )
            );

            return response.getResult().getOutput().getText();

        } catch (Exception e) {
            log.error("查询失败", e);
            throw GlobalException.businessError("查询失败");
        }
    }

    /**
     * 消费分析报告
     */
    public String analyze(AnalyzeRequest request) {
        log.info("AI 消费分析，userId: {}, period: {}", request.getUserId(), request.getPeriod());

        try {
            String prompt = PromptBuilder.buildAnalyzePrompt(
                    aiConfig.getPrompts().getAnalyze(),
                    request.getUserId(),
                    request.getPeriod(),
                    request.getData()
            );

            ChatResponse response = chatModel.call(
                    new Prompt(
                            new SystemMessage("你是一个专业的财务顾问，生成消费分析报告。"),
                            new UserMessage(prompt)
                    )
            );

            return response.getResult().getOutput().getText();

        } catch (Exception e) {
            log.error("分析失败", e);
            throw GlobalException.businessError("分析失败");
        }
    }
}
