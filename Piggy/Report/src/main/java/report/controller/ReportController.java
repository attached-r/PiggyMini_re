package report.controller;

import common.model.Result;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import report.dto.*;
import report.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;
    /**
     * 获取报表
     *
     * @param userId  用户ID
     * @param period  时间周期
     * @param date    时间
     * @return 报表
     */
    @GetMapping("/summary")
    public Result getSummary(
            @RequestParam Long userId,
            @RequestParam @Pattern(regexp = "month|year", message = "period只能为month或year") String period,
            @RequestParam @NotBlank(message = "date不能为空") String date) {
        SummaryReportResponse response = reportService.getSummary(userId, period, date);
        return Result.success(response);
    }
    /**
     * 获取分类报表
     *
     * @param userId  用户ID
     * @param period  时间周期
     * @param date    时间
     * @return 分类报表
     */
    @GetMapping("/category")
    public Result getCategory(
            @RequestParam Long userId,
            @RequestParam @Pattern(regexp = "month|year", message = "period只能为month或year") String period,
            @RequestParam @NotBlank(message = "date不能为空") String date) {
        List<CategoryReportResponse> response = reportService.getCategoryReport(userId, period, date);
        return Result.success(response);
    }
    /**
     * 获取预算执行率报表
     *
     * @param userId 用户ID
     * @param month  目标月份（yyyy-MM格式）
     * @return 预算执行率响应列表
     */
    @GetMapping("/budget-execution")
    public Result getBudgetExecution(
            @RequestParam Long userId,
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month格式必须为yyyy-MM") String month) {
        List<BudgetExecutionResponse> response = reportService.getBudgetExecution(userId, month);
        return Result.success(response);
    }
    /**
     * 获取收支趋势报表
     *
     * @param userId    用户ID
     * @param startDate 开始日期（yyyy-MM-dd格式）
     * @param endDate   结束日期（yyyy-MM-dd格式）
     * @return 收支趋势报表响应
     */
    @GetMapping("/trend")
    public Result getTrend(
            @RequestParam Long userId,
            @RequestParam @NotBlank(message = "startDate不能为空") String startDate,
            @RequestParam @NotBlank(message = "endDate不能为空") String endDate) {
        TrendReportResponse response = reportService.getTrend(userId, startDate, endDate);
        return Result.success(response);
    }


}
