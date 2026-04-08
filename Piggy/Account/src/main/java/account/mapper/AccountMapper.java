package account.mapper;

import account.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 账户Mapper接口
 *
 * @author: rj
 */
@SuppressWarnings("SqlResolve")
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    @Update("UPDATE accounts SET balance = balance + #{amount}, update_time = NOW() WHERE id = #{id}")
    int updateBalanceById(@Param("id") Long id, @Param("amount") BigDecimal amount);
}