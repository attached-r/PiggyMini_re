package transaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import transaction.dto.AiClassifyRequest;
import transaction.dto.CreateTransactionRequest;
import transaction.dto.TransactionQueryRequest;
import transaction.dto.UpdateTransactionRequest;
import transaction.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface TransactionService {

    /**
     * 新增交易记录
     *
     * @param userId 用户ID
     * @param request 新增交易记录参数
     * @return 新增的交易记录
     */
    Transaction createTransaction(Long userId, CreateTransactionRequest request);

    /**
     * 分页查询交易记录
     *
     * @param userId 用户ID
     * @param queryRequest 查询参数
     * @return 交易记录列表
     */
    Page<Transaction> getTransactions(Long userId, TransactionQueryRequest queryRequest);

    /**
     * 查询单条交易记录
     *
     * @param userId 用户ID
     * @param id 交易ID
     * @return 交易记录详情
     */
    Transaction getTransactionById(Long userId, Long id);

    /**
     * 修改交易记录
     *
     * @param userId 用户ID
     * @param id 交易ID
     * @param request 修改交易记录参数
     * @return 修改后的交易记录
     */
    Transaction updateTransaction(Long userId, Long id, UpdateTransactionRequest request);

    /**
     * 删除交易记录
     *
     * @param userId 用户ID
     * @param id 交易ID
     */
    void deleteTransaction(Long userId, Long id);

    /**
     * AI智能分类
     *
     * @param request 交易文本
     * @return 分类结果
     */
    String aiClassify(AiClassifyRequest request);

    /**
     * 按分类统计指定时间范围内的支出总额
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分类支出统计 Map<分类, 支出总额>
     */
    Map<String, BigDecimal> getCategoryExpenseStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}

