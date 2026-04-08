import account.dto.CreateAccountRequest;
import account.dto.UpdateAccountRequest;
import account.entity.Account;
import account.mapper.AccountMapper;
import account.service.AccountServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.exception.GlobalException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static common.enums.AccountType.CASH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 账户服务层单元测试类
 *
 * @author: rj
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountTest {

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Long userId = 1L;

    /**
     * 测试1：新增账户 - 成功
     */
    @Test
    @Order(1)
    @DisplayName("新增账户-成功")
    void test_CreateAccount_Success() {
        // 准备数据
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("现金账户");
        request.setAccountType(CASH);
        request.setBalance(new BigDecimal("1000.00"));
        request.setCurrency("CNY");
        request.setRemark("日常现金");
        request.setSortOrder(1);
        request.setIsDefault(1);

        // Mock 行为：账户名称不存在
        when(accountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(accountMapper.insert(any(Account.class))).thenReturn(1);

        // 执行测试
        Account result = accountService.createAccount(userId, request);

        // 验证结果
        assertNotNull(result);
        assertEquals("现金账户", result.getAccountName());
        assertEquals(CASH, result.getAccountType());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getUpdateTime());

        // 验证方法调用
        verify(accountMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(accountMapper, times(1)).insert(any(Account.class));
    }

    /**
     * 测试2：新增账户 - 名称重复失败
     */
    @Test
    @Order(2)
    @DisplayName("新增账户-名称重复失败")
    void test_CreateAccount_Fail_NameExists() {
        // 准备数据
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("已存在账户");
        request.setAccountType(CASH);
        request.setBalance(BigDecimal.ZERO);

        // Mock 行为：账户名称已存在
        when(accountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 执行测试并验证异常
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            accountService.createAccount(userId, request);
        });

        assertEquals(400, exception.getCode());
        assertEquals("账户名称已存在", exception.getMessage());

        verify(accountMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(accountMapper, never()).insert(any(Account.class));
    }

    /**
     * 测试3：查询账户列表 - 成功
     */
    @Test
    @Order(3)
    @DisplayName("查询账户列表-成功")
    void test_GetAccountsByUserId_Success() {
        // 准备数据
        Account account = Account.builder()
                .id(1L)
                .userId(userId)
                .accountName("现金账户")
                .accountType(CASH)
                .balance(new BigDecimal("1000.00"))
                .build();

        Page<Account> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(account));
        mockPage.setTotal(1);

        when(accountMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // 执行测试
        Page<Account> result = accountService.getAccountsByUserId(userId, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("现金账户", result.getRecords().get(0).getAccountName());

        verify(accountMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    /**
     * 测试4：查询账户详情 - 成功
     */
    @Test
    @Order(4)
    @DisplayName("查询账户详情-成功")
    void test_GetAccountById_Success() {
        // 准备数据
        Account mockAccount = Account.builder()
                .id(1L)
                .userId(userId)
                .accountName("现金账户")
                .accountType(CASH)
                .balance(new BigDecimal("1000.00"))
                .build();

        when(accountMapper.selectById(1L)).thenReturn(mockAccount);

        // 执行测试
        Account result = accountService.getAccountById(userId, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("现金账户", result.getAccountName());
        assertEquals(userId, result.getUserId());

        verify(accountMapper, times(1)).selectById(1L);
    }

    /**
     * 测试5：查询账户详情 - 账户不存在
     */
    @Test
    @Order(5)
    @DisplayName("查询账户详情-账户不存在")
    void test_GetAccountById_Fail_NotFound() {
        when(accountMapper.selectById(999L)).thenReturn(null);

        GlobalException exception = assertThrows(GlobalException.class, () -> {
            accountService.getAccountById(userId, 999L);
        });

        assertEquals(400, exception.getCode());
        assertEquals("账户不存在", exception.getMessage());

        verify(accountMapper, times(1)).selectById(999L);
    }

    /**
     * 测试6：查询账户详情 - 无权访问（用户不匹配）
     */
    @Test
    @Order(6)
    @DisplayName("查询账户详情-无权访问")
    void test_GetAccountById_Fail_Unauthorized() {
        Account mockAccount = Account.builder()
                .id(1L)
                .userId(2L)
                .accountName("他人账户")
                .build();

        when(accountMapper.selectById(1L)).thenReturn(mockAccount);

        GlobalException exception = assertThrows(GlobalException.class, () -> {
            accountService.getAccountById(userId, 1L);
        });

        assertEquals(400, exception.getCode());
        assertEquals("账户不存在", exception.getMessage());

        verify(accountMapper, times(1)).selectById(1L);
    }

    /**
     * 测试7：更新账户 - 成功
     */
    @Test
    @Order(7)
    @DisplayName("更新账户-成功")
    void test_UpdateAccount_Success() {
        // 准备数据
        Account existingAccount = Account.builder()
                .id(1L)
                .userId(userId)
                .accountName("原账户名")
                .accountType(CASH)
                .balance(new BigDecimal("1000.00"))
                .currency("CNY")
                .build();

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountName("新账户名");
        request.setBalance(new BigDecimal("2000.00"));
        request.setRemark("更新备注");

        when(accountMapper.selectById(1L)).thenReturn(existingAccount);
        when(accountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(accountMapper.updateById(any(Account.class))).thenReturn(1);

        // 执行测试
        Account result = accountService.updateAccount(userId, 1L, request);

        // 验证结果
        assertNotNull(result);
        assertEquals("新账户名", result.getAccountName());
        assertEquals(new BigDecimal("2000.00"), result.getBalance());
        assertEquals("更新备注", result.getRemark());
        assertNotNull(result.getUpdateTime());

        verify(accountMapper, times(1)).selectById(1L);
        verify(accountMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(accountMapper, times(1)).updateById(any(Account.class));
    }

    /**
     * 测试8：更新账户 - 名称重复失败
     */
    @Test
    @Order(8)
    @DisplayName("更新账户-名称重复失败")
    void test_UpdateAccount_Fail_NameExists() {
        Account existingAccount = Account.builder()
                .id(1L)
                .userId(userId)
                .accountName("原账户名")
                .accountType(CASH)
                .build();

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountName("重复的账户名");

        when(accountMapper.selectById(1L)).thenReturn(existingAccount);
        when(accountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        GlobalException exception = assertThrows(GlobalException.class, () -> {
            accountService.updateAccount(userId, 1L, request);
        });

        assertEquals(400, exception.getCode());
        assertEquals("账户名称已存在", exception.getMessage());

        verify(accountMapper, times(1)).selectById(1L);
        verify(accountMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(accountMapper, never()).updateById(any(Account.class));
    }

    /**
     * 测试9：删除账户 - 成功
     */
    @Test
    @Order(9)
    @DisplayName("删除账户-成功")
    void test_DeleteAccount_Success() {
        Account existingAccount = Account.builder()
                .id(1L)
                .userId(userId)
                .accountName("待删除账户")
                .build();

        when(accountMapper.selectById(1L)).thenReturn(existingAccount);
        doNothing().when(accountMapper).deleteById(1L);

        // 执行测试
        accountService.deleteAccount(userId, 1L);

        // 验证结果
        verify(accountMapper, times(1)).selectById(1L);
        verify(accountMapper, times(1)).deleteById(1L);
    }

    /**
     * 测试10：删除账户 - 账户不存在
     */
    @Test
    @Order(10)
    @DisplayName("删除账户-账户不存在")
    void test_DeleteAccount_Fail_NotFound() {
        when(accountMapper.selectById(999L)).thenReturn(null);

        GlobalException exception = assertThrows(GlobalException.class, () -> {
            accountService.deleteAccount(userId, 999L);
        });

        assertEquals(400, exception.getCode());
        assertEquals("账户不存在", exception.getMessage());

        verify(accountMapper, times(1)).selectById(999L);
        verify(accountMapper, never()).deleteById(anyLong());
    }
}
