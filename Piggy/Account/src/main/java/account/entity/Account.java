package account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 *
 * 绑定表名称：accounts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("accounts")
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String accountName;

    private AccountType accountType;

    private BigDecimal balance;

    private String currency;

    private String icon;

    private String remark;

    private Integer sortOrder;

    private Integer isDefault;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
