package transaction.dto;

import common.enums.ExpenseCategory;
import common.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * 创建交易请求参数
 *
 * @author: rj
 */
@Data
public class CreateTransactionRequest {

    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    @NotNull(message = "交易类型不能为空")
    private TransactionType type;

    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    private BigDecimal amount;

    private ExpenseCategory category;

    private String description;

    @NotNull(message = "交易时间不能为空")
    private LocalDateTime tradeTime;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;

    @Size(max = 200, message = "标签不能超过200个字符")
    private String tags;

    private Long targetAccountId;
}
