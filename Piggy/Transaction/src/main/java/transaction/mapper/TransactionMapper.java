package transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import transaction.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 交易Mapper接口
 *
 * @author: rj
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    /**
     * 按分类统计指定时间范围内的支出总额
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List<Map<category, total>> 分类支出统计
     */
    @Select("SELECT category, SUM(amount) as total " +
            "FROM transactions " +
            "WHERE user_id = #{userId} " +
            "AND transaction_type = 'EXPENSE' " +
            "AND transaction_time >= #{startTime} " +
            "AND transaction_time < #{endTime} " +
            "AND category IS NOT NULL " +
            "GROUP BY category")
    List<Map<String, Object>> selectCategoryExpenseSum(@Param("userId") Long userId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 按分类统计指定时间范围内的收入总额
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List<Map<category, total>> 分类收入统计
     */
    @Select("SELECT category, SUM(amount) as total " +
            "FROM transactions " +
            "WHERE user_id = #{userId} " +
            "AND transaction_type = 'INCOME' " +
            "AND transaction_time >= #{startTime} " +
            "AND transaction_time < #{endTime} " +
            "AND category IS NOT NULL " +
            "GROUP BY category")
    List<Map<String, Object>> selectCategoryIncomeSum(@Param("userId") Long userId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}
