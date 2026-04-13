package account.service;

import account.dto.CreateAccountRequest;
import account.dto.UpdateAccountRequest;
import account.entity.Account;
import account.mapper.AccountMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.exception.GlobalException;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final RedisUtil redisUtil;

    private static final long CACHE_EXPIRE_SECONDS = 300;

    @Override
    public Account createAccount(Long userId, CreateAccountRequest request) {
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Account::getUserId, userId)
                .eq(Account::getAccountName, request.getAccountName());
        Long count = accountMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw GlobalException.businessError("账户名称已存在");
        }

        Account account = Account.builder()
                .userId(userId)
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .balance(request.getBalance() != null ? request.getBalance() : java.math.BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "CNY")
                .icon(request.getIcon())
                .remark(request.getRemark())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : 0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        accountMapper.insert(account);

        clearAccountCache(userId, account.getId());

        return account;
    }

    @Override
    public Page<Account> getAccountsByUserId(Long userId, Integer page, Integer size) {
        String cacheKey = "account:list:" + userId;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            return (Page<Account>) cached;
        }

        Page<Account> accountPage = new Page<>(page, size);
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .orderByAsc(Account::getSortOrder)
                .orderByDesc(Account::getCreateTime);
        Page<Account> result = accountMapper.selectPage(accountPage, wrapper);

        if (result.getTotal() > 0) {
            redisUtil.set(cacheKey, result, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return result;
    }

    @Override
    public Account getAccountById(Long userId, Long id) {
        String cacheKey = "account:detail:" + id;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            Account account = (Account) cached;
            if (account.getUserId().equals(userId)) {
                return account;
            }
        }

        Account account = accountMapper.selectById(id);
        if (account == null || !account.getUserId().equals(userId)) {
            throw GlobalException.businessError("账户不存在");
        }

        redisUtil.set(cacheKey, account, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return account;
    }

    @Override
    public Account updateAccount(Long userId, Long id, UpdateAccountRequest request) {
        Account account = getAccountById(userId, id);

        if (request.getAccountName() != null && !request.getAccountName().equals(account.getAccountName())) {
            LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Account::getUserId, userId)
                    .eq(Account::getAccountName, request.getAccountName());
            if (accountMapper.selectCount(queryWrapper) > 0) {
                throw GlobalException.businessError("账户名称已存在");
            }
            account.setAccountName(request.getAccountName());
        }

        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        if (request.getBalance() != null) {
            account.setBalance(request.getBalance());
        }
        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }
        if (request.getIcon() != null) {
            account.setIcon(request.getIcon());
        }
        if (request.getRemark() != null) {
            account.setRemark(request.getRemark());
        }
        if (request.getSortOrder() != null) {
            account.setSortOrder(request.getSortOrder());
        }
        if (request.getIsDefault() != null) {
            account.setIsDefault(request.getIsDefault());
        }

        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);

        clearAccountCache(userId, id);

        return account;
    }

    @Override
    public void deleteAccount(Long userId, Long id) {
        Account account = getAccountById(userId, id);
        accountMapper.deleteById(account.getId());

        clearAccountCache(userId, id);
    }

    @Override
    public void updateBalance(Long accountId, BigDecimal amount) {
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw GlobalException.businessError("账户不存在");
        }

        int rows = accountMapper.updateBalanceById(accountId, amount);
        if (rows == 0) {
            throw GlobalException.businessError("余额更新失败");
        }

        Account updatedAccount = accountMapper.selectById(accountId);
        if (updatedAccount.getBalance().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw GlobalException.businessError("账户余额不足");
        }

        String detailCacheKey = "account:detail:" + accountId;
        redisUtil.delete(detailCacheKey);

        String listCacheKey = "account:list:" + updatedAccount.getUserId();
        redisUtil.delete(listCacheKey);
    }

    private void clearAccountCache(Long userId, Long accountId) {
        redisUtil.delete("account:list:" + userId);
        redisUtil.delete("account:detail:" + accountId);
    }
}
