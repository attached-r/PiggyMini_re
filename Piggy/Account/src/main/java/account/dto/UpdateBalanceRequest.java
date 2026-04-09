package account.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateBalanceRequest {
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    @NotNull(message = "变动金额不能为空")
    private BigDecimal amount;
}
