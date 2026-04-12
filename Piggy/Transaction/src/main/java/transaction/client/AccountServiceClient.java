package transaction.client;

import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Account 服务 Feign 客户端
 * 供 Transaction 服务调用 Account 服务
 *
 * @author: rj
 */
@FeignClient(name = "Piggy-Account", path = "/api/accounts",
            fallbackFactory = AccountServiceFallback.AccountServiceFallbackFactory.class)
public interface AccountServiceClient {

    /**
     * 更新账户余额
     *
     * @param accountId 账户ID
     * @param amount    变动金额（正数为收入，负数为支出）
     * @return 更新结果
     */
    @PostMapping("/balance")
    Result updateBalance(@RequestParam("accountId") Long accountId,
                            @RequestParam("amount") BigDecimal amount);
}
