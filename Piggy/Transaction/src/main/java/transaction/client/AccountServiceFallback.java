package transaction.client;

import common.exception.GlobalException;
import common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Account 服务 Feign 客户端降级实现
 * 当 Account 服务不可用时触发降级
 *
 * @author: rj
 */
@Slf4j
@Component
public class AccountServiceFallback implements AccountServiceClient {

    @Override
    public Result updateBalance(Long accountId, BigDecimal amount) {
        log.error("Account 服务调用失败，触发降级 - accountId: {}, amount: {}", accountId, amount);
        throw GlobalException.serviceUnavailable("账户服务暂时不可用，请稍后重试");
    }

    /**
     * 降级工厂类 - 可以获取异常信息
     */
    @Slf4j
    @Component
    public static class AccountServiceFallbackFactory implements FallbackFactory<AccountServiceClient> {

        @Override
        public AccountServiceClient create(Throwable cause) {
            return new AccountServiceClient() {
                @Override
                public Result updateBalance(Long accountId, BigDecimal amount) {
                    log.error("Account 服务降级 - accountId: {}, amount: {}, 异常: {}",
                            accountId, amount, cause.getMessage(), cause);
                    throw GlobalException.serviceUnavailable("账户服务暂时不可用，请稍后重试");
                }
            };
        }
    }
}
