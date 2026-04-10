package budget.dto;

import common.enums.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 预算响应数据
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    /**
     * 消费分类
     */
    private ExpenseCategory category;

    /**
     * 预算金额
     */
    private BigDecimal budget;

    /**
     * 已用金额
     */
    private BigDecimal spent;

    /**
     * 剩余金额
     */
    private BigDecimal remain;
}
