package budget.dto;

import common.enums.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建/更新预算请求参数
 *
 * @author: rj
 */
@Data
public class CreateBudgetRequest {

    @NotBlank(message = "月份不能为空")
    private String month;

    private ExpenseCategory category;

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    private BigDecimal amount;
}
