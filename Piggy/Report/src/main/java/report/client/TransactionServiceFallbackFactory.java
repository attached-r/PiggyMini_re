package report.client;

import common.exception.GlobalException;
import common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Transaction 服务 Feign 客户端降级工厂
 *
 * @author: rj
 */
@Slf4j
@Component
public class TransactionServiceFallbackFactory implements FallbackFactory<TransactionServiceClient> {

    @Override
    public TransactionServiceClient create(Throwable cause) {
        return new TransactionServiceClient() {
            @Override
            public Result getCategoryExpenseStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
                log.error("Transaction 服务熔断降级(支出统计) - userId: {}, 异常: {}", userId, cause.getMessage());
                return Result.error(503, "交易服务暂时不可用,支出统计数据无法获取");
            }

            @Override
            public Result getTransactionSummary(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
                log.error("Transaction 服务熔断降级(交易摘要) - userId: {}, 异常: {}", userId, cause.getMessage());
                return Result.error(503, "交易服务暂时不可用,交易摘要数据无法获取");
            }

            @Override
            public Result getCategoryIncomeStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
                log.error("Transaction 服务熔断降级(收入统计) - userId: {}, 异常: {}", userId, cause.getMessage());
                return Result.error(503, "交易服务暂时不可用,收入统计数据无法获取");
            }
        };
    }
}
