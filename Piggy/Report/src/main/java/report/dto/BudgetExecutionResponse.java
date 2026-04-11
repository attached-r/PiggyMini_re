package report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
/**
 * 预算执行响应数据
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetExecutionResponse {

    private String category;

    private BigDecimal budgetAmount;

    private BigDecimal usedAmount;

    private BigDecimal remainingAmount;

    private Double executionRate;

    private Boolean isOverBudget;
}
