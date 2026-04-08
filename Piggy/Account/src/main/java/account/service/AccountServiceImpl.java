package account.service;

import account.dto.CreateAccountRequest;
import account.dto.UpdateAccountRequest;
import account.entity.Account;
import account.mapper.AccountMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor // 创建构造函数
@Transactional(rollbackFor = Exception.class)  // 开启事务保证数据一致性
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;

    @Override
    public Account createAccount(Long userId, CreateAccountRequest request) {
        // 1. 校验账户名称是否已存在
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        // 先查用户id 再查账户名称，确保账户名称的唯一性
        queryWrapper.eq(Account::getUserId, userId)
                .eq(Account::getAccountName, request.getAccountName());
        Long count = accountMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw GlobalException.businessError("账户名称已存在");
        }

        // 2. 创建账户
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
        return account;
    }

    @Override
    public Page<Account> getAccountsByUserId(Long userId, Integer page, Integer size) {
        Page<Account> accountPage = new Page<>(page, size);
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .orderByAsc(Account::getSortOrder)
                .orderByDesc(Account::getCreateTime);
        return accountMapper.selectPage(accountPage, wrapper);
    }

    @Override
    public Account getAccountById(Long userId, Long id) {
        Account account = accountMapper.selectById(id);
        if (account == null || !account.getUserId().equals(userId)) {
            throw GlobalException.businessError("账户不存在");
        }
        return account;
    }

    @Override
    public Account updateAccount(Long userId, Long id, UpdateAccountRequest request) {
        Account account = getAccountById(userId, id);

        // 如果修改了名称，需要校验新名称是否与其他账户重复
        if (request.getAccountName() != null && !request.getAccountName().equals(account.getAccountName())) {
            LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Account::getUserId, userId)
                    .eq(Account::getAccountName, request.getAccountName());
            if (accountMapper.selectCount(queryWrapper) > 0) {
                throw GlobalException.businessError("账户名称已存在");
            }
            account.setAccountName(request.getAccountName());
        }
        // 修改账户类型
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        // 修改账户余额
        if (request.getBalance() != null) {
            account.setBalance(request.getBalance());
        }
        // 修改账户币种
        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }
        // 修改账户图标
        if (request.getIcon() != null) {
            account.setIcon(request.getIcon());
        }
        // 修改账户备注
        if (request.getRemark() != null) {
            account.setRemark(request.getRemark());
        }
        // 修改账户排序
        if (request.getSortOrder() != null) {
            account.setSortOrder(request.getSortOrder());
        }
        // 修改账户是否默认
        if (request.getIsDefault() != null) {
            account.setIsDefault(request.getIsDefault());
        }
        // 修改更新时间
        account.setUpdateTime(LocalDateTime.now());

        // 更新账户
        accountMapper.updateById(account);
        return account;
    }

    @Override
    public void deleteAccount(Long userId, Long id) {
        Account account = getAccountById(userId, id);
        accountMapper.deleteById(account.getId());
    }
}
