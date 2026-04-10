package budget.controller;

import budget.dto.BudgetResponse;
import budget.dto.CreateBudgetRequest;
import budget.entity.Budget;
import budget.service.BudgetService;
import common.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预算控制器
 *
 * @author: rj
 * @TODO: 用户ID获取应该会从token里面获取 这里写死为1
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * 创建/更新月度预算
     *
     * @param request 创建预算请求参数
     * @return 创建/更新后的预算对象
     */
    @PostMapping
    public Result createOrUpdateBudget(@Valid @RequestBody CreateBudgetRequest request) {
        Long userId = 1L;
        Budget budget = budgetService.createOrUpdateBudget(userId, request);
        return Result.success("预算设置成功", budget);
    }

    /**
     * 查询当月所有预算
     *
     * @return 当月所有预算列表
     */
    @GetMapping("/current")
    public Result getCurrentBudgets() {
        Long userId = 1L;
        List<BudgetResponse> budgets = budgetService.getCurrentBudgets(userId);
        return Result.success("查询成功", budgets);
    }

    /**
     * 超支预警列表
     *
     * @return 超支预算列表
     */
    @GetMapping("/warn")
    public Result getWarningBudgets() {
        Long userId = 1L;
        List<BudgetResponse> warningBudgets = budgetService.getWarningBudgets(userId);
        return Result.success("查询成功", warningBudgets);
    }
}
