package transaction.dto;

import common.enums.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 交易查询参数
 *
 * @author: rj
 */
@Data
public class TransactionQueryRequest {

    private Long accountId;

    private TransactionType type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer page;

    private Integer size;
}
