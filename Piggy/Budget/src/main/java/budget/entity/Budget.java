package budget.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import common.enums.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算实体类
 *
 * @author: rj
 *
 * 绑定表名称：budgets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("budgets")
public class Budget implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预算ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 预算金额
     */
    private BigDecimal budgetAmount;

    /**
     * 已用金额
     */
    private BigDecimal usedAmount;

    /**
     * 预算周期：1-周，2-月，3-年
     */
    private Integer cycleType;

    /**
     * 消费分类（为空表示总预算）
     */
    private ExpenseCategory category;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 预警阈值（百分比，如80表示80%时预警）
     */
    private Integer warningThreshold;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
