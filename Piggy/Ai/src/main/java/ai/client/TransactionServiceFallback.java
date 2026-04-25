package ai.client;

import common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import transaction.dto.CreateTransactionRequest;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * lxy: 降级处理
 */
@Slf4j
@Component
public class TransactionServiceFallback implements FallbackFactory<TransactionServiceClient> {

    @Override
    public TransactionServiceClient create(Throwable cause) {
        log.error("调用Transaction服务失败，使用降级数据", cause);

        return new TransactionServiceClient() {
            /**
             * lxy: 创建交易记录降级实现
             */
            @Override
            public Result createTransaction(Long userId, CreateTransactionRequest request) {
                log.warn("创建交易记录降级，userId: {}", userId);
                return Result.error("交易服务暂时不可用，AI分类结果未保存");
            }

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
