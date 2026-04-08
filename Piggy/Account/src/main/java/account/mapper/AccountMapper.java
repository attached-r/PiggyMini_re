package account.mapper;

import account.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账户Mapper接口
 *
 * @author: rj
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
