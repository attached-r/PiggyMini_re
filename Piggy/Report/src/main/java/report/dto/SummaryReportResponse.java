package report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
/**
 * 报表响应数据
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryReportResponse {

    private BigDecimal income;

    private BigDecimal expense;

    private BigDecimal balance;

    private String trend;
}
