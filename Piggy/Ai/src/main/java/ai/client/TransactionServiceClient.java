package ai.client;

import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import transaction.dto.CreateTransactionRequest;

import java.time.LocalDateTime;

/**
 * lxy: 账单服务客户端
 */
@FeignClient(name = "Piggy-Transaction", path = "/api/transactions",
        fallbackFactory = TransactionServiceFallback.class)
public interface TransactionServiceClient {

    /**
     * lxy: 创建交易记录
     */
    @PostMapping
    Result createTransaction(@RequestHeader("X-User-Id") Long userId,
                            @RequestBody CreateTransactionRequest request);

    @GetMapping("/statistics/category-expense")
    Result getCategoryExpenseStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    );

    @GetMapping("/statistics/category-income")
    Result getCategoryIncomeStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    );
}
