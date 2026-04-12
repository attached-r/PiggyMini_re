package budget.client;

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
                log.error("Transaction 服务熔断降级 - userId: {}, 异常: {}", userId, cause.getMessage());
                return Result.error(503, "交易服务暂时不可用,支出统计数据无法获取");
            }
        };
    }
}
