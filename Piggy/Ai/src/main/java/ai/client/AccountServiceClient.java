package ai.client;

import account.dto.CreateAccountRequest;
import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * lxy: 账户服务客户端
 * 用于 AI 服务获取或创建用户账户
 */
@FeignClient(name = "Piggy-Account", path = "/api/accounts")
public interface AccountServiceClient {

    /**
     * lxy: 获取用户账户列表
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（从请求头获取）
     * @return 账户分页列表
     */
    @GetMapping
    Result getAccounts(@RequestParam(value = "page", defaultValue = "1") Integer page,
                      @RequestParam(value = "size", defaultValue = "10") Integer size,
                      @RequestHeader("X-User-Id") Long userId);

    /**
     * lxy: 创建账户
     * @param request 创建账户请求
     * @param userId 用户ID（从请求头获取）
     * @return 创建结果
     */
    @PostMapping
    Result createAccount(@RequestBody CreateAccountRequest request,
                        @RequestHeader("X-User-Id") Long userId);
}
