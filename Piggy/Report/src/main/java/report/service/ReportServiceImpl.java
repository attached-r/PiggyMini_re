package report.service;

import common.enums.ExpenseCategory;
import report.dto.BudgetResponse;
import common.exception.GlobalException;
import common.model.Result;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import report.client.BudgetServiceClient;
import report.client.TransactionServiceClient;
import report.dto.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionServiceClient transactionClient;
    private final BudgetServiceClient budgetClient;
    private final RedisUtil redisUtil;

    private static final long CACHE_EXPIRE_SECONDS = 300;

    @Override
    public SummaryReportResponse getSummary(Long userId, String period, String date) {
        if (userId == null) {
            throw new GlobalException("用户ID不能为空");
        }

        String cacheKey = "report:summary:" + userId + ":" + period + ":" + date;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            return (SummaryReportResponse) cached;
        }

        LocalDateTime[] timeRange = parseTimeRange(period, date);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        Result expenseResult;
        Result incomeResult;
        try {
            expenseResult = transactionClient.getCategoryExpenseStatistics(userId, startTime, endTime);
            incomeResult = transactionClient.getCategoryIncomeStatistics(userId, startTime, endTime);
        } catch (Exception e) {
            log.error("调用交易服务获取统计数据失败, userId: {}", userId, e);
            throw new GlobalException("获取交易数据失败");
        }

        BigDecimal expense = BigDecimal.ZERO;
        if (expenseResult != null && expenseResult.isSuccess() && expenseResult.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> expenseStats = (Map<String, Object>) expenseResult.getData();
            for (Object value : expenseStats.values()) {
                if (value != null) {
                    expense = expense.add(convertToBigDecimal(value));
                }
            }
        }

        BigDecimal income = BigDecimal.ZERO;
        if (incomeResult != null && incomeResult.isSuccess() && incomeResult.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> incomeStats = (Map<String, Object>) incomeResult.getData();
            for (Object value : incomeStats.values()) {
                if (value != null) {
                    income = income.add(convertToBigDecimal(value));
                }
            }
        }

        BigDecimal balance = income.subtract(expense);
        String trend = balance.compareTo(BigDecimal.ZERO) >= 0 ? "positive" : "negative";

        SummaryReportResponse response = SummaryReportResponse.builder()
                .income(income)
                .expense(expense)
                .balance(balance)
                .trend(trend)
                .build();

        redisUtil.set(cacheKey, response, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return response;
    }

    @Override
    public List<CategoryReportResponse> getCategoryReport(Long userId, String period, String date) {
        if (userId == null) {
            throw new GlobalException("用户ID不能为空");
        }

        String cacheKey = "report:category:" + userId + ":" + period + ":" + date;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            return (List<CategoryReportResponse>) cached;
        }

        LocalDateTime[] timeRange = parseTimeRange(period, date);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        Result result;
        try {
            result = transactionClient.getCategoryExpenseStatistics(userId, startTime, endTime);
        } catch (Exception e) {
            log.error("调用交易服务获取分类统计失败, userId: {}", userId, e);
            throw new GlobalException("获取交易数据失败");
        }

        if (result == null || !result.isSuccess() || result.getData() == null) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) result.getData();

        if (statistics.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal hundred = new BigDecimal("100");

        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Object value : statistics.values()) {
            if (value != null) {
                totalExpense = totalExpense.add(convertToBigDecimal(value));
            }
        }

        List<CategoryReportResponse> responses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : statistics.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                BigDecimal amount = convertToBigDecimal(value);
                String category = entry.getKey();

                double percent = totalExpense.compareTo(BigDecimal.ZERO) > 0
                        ? amount.multiply(hundred)
                        .divide(totalExpense, 2, RoundingMode.HALF_UP)
                        .doubleValue()
                        : 0.0;

                responses.add(CategoryReportResponse.builder()
                        .category(category)
                        .amount(amount)
                        .percent(percent)
                        .build());
            }
        }

        responses.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        if (!responses.isEmpty()) {
            redisUtil.set(cacheKey, responses, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return responses;
    }

    @Override
    public List<BudgetExecutionResponse> getBudgetExecution(Long userId, String month) {
        if (userId == null) {
            throw new GlobalException("用户ID不能为空");
        }

        String cacheKey = "report:budgetExecution:" + userId + ":" + month;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            return (List<BudgetExecutionResponse>) cached;
        }

        Result budgetResult;
        try {
            budgetResult = budgetClient.getCurrentBudgets(userId);
        } catch (Exception e) {
            log.error("调用预算服务获取数据失败, userId: {}", userId, e);
            throw new GlobalException("获取预算数据失败");
        }

        if (budgetResult == null || !budgetResult.isSuccess() || budgetResult.getData() == null) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawData = (List<Map<String, Object>>) budgetResult.getData();
        List<BudgetResponse> budgets = rawData.stream()
                .map(this::convertToBudgetResponse)
                .collect(Collectors.toList());
        BigDecimal hundred = new BigDecimal("100");

        List<BudgetExecutionResponse> responses = budgets.stream()
                .map(budget -> {
                    BigDecimal budgetAmount = budget.getBudget() != null ? budget.getBudget() : BigDecimal.ZERO;
                    BigDecimal usedAmount = budget.getSpent() != null ? budget.getSpent() : BigDecimal.ZERO;
                    BigDecimal remainingAmount = budgetAmount.subtract(usedAmount);

                    double executionRate = budgetAmount.compareTo(BigDecimal.ZERO) > 0
                            ? usedAmount.multiply(hundred)
                            .divide(budgetAmount, 2, RoundingMode.HALF_UP)
                            .doubleValue()
                            : 0.0;

                    Boolean isOverBudget = usedAmount.compareTo(budgetAmount) > 0;

                    return BudgetExecutionResponse.builder()
                            .category(budget.getCategory() != null ? budget.getCategory().getDescription() : "总预算")
                            .budgetAmount(budgetAmount)
                            .usedAmount(usedAmount)
                            .remainingAmount(remainingAmount)
                            .executionRate(executionRate)
                            .isOverBudget(isOverBudget)
                            .build();
                })
                .collect(Collectors.toList());

        if (!responses.isEmpty()) {
            redisUtil.set(cacheKey, responses, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return responses;
    }

    @Override
    public TrendReportResponse getTrend(Long userId, String startDate, String endDate) {
        if (userId == null || startDate == null || endDate == null) {
            throw new GlobalException("请求参数不能为空");
        }

        String cacheKey = "report:trend:" + userId + ":" + startDate + ":" + endDate;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            return (TrendReportResponse) cached;
        }

        List<String> dates;
        List<BigDecimal> incomeData;
        List<BigDecimal> expenseData;

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } catch (Exception e) {
            throw new GlobalException("日期格式错误，请使用 yyyy-MM-dd");
        }

        int estimatedSize = Math.toIntExact(start.until(end, ChronoUnit.DAYS) + 2);
        dates = new ArrayList<>(estimatedSize);
        incomeData = new ArrayList<>(estimatedSize);
        expenseData = new ArrayList<>(estimatedSize);

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            dates.add(date.format(formatter));

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            try {
                Result expenseResult = transactionClient.getCategoryExpenseStatistics(userId, dayStart, dayEnd);
                Result incomeResult = transactionClient.getCategoryIncomeStatistics(userId, dayStart, dayEnd);

                BigDecimal dayIncome = BigDecimal.ZERO;
                BigDecimal dayExpense = BigDecimal.ZERO;

                if (expenseResult != null && expenseResult.isSuccess() && expenseResult.getData() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> expenseStats = (Map<String, Object>) expenseResult.getData();
                    for (Object value : expenseStats.values()) {
                        if (value != null) {
                            dayExpense = dayExpense.add(convertToBigDecimal(value));
                        }
                    }
                }

                if (incomeResult != null && incomeResult.isSuccess() && incomeResult.getData() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> incomeStats = (Map<String, Object>) incomeResult.getData();
                    for (Object value : incomeStats.values()) {
                        if (value != null) {
                            dayIncome = dayIncome.add(convertToBigDecimal(value));
                        }
                    }
                }

                incomeData.add(dayIncome);
                expenseData.add(dayExpense);
            } catch (Exception e) {
                log.error("获取 {} 的交易统计数据失败", date.format(formatter), e);
                throw new GlobalException("获取 " + date.format(formatter) + " 的数据失败");
            }
        }

        TrendReportResponse response = TrendReportResponse.builder()
                .dates(dates)
                .income(incomeData)
                .expense(expenseData)
                .build();

        redisUtil.set(cacheKey, response, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return response;
    }

    private LocalDateTime[] parseTimeRange(String period, String date) {
        LocalDate targetDate;
        if (date.length() == 7) {
            targetDate = LocalDate.parse(date + "-01");
        } else {
            targetDate = LocalDate.parse(date);
        }
        LocalDateTime startTime;
        LocalDateTime endTime;

        switch (period.toLowerCase()) {
            case "month" -> {
                startTime = targetDate.withDayOfMonth(1).atStartOfDay();
                endTime = targetDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            }
            case "year" -> {
                startTime = targetDate.withDayOfYear(1).atStartOfDay();
                endTime = targetDate.plusYears(1).withDayOfYear(1).atStartOfDay();
            }
            default -> throw new GlobalException("不支持的时间周期: " + period);
        }

        return new LocalDateTime[]{startTime, endTime};
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return new BigDecimal(value.toString());
        } else {
            try {
                return new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                log.warn("无法转换值为BigDecimal: {}", value);
                return BigDecimal.ZERO;
            }
        }
    }

    private BudgetResponse convertToBudgetResponse(Map<String, Object> map) {
        return BudgetResponse.builder()
                .category(map.get("category") != null ? ExpenseCategory.valueOf(map.get("category").toString()) : null)
                .budget(convertToBigDecimal(map.get("budget")))
                .spent(convertToBigDecimal(map.get("spent")))
                .remain(convertToBigDecimal(map.get("remain")))
                .build();
    }
}
