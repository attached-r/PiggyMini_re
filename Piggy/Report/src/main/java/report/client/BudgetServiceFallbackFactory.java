package report.client;

import common.exception.GlobalException;
import common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Budget 服务 Feign 客户端降级工厂
 *
 * @author: rj
 */
@Slf4j
@Component
public class BudgetServiceFallbackFactory implements FallbackFactory<BudgetServiceClient> {

    @Override
    public BudgetServiceClient create(Throwable cause) {
        return new BudgetServiceClient() {
            @Override
            public Result getCurrentBudgets() {
                log.error("Budget 服务熔断降级, 异常: {}", cause.getMessage());
                return Result.error(503, "预算服务暂时不可用,预算数据无法获取");
            }
        };
    }
}
