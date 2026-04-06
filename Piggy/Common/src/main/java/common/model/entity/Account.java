package common.model.entity;

import common.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体类
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账户ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 账户类型
     */
    private AccountType accountType;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 币种（默认CNY）
     */
    private String currency;

    /**
     * 账户图标
     */
    private String icon;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否默认账户：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
