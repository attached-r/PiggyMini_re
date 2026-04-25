package ai.service;

import account.dto.CreateAccountRequest;
import ai.client.AccountServiceClient;
import ai.client.TransactionServiceClient;
import ai.config.PromptConfig;
import ai.dto.*;
import ai.util.JsonExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.AiClassifyMessage;
import common.enums.AccountType;
import common.enums.ExpenseCategory;
import common.enums.TransactionType;
import common.exception.GlobalException;
import common.model.Result;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import transaction.dto.CreateTransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * lxy: AI 服务类
 * 提供智能记账分类、自然语言查询、消费分析及流式对话等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ClassifyAssistant classifyAssistant;  // lxy: 智能分类
    private final QueryAssistant queryAssistant; // lxy: 智能查询
    private final AnalyzeAssistant analyzeAssistant; // lxy: 智能分析
    private final ChatAssistant chatAssistant; // lxy: 流式对话
    private final PromptConfig promptProperties; // lxy: 提示语
    private final ObjectMapper objectMapper; // lxy: JSON转换
    private final TransactionServiceClient transactionServiceClient; // lxy: 交易服务
    private final AccountServiceClient accountServiceClient; // lxy: 账户服务
    private final RabbitTemplate rabbitTemplate; // lxy: 消息队列
    private final RedisUtil redisUtil;

    /**
     * lxy: 智能记账 - 单条流水分类（同步）
     * 注意：此方法仅返回 AI 分类结果，不再自动保存交易记录
     * 前端收到分类结果后，可让用户选择账户，然后调用保存接口
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

            // lxy: 不再自动保存交易记录，仅返回分类结果
            // 前端应根据用户选择的账户调用保存接口

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
     * lxy: 保存交易记录到数据库
     */
    @SuppressWarnings("unchecked")
    private void saveTransaction(Long userId, ClassifyResponse result) {
        try {
            // lxy: 动态获取或创建用户默认账户
            Long accountId = getOrCreateDefaultAccountId(userId);
            if (accountId == null) {
                // lxy: 无法获取账户，跳过保存但不影响 AI 分类结果返回
                log.warn("无法获取账户 ID，跳过保存交易记录，userId: {}", userId);
                return;
            }

            // lxy: 构建创建交易请求
            CreateTransactionRequest createRequest = new CreateTransactionRequest();
            createRequest.setAccountId(accountId);

            // lxy: 转换交易类型（支持大小写兼容）
            String txType = result.getTransactionType();
            if (txType != null) {
                // 尝试直接转换（大写），再尝试 value 字段匹配
                try {
                    createRequest.setType(TransactionType.valueOf(txType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // 如果大写不匹配，尝试用 getByValue
                    TransactionType type = TransactionType.getByValue(txType.toLowerCase());
                    if (type == null) {
                        type = TransactionType.getByValue(txType);
                    }
                    createRequest.setType(type != null ? type : TransactionType.EXPENSE);
                }
            } else {
                createRequest.setType(TransactionType.EXPENSE);  // lxy: 默认支出
            }

            // lxy: 转换金额（确保为正数）
            if (result.getAmount() != null) {
                createRequest.setAmount(result.getAmount().abs());
            } else {
                createRequest.setAmount(BigDecimal.ZERO);
            }

            // lxy: 转换消费分类（支持大小写兼容）
            String categoryValue = result.getCategory();
            if (categoryValue != null) {
                // 尝试直接匹配，再尝试忽略大小写匹配
                ExpenseCategory category = ExpenseCategory.getByValue(categoryValue);
                if (category == null) {
                    // lxy: 尝试模糊匹配（包含中文父分类）
                    category = findCategoryByFuzzyMatch(categoryValue);
                }
                createRequest.setCategory(category);
            }

            createRequest.setDescription(result.getDescription());
            createRequest.setTradeTime(LocalDateTime.now());
            createRequest.setRemark(result.getMerchant());

            // lxy: 调用交易服务保存记录
            Result saveResult = transactionServiceClient.createTransaction(userId, createRequest);

            if (saveResult != null && saveResult.isSuccess()) {
                log.info("交易记录保存成功，userId: {}, amount: {}", userId, result.getAmount());
            } else {
                log.warn("交易记录保存失败，userId: {}, result: {}", userId, saveResult);
            }
        } catch (Exception e) {
            log.error("保存交易记录失败，userId: {}", userId, e);
        }
    }

    /**
     * lxy: 获取或创建用户默认账户
     * 优先获取用户已有账户，若无则自动创建一个"AI记账默认账户"
     */
    @SuppressWarnings("unchecked")
    private Long getOrCreateDefaultAccountId(Long userId) {
        try {
            // lxy: 尝试获取用户账户列表
            Result res = accountServiceClient.getAccounts(1, 1, userId);
            if (res != null && res.isSuccess() && res.getData() != null) {
                // lxy: 解析分页数据，获取第一个账户的 ID
                Map<String, Object> pageData = (Map<String, Object>) res.getData();
                List<Map<String, Object>> records = (List<Map<String, Object>>) pageData.get("records");
                if (records != null && !records.isEmpty()) {
                    Object idObj = records.get(0).get("id");
                    if (idObj != null) {
                        log.info("获取到用户账户 ID: {}, userId: {}", idObj, userId);
                        return ((Number) idObj).longValue();
                    }
                }
            }

            // lxy: 用户没有账户，自动创建一个默认账户
            log.warn("用户 {} 没有账户，自动创建默认账户", userId);
            CreateAccountRequest req = new CreateAccountRequest();
            req.setAccountName("AI 记账默认账户"); // lxy: 默认账户名称
            req.setAccountType(AccountType.CASH);  // lxy: 账户类型为现金账户
            req.setBalance(BigDecimal.ZERO);      // lxy: 初始余额为零

            Result createRes = accountServiceClient.createAccount(req, userId);
            if (createRes != null && createRes.isSuccess()) {
                Map<String, Object> accountData = (Map<String, Object>) createRes.getData();
                Object idObj = accountData.get("id");
                if (idObj != null) {
                    Long newAccountId = ((Number) idObj).longValue();
                    log.info("成功创建默认账户，ID: {}, userId: {}", newAccountId, userId);
                    return newAccountId;
                }
            }
        } catch (Exception e) {
            log.error("获取或创建账户失败，userId: {}", userId, e);
        }

        // lxy: 兜底策略：返回 null，让上层决定是否跳过保存
        log.error("无法获取或创建账户，userId: {}", userId);
        return null;
    }

    /**
     * lxy: 模糊匹配消费分类
     * 根据关键词在枚举的 description、value、parentCategory 中查找匹配
     */
    private ExpenseCategory findCategoryByFuzzyMatch(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        String lowerKeyword = keyword.toLowerCase();

        // lxy: 先尝试匹配父分类（餐饮、交通、购物等）
        for (ExpenseCategory category : ExpenseCategory.values()) {
            if (category.getParentCategory().toLowerCase().contains(lowerKeyword)) {
                return category;
            }
        }

        // lxy: 再尝试匹配 description 或 value
        for (ExpenseCategory category : ExpenseCategory.values()) {
            if (category.getDescription().toLowerCase().contains(lowerKeyword)
                    || category.getValue().toLowerCase().contains(lowerKeyword)) {
                return category;
            }
        }

        log.warn("未找到匹配的分类，关键字: {}", keyword);
        return null;
    }

    /**
     * lxy: 提交批量分类任务（异步MQ）
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
     * lxy: 获取任务进度
     */
    public Integer getTaskProgress(String taskId) {
        Object progress = redisUtil.get("ai:task:" + taskId + ":progress");
        return progress != null ? (Integer) progress : 0;
    }

    /**
     * lxy: 获取任务总数
     */
    public Object getTaskTotal(String taskId) {
        return redisUtil.get("ai:task:" + taskId + ":total");
    }

    /**
     * lxy: 更新任务进度（供Transaction服务回调）
     */
    public void updateTaskProgress(String taskId) {
        String key = "ai:task:" + taskId + ":progress";
        Object currentObj = redisUtil.get(key);
        int current = currentObj != null ? (Integer) currentObj : 0;
        redisUtil.set(key, current + 1, 3600, TimeUnit.SECONDS);
    }

    /**
     * lxy: 自然语言查询
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
     * lxy: 消费分析报告
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
     * lxy: 流式对话 - 支持前端打字机效果
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
     * lxy: 清除对话历史
     */
    public void clearHistory(String userId) {
        log.info("清除对话历史，userId: {}", userId);
    }

    /**
     * lxy: 获取支出统计数据
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
     * lxy: 获取收入统计数据
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

    /**
     * lxy: 组装汇总数据
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
     * lxy: 根据周期和日期计算时间范围
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
