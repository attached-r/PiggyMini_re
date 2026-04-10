package budget.service;

import budget.client.TransactionServiceClient;
import budget.dto.BudgetResponse;
import budget.dto.CreateBudgetRequest;
import budget.entity.Budget;
import budget.mapper.BudgetMapper;
import common.model.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 预算服务实现类
 *
 * @author: rj
 * @TODO: 后续会给每个微服务做降级熔断处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetMapper budgetMapper;
    private final TransactionServiceClient transactionServiceClient;
    /**
     * 创建或更新预算
     *
     * @param userId  用户ID
     * @param request 创建预算请求参数
     * @return 创建/更新后的预算对象
     *
     * @Author: rj
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Budget createOrUpdateBudget(Long userId, CreateBudgetRequest request) {
        log.info("创建/更新预算, userId: {}, request: {}", userId, request);
        // 1.获取当月的开始时间和结束时间
        LocalDate monthDate = LocalDate.parse(request.getMonth() + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startTime = monthDate.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endTime = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(23, 59, 59);

        // 2. 复用已抽取的范围查询方法
        List<Budget> budgets = queryBudgetsByTimeRange(userId, startTime, endTime);

        // 3. 过滤出匹配分类的预算
        Budget existBudget = budgets.stream()
                .filter(b -> {
                    if (request.getCategory() == null) {
                        return b.getCategory() == null;
                    }
                    return request.getCategory().equals(b.getCategory());
                })
                .findFirst()
                .orElse(null);
        // 4. 如果存在则更新
        if (existBudget != null) {
            existBudget.setBudgetAmount(request.getAmount());
            existBudget.setUpdateTime(LocalDateTime.now());
            budgetMapper.updateById(existBudget);
            log.info("更新预算成功, budgetId: {}", existBudget.getId());
            return existBudget;
        } else {
            // 5. 否则 创建预算
            Budget budget = Budget.builder()
                    .userId(userId)
                    .budgetAmount(request.getAmount())
                    .usedAmount(BigDecimal.ZERO)
                    .cycleType(2)
                    .category(request.getCategory())
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(1)
                    .warningThreshold(80)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            budgetMapper.insert(budget);
            log.info("创建预算成功, budgetId: {}", budget.getId());
            return budget;
        }
    }
    /**
     * 查询当月所有预算
     *
     * @param userId 用户ID
     * @return 当月所有预算列表（包含已用金额和剩余金额）
     *
     * @Author: rj
     */
    @Override
    public List<BudgetResponse> getCurrentBudgets(Long userId) {
        log.info("查询当月所有预算, userId: {}", userId);

        LocalDateTime[] timeRange = getCurrentMonthTimeRange();
        List<Budget> budgets = queryBudgetsByTimeRange(userId, timeRange[0], timeRange[1]);
        Map<String, BigDecimal> categoryExpenseMap = getCategoryExpenseStatistics(userId, timeRange[0], timeRange[1]);
        // 构建预算响应列表
        return buildBudgetResponses(budgets, categoryExpenseMap, false);
    }

    /**
     * 查询超支预警列表
     *
     * @param userId 用户ID
     * @return 超支预算列表
     *
     * @Author: rj
     */
    @Override
    public List<BudgetResponse> getWarningBudgets(Long userId) {
        log.info("查询超支预警列表, userId: {}", userId);

        LocalDateTime[] timeRange = getCurrentMonthTimeRange();
        List<Budget> budgets = queryBudgetsByTimeRange(userId, timeRange[0], timeRange[1]);
        Map<String, BigDecimal> categoryExpenseMap = getCategoryExpenseStatistics(userId, timeRange[0], timeRange[1]);

        return buildBudgetResponses(budgets, categoryExpenseMap, true);
    }

    // 获取当前月份的开始时间和结束时间
    private LocalDateTime[] getCurrentMonthTimeRange() {
        LocalDate now = LocalDate.now();
        LocalDateTime startTime = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endTime = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);
        return new LocalDateTime[]{startTime, endTime};
    }
    // 查询指定时间范围内的预算
    private List<Budget> queryBudgetsByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId)
                .le(Budget::getStartTime, endTime)        // 预算开始时间 <= 查询结束时间
                .ge(Budget::getEndTime, startTime)        // 预算结束时间 >= 查询开始时间
                .eq(Budget::getStatus, 1)
                .orderByDesc(Budget::getCategory)
                .orderByDesc(Budget::getCreateTime);
        return budgetMapper.selectList(wrapper);
    }

    // 获取指定时间范围内的分类消费统计
    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> getCategoryExpenseStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Result result = transactionServiceClient.getCategoryExpenseStatistics(userId, startTime, endTime);
        if (result != null && result.getCode() == 200 && result.getData() != null) {
            return (Map<String, BigDecimal>) result.getData();
        }
        return new HashMap<>();
    }

    // 构建预算响应列表
    private List<BudgetResponse> buildBudgetResponses(List<Budget> budgets, Map<String, BigDecimal> categoryExpenseMap, boolean onlyWarning) {
        List<BudgetResponse> responses = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spent = calculateSpentAmount(budget, categoryExpenseMap);

            if (onlyWarning) {
                if (!isWarningBudget(budget, spent)) {
                    continue;
                }
            }

            BigDecimal remain = budget.getBudgetAmount().subtract(spent);

            responses.add(BudgetResponse.builder()
                    .category(budget.getCategory())
                    .budget(budget.getBudgetAmount())
                    .spent(spent)
                    .remain(remain)
                    .build());
        }

        return responses;
    }
    // 计算已用金额
    private BigDecimal calculateSpentAmount(Budget budget, Map<String, BigDecimal> categoryExpenseMap) {
        if (budget.getCategory() != null) {
            return categoryExpenseMap.getOrDefault(budget.getCategory().name(), BigDecimal.ZERO);
        } else {
            return categoryExpenseMap.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
    // 判断是否为超支预算
    private boolean isWarningBudget(Budget budget, BigDecimal spent) {
        if (budget.getWarningThreshold() == null || budget.getBudgetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal thresholdAmount = budget.getBudgetAmount()
                .multiply(BigDecimal.valueOf(budget.getWarningThreshold()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return spent.compareTo(thresholdAmount) >= 0;
    }
}
