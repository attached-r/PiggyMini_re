package account.controller;

import account.dto.CreateAccountRequest;
import account.dto.UpdateAccountRequest;

import account.entity.Account;
import account.service.AccountService;
import common.model.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 账户控制器
 *
 * @author: rj
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 新增账户
     *
     * @param request 创建账户请求参数
     * @return 创建的账户信息
     */
    @PostMapping
    public Result createAccount(@Valid @RequestBody CreateAccountRequest request,
                                @RequestHeader("X-User-Id") Long userId) {
        Account account = accountService.createAccount(userId, request);
        return Result.success("新增账户成功", account);
    }

    /**
     * 查询账户列表
     *
     * @param page 页码
     * @param size 页大小
     * @return 账户列表
     */
    @GetMapping
    public Result getAccounts(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestHeader("X-User-Id") Long userId) {
        Page<Account> accountPage = accountService.getAccountsByUserId(userId, page, size);
        return Result.success("查询成功", accountPage);
    }

    /**
     * 查询账户详情
     *
     * @param id 账户ID
     * @return 账户详情
     */
    @GetMapping("/{id}")
    public Result getAccountById(@PathVariable(value = "id") Long id,
                                 @RequestHeader("X-User-Id") Long userId) {
        Account account = accountService.getAccountById(userId, id);
        return Result.success("查询成功", account);
    }

    /**
     * 更新账户
     *
     * @param id 账户ID
     * @param request 更新账户请求参数
     * @return 更新的账户信息
     */
    @PutMapping("/{id}")
    public Result updateAccount(@PathVariable(value = "id") Long id,
                                @Valid @RequestBody UpdateAccountRequest request,
                                @RequestHeader("X-User-Id") Long userId) {
        Account account = accountService.updateAccount(userId, id, request);
        return Result.success("更新账户成功", account);
    }

    /**
     * 删除账户
     *
     * @param id 账户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteAccount(@PathVariable(value = "id") Long id,
                                @RequestHeader("X-User-Id") Long userId) {
        accountService.deleteAccount(userId, id);
        return Result.success("删除账户成功");
    }

    /**
     * 更新账户余额（内部 RPC 接口）
     * <p>
     * 此接口供 Transaction 服务通过 Feign 调用
     * 接口契约已定义在 AccountFeignClient
     *
     * @param accountId 账户ID
     * @param amount    变动金额
     * @return 更新结果
     */
    @PostMapping("/balance")
    public Result updateBalance(@RequestParam("accountId") Long accountId,
                                @RequestParam("amount") BigDecimal amount) {
        accountService.updateBalance(accountId, amount);
        return Result.success("余额更新成功", null);
    }
}
