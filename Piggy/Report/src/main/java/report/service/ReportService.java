package report.service;

import report.dto.*;

import java.util.List;
/**
 * 报表服务接口
 *
 * @author: rj
 */
public interface ReportService {
    /**
     * 获取总览报表
     *
     * @param userId 用户ID
     * @param period 时间段
     * @param date   查询日期
     * @return 总览报表
     */
    SummaryReportResponse getSummary(Long userId, String period, String date);

    /**
     * 获取分类报表
     *
     * @param userId 用户ID
     * @param period 时间段
     * @param date   查询日期
     * @return 分类报表
     */
    List<CategoryReportResponse> getCategoryReport(Long userId, String period, String date);

    /**
     * 获取预算执行报表
     *
     * @param userId 用户ID
     * @param month  查询月份
     * @return 预算执行报表
     */
    List<BudgetExecutionResponse> getBudgetExecution(Long userId, String month);

    /**
     * 获取趋势报表
     *
     * @param userId  用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势报表
     */
    TrendReportResponse getTrend(Long userId, String startDate, String endDate);
}
