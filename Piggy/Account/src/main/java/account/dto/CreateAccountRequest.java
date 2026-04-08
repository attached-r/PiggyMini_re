package account.dto;

import common.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "账户名称不能为空")
    @Size(max = 50, message = "账户名称不能超过50个字符")
    private String accountName;

    @NotNull(message = "账户类型不能为空")
    private AccountType accountType;

    @DecimalMin(value = "0.00", message = "账户余额不能为负数")
    private BigDecimal balance;

    @Size(max = 10, message = "币种不能超过10个字符")
    private String currency;

    @Size(max = 50, message = "账户图标不能超过50个字符")
    private String icon;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;

    private Integer sortOrder;

    private Integer isDefault;
}
