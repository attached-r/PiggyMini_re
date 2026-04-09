package transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import transaction.entity.Transaction;
/**
 * 交易Mapper接口
 *
 * @author: rj
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

}
