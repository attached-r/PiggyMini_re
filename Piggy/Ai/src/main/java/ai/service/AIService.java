package ai.service;

import ai.client.TransactionServiceClient;
import ai.config.AiConfig;
import ai.config.RabbitMqConfig;
import ai.dto.*;
import ai.util.JsonExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.AiClassifyMessage;
import common.exception.GlobalException;
import common.model.Result;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final AiConfig aiConfig;
    private final TransactionServiceClient transactionServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final RedisUtil redisUtil;

    /**
     * 智能记账 - 单条流水分类（同步）
     */
    public ClassifyResponse classify(ClassifyRequest request) {
        log.info("AI 流水分类，userId: {}, input: {}", request.getUserId(), request.getInput());
        try {
            String prompt = PromptBuilder.buildClassifyPrompt(
                    aiConfig.getPrompts().getClassify(),
                    request.getInput()
            );

            String aiOutput = chatClient.prompt()
                    .system("你是一个专业的财务流水分析助手，只返回 JSON 格式数据。")
                    .user(prompt)
                    .call()
                    .content();

            String jsonStr = JsonExtractor.extract(aiOutput);
            log.info("AI 返回：{}", jsonStr);

            ClassifyResponse result = objectMapper.readValue(jsonStr, ClassifyResponse.class);

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
     * 提交批量分类任务（异步MQ）
     */
    public String submitBatchClassifyTask(BatchClassifyRequest request) {
        String taskId = UUID.randomUUID().toString().replace("-", "");

        redisUtil.set("ai:task:" + taskId + ":progress", 0, 3600, TimeUnit.SECONDS);
        redisUtil.set("ai:task:" + taskId + ":total", request.getInputs().size(), 3600, TimeUnit.SECONDS);

        AiClassifyMessage message = new AiClassifyMessage();
        message.setTaskId(taskId);
        message.setUserId(request.getUserId());
        message.setInputs(request.getInputs());

        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY, message);

        log.info("AI批量分类任务已提交, taskId: {}, count: {}", taskId, request.getInputs().size());
        return taskId;
    }

    /**
     * 获取任务进度
     */
    public Integer getTaskProgress(String taskId) {
        Object progress = redisUtil.get("ai:task:" + taskId + ":progress");
        return progress != null ? (Integer) progress : 0;
    }

    /**
     * 获取任务总数
     */
    public Object getTaskTotal(String taskId) {
        return redisUtil.get("ai:task:" + taskId + ":total");
    }

    /**
     * 更新任务进度（供Transaction服务回调）
     */
    public void updateTaskProgress(String taskId) {
        String key = "ai:task:" + taskId + ":progress";
        Object currentObj = redisUtil.get(key);
        int current = currentObj != null ? (Integer) currentObj : 0;
        redisUtil.set(key, current + 1, 3600, TimeUnit.SECONDS);
    }

    /**
     * 自然语言查询
     */
    public String query(QueryRequest request) {
        log.info("AI 自然查询，userId: {}, query: {}", request.getUserId(), request.getQuery());

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusMonths(1);

            Map<String, BigDecimal> expenseData = getExpenseData(request.getUserId(), startTime, endTime);
            Map<String, BigDecimal> incomeData = getIncomeData(request.getUserId(), startTime, endTime);

            Map<String, Object> summaryData = buildSummaryData(expenseData, incomeData, "最近1个月");

            String prompt = PromptBuilder.buildQueryPrompt(
                    aiConfig.getPrompts().getQuery(),
                    request.getUserId(),
                    request.getQuery(),
                    summaryData
            );

            return chatClient.prompt()
                    .system("你是一个财务分析助手，根据用户真实交易数据回答问题。如果数据为空，请明确告知用户。")
                    .user(prompt)
                    .call()
                    .content();

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
            String period = request.getPeriod() != null ? request.getPeriod() : "month";
            String date = request.getDate() != null ? request.getDate() :
                    YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            LocalDateTime[] timeRange = calculateTimeRange(period, date);

            Map<String, BigDecimal> expenseData = getExpenseData(request.getUserId(), timeRange[0], timeRange[1]);
            Map<String, BigDecimal> incomeData = getIncomeData(request.getUserId(), timeRange[0], timeRange[1]);

            Map<String, Object> analysisData = buildSummaryData(expenseData, incomeData, period);
            analysisData.put("date", date);

            String prompt = PromptBuilder.buildAnalyzePrompt(
                    aiConfig.getPrompts().getAnalyze(),
                    request.getUserId(),
                    period,
                    analysisData
            );

            return chatClient.prompt()
                    .system("你是一个专业的财务顾问，基于用户真实交易数据生成详细的消费分析报告，包含趋势分析、分类占比、优化建议等。")
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("分析失败", e);
            throw GlobalException.businessError("分析失败");
        }
    }

    /**
     * 流式对话 - 支持前端打字机效果
     */
    public Flux<String> streamChat(String userId, String message) {
        log.info("AI 流式对话，userId: {}, message: {}", userId, message);

        try {
            return chatClient.prompt()
                    .advisors(advisor -> advisor.param("chat_memory_conversation", "user_" + userId))
                    .user(message)
                    .stream()
                    .content()
                    .doOnNext(chunk -> log.debug("流式输出片段: {}", chunk))
                    .doOnComplete(() -> log.info("流式对话完成，userId: {}", userId))
                    .doOnError(error -> log.error("流式对话异常，userId: {}", userId, error));

        } catch (Exception e) {
            log.error("流式对话失败", e);
            return Flux.error(GlobalException.businessError("对话服务异常"));
        }
    }

    /**
     * 清除对话历史
     */
    public void clearHistory(String userId) {
        log.info("清除对话历史，userId: {}", userId);
    }

    /**
     * 获取支出统计数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> getExpenseData(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Result result = transactionServiceClient.getCategoryExpenseStatistics(userId, startTime, endTime);
        if (result != null && result.getData() != null) {
            return (Map<String, BigDecimal>) result.getData();
        }
        return new HashMap<>();
    }

    /**
     * 获取收入统计数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> getIncomeData(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Result result = transactionServiceClient.getCategoryIncomeStatistics(userId, startTime, endTime);
        if (result != null && result.getData() != null) {
            return (Map<String, BigDecimal>) result.getData();
        }
        return new HashMap<>();
    }

    /**
     * 组装汇总数据
     */
    private Map<String, Object> buildSummaryData(Map<String, BigDecimal> expenseData,
                                                 Map<String, BigDecimal> incomeData,
                                                 String period) {
        BigDecimal totalExpense = expenseData.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIncome = incomeData.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("totalIncome", totalIncome);
        data.put("totalExpense", totalExpense);
        data.put("netBalance", totalIncome.subtract(totalExpense));
        data.put("expenseByCategory", expenseData);
        data.put("incomeByCategory", incomeData);
        data.put("period", period);

        return data;
    }

    /**
     * 根据周期和日期计算时间范围
     */
    private LocalDateTime[] calculateTimeRange(String period, String date) {
        LocalDateTime startTime;
        LocalDateTime endTime;

        if ("month".equals(period)) {
            YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
            startTime = yearMonth.atDay(1).atStartOfDay();
            endTime = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        } else if ("year".equals(period)) {
            int year = Integer.parseInt(date.substring(0, 4));
            startTime = LocalDateTime.of(year, 1, 1, 0, 0);
            endTime = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        } else {
            throw GlobalException.businessError("不支持的时间周期: " + period);
        }

        return new LocalDateTime[]{startTime, endTime};
    }
}
