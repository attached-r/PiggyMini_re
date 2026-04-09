package transaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import common.enums.ExpenseCategory;
import common.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录实体类
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("transactions")
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 交易ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 交易类型（支出/收入/转账）
     */
    private TransactionType transactionType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 消费分类
     */
    private ExpenseCategory category;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标签（多个标签用逗号分隔）
     */
    private String tags;

    /**
     * 转账目标账户ID（仅转账类型使用）
     */
    private Long targetAccountId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
