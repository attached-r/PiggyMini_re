package report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
/**
 * 趋势报表响应数据
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendReportResponse {

    private List<String> dates;

    private List<BigDecimal> income;

    private List<BigDecimal> expense;
}
