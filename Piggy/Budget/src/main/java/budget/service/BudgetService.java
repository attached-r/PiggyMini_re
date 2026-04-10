package budget.service;

import budget.dto.BudgetResponse;
import budget.dto.CreateBudgetRequest;
import budget.entity.Budget;

import java.util.List;

/**
 * 预算服务接口
 *
 * @author: rj
 */
public interface BudgetService {

    /**
     * 创建或更新月度预算
     *
     * @param userId  用户ID
     * @param request 创建预算请求参数
     * @return 创建/更新后的预算对象
     */
    Budget createOrUpdateBudget(Long userId, CreateBudgetRequest request);

    /**
     * 查询当月所有预算
     *
     * @param userId 用户ID
     * @return 当月所有预算列表（包含已用金额和剩余金额）
     */
    List<BudgetResponse> getCurrentBudgets(Long userId);

    /**
     * 查询超支预警列表
     *
     * @param userId 用户ID
     * @return 超支预算列表
     */
    List<BudgetResponse> getWarningBudgets(Long userId);
}
