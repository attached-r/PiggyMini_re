package report.client;

import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
/**
 * 交易服务客户端
 */
@FeignClient(name = "Piggy-Transaction", path = "/api/transactions",
        fallbackFactory = TransactionServiceFallbackFactory.class)
public interface TransactionServiceClient {
    /**
     * 按分类统计支出（内部 RPC 接口）
     * <p>
     *
     * @param userId    用户ID
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     * @return 分类支出统计
     */
    @GetMapping("/statistics/category-expense")
    Result getCategoryExpenseStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime);
    /**
     * 获取交易记录摘要（内部 RPC 接口）
     * <p>
     *
     * @param userId    用户ID
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     * @return 交易记录摘要
     */
    @GetMapping("/statistics/summary")
    Result getTransactionSummary(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime);

    /**
     * 按分类统计收入（内部 RPC 接口）
     * <p>
     *
     * @param userId    用户ID
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     * @return 分类收入统计
     */
    @GetMapping("/statistics/category-income")
    Result getCategoryIncomeStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime);
}


