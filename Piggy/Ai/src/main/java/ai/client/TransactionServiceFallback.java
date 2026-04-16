package ai.client;

import common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * 降级处理
 */
@Slf4j
@Component
public class TransactionServiceFallback implements FallbackFactory<TransactionServiceClient> {

    @Override
    public TransactionServiceClient create(Throwable cause) {
        log.error("调用Transaction服务失败，使用降级数据", cause);

        return new TransactionServiceClient() {
            @Override
            public Result getCategoryExpenseStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
                return Result.success(Collections.emptyMap());
            }

            @Override
            public Result getCategoryIncomeStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
                return Result.success(Collections.emptyMap());
            }
        };
    }
}
