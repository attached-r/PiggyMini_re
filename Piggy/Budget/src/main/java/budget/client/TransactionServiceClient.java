package budget.client;

import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;


/**
 * Transaction 服务 Feign 客户端
 * 供 Budget 服务调用 Transaction 服务
 *
 * @author: rj
 */
@FeignClient(name = "Piggy-Transaction", path = "/api/transactions")
public interface TransactionServiceClient {

    /**
     * 按分类统计指定时间范围内的支出总额
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分类支出统计 Map<分类, 支出总额>
     */
    @GetMapping("/statistics/category-expense")
    Result getCategoryExpenseStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime);
}
