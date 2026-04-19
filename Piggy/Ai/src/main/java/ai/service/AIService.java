package ai.service;

import ai.client.TransactionServiceClient;
import ai.config.PromptConfig;
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
/**
 * AI 服务类
 * 提供智能记账分类、自然语言查询、消费分析及流式对话等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ClassifyAssistant classifyAssistant;  //智能分类
    private final QueryAssistant queryAssistant; //智能查询
    private final AnalyzeAssistant analyzeAssistant; //智能分析
    private final ChatAssistant chatAssistant; //流式对话
    private final PromptConfig promptProperties; //提示语
    private final ObjectMapper objectMapper; //JSON转换
    private final TransactionServiceClient transactionServiceClient; //交易服务
    private final RabbitTemplate rabbitTemplate; //消息队列
    private final RedisUtil redisUtil;

    /**
     * 智能记账 - 单条流水分类（同步）
     */
    public ClassifyResponse classify(ClassifyRequest request) {
        log.info("AI 流水分类，userId: {}, input: {}", request.getUserId(), request.getInput());
        try {
            String aiOutput = classifyAssistant.classify(request.getInput(),
                    promptProperties.getClassify());

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

        rabbitTemplate.convertAndSend(ai.config.RabbitMqConfig.EXCHANGE, ai.config.RabbitMqConfig.ROUTING_KEY, message);

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
            String summaryDataJson = objectMapper.writeValueAsString(summaryData);

            return queryAssistant.query(request.getQuery(), summaryDataJson, promptProperties.getQuery());

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
            String dataJson = objectMapper.writeValueAsString(analysisData);

            return analyzeAssistant.analyze(
                    "生成分析报告",
                    String.valueOf(request.getUserId()),
                    period,
                    dataJson,
                    promptProperties.getAnalyze()
            );

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
            return chatAssistant.streamChat(message, promptProperties.getChat())
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

    // ... existing code ...

    /**
     * 获取支出统计数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> getExpenseData(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Result result = transactionServiceClient.getCategoryExpenseStatistics(userId, startTime, endTime);
        if (result != null && result.getData() != null) {
            Map<String, Object> rawData = (Map<String, Object>) result.getData();
            Map<String, BigDecimal> expenseData = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawData.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    expenseData.put(entry.getKey(), new BigDecimal(value.toString()));
                } else {
                    expenseData.put(entry.getKey(), BigDecimal.ZERO);
                }
            }
            return expenseData;
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
            Map<String, Object> rawData = (Map<String, Object>) result.getData();
            Map<String, BigDecimal> incomeData = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawData.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    incomeData.put(entry.getKey(), new BigDecimal(value.toString()));
                } else {
                    incomeData.put(entry.getKey(), BigDecimal.ZERO);
                }
            }
            return incomeData;
        }
        return new HashMap<>();
    }

// ... existing code ...

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
