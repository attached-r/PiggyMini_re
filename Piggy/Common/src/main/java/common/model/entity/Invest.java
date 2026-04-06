package common.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资实体类
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 投资ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 投资名称
     */
    private String investName;

    /**
     * 投资类型（基金/股票/理财/定期等）
     */
    private String investType;

    /**
     * 投资金额
     */
    private BigDecimal investAmount;

    /**
     * 当前价值
     */
    private BigDecimal currentValue;

    /**
     * 收益率（百分比）
     */
    private BigDecimal yieldRate;

    /**
     * 投资时间
     */
    private LocalDateTime investTime;

    /**
     * 到期时间（定期投资使用）
     */
    private LocalDateTime maturityTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：0-已赎回，1-持有中
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
