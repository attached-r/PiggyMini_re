package account.service;

import account.dto.CreateAccountRequest;
import account.dto.UpdateAccountRequest;
import account.entity.Account;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;

public interface AccountService {
    /**
     * 新增账户
     *
     * @param userId 用户ID
     * @param request 新增账户参数
     * @return 新增的账户信息
     */
    Account createAccount(Long userId, CreateAccountRequest request);
    /**
     * 查询账户列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 页大小
     * @return 账户列表
     */
    Page<Account> getAccountsByUserId(Long userId, Integer page, Integer size);
    /**
     * 查询账户详情
     *
     * @param userId 用户ID
     * @param id 账户ID
     * @return 账户详情
     */
    Account getAccountById(Long userId, Long id);
    /**
     * 更新账户信息
     *
     * @param userId 用户ID
     * @param id 账户ID
     * @param request 更新账户参数
     * @return 更新的账户信息
     */
    Account updateAccount(Long userId, Long id, UpdateAccountRequest request);
    /**
     * 删除账户
     *
     * @param userId 用户ID
     * @param id 账户ID
     */
    void deleteAccount(Long userId, Long id);

    /**
     * 更新账户余额（供 Transaction 服务调用）
     *
     * @param accountId 账户ID
     * @param amount    变动金额（正数为收入，负数为支出）
     */
    void updateBalance(Long accountId, BigDecimal amount);
}
