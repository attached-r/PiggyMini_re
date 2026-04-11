package transaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.enums.TransactionType;
import common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transaction.client.AccountServiceClient;
import transaction.dto.AiClassifyRequest;
import transaction.dto.CreateTransactionRequest;
import transaction.dto.TransactionQueryRequest;
import transaction.dto.UpdateTransactionRequest;
import transaction.entity.Transaction;
import transaction.mapper.TransactionMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易记录服务实现类
 *
 * @author: rj
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper transactionMapper;
    private final AccountServiceClient accountServiceClient;

    @Override
    public Transaction createTransaction(Long userId, CreateTransactionRequest request) {
        log.info("创建交易记录, userId: {}, request: {}", userId, request);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .accountId(request.getAccountId())
                .transactionType(request.getType())
                .amount(request.getAmount())
                .category(request.getCategory())
                .transactionTime(request.getTradeTime())
                .remark(request.getRemark())
                .tags(request.getTags())
                .targetAccountId(request.getTargetAccountId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int rows = transactionMapper.insert(transaction);
        if (rows == 0) {
            throw GlobalException.businessError("创建交易记录失败");
        }

        updateAccountBalance(request.getAccountId(), request.getType(), request.getAmount());

        return transaction;
    }

    @Override
    public Page<Transaction> getTransactions(Long userId, TransactionQueryRequest queryRequest) {
        log.info("分页查询交易记录, userId: {}, queryRequest: {}", userId, queryRequest);

        Page<Transaction> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());

        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
                .eq(queryRequest.getAccountId() != null, Transaction::getAccountId, queryRequest.getAccountId())
                .eq(queryRequest.getType() != null, Transaction::getTransactionType, queryRequest.getType())
                .ge(queryRequest.getStartDate() != null, Transaction::getTransactionTime, queryRequest.getStartDate())
                .le(queryRequest.getEndDate() != null, Transaction::getTransactionTime, queryRequest.getEndDate())
                .orderByDesc(Transaction::getTransactionTime)
                .orderByDesc(Transaction::getCreateTime);

        return transactionMapper.selectPage(page, wrapper);
    }

    @Override
    public Transaction getTransactionById(Long userId, Long id) {
        log.info("查询交易记录详情, userId: {}, id: {}", userId, id);

        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null || !transaction.getUserId().equals(userId)) {
            throw GlobalException.businessError("交易记录不存在");
        }

        return transaction;
    }

    @Override
    public Transaction updateTransaction(Long userId, Long id, UpdateTransactionRequest request) {
        log.info("修改交易记录, userId: {}, id: {}, request: {}", userId, id, request);

        Transaction oldTransaction = getTransactionById(userId, id);

        TransactionType oldType = oldTransaction.getTransactionType();
        BigDecimal oldAmount = oldTransaction.getAmount();
        Long oldAccountId = oldTransaction.getAccountId();

        if (request.getAccountId() != null) {
            oldTransaction.setAccountId(request.getAccountId());
        }
        if (request.getType() != null) {
            oldTransaction.setTransactionType(request.getType());
        }
        if (request.getAmount() != null) {
            oldTransaction.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            oldTransaction.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            oldTransaction.setRemark(request.getDescription());
        }
        if (request.getTradeTime() != null) {
            oldTransaction.setTransactionTime(request.getTradeTime());
        }
        if (request.getRemark() != null) {
            oldTransaction.setRemark(request.getRemark());
        }
        if (request.getTags() != null) {
            oldTransaction.setTags(request.getTags());
        }
        if (request.getTargetAccountId() != null) {
            oldTransaction.setTargetAccountId(request.getTargetAccountId());
        }

        oldTransaction.setUpdateTime(LocalDateTime.now());

        int rows = transactionMapper.updateById(oldTransaction);
        if (rows == 0) {
            throw GlobalException.businessError("修改交易记录失败");
        }

        revertAccountBalance(oldAccountId, oldType, oldAmount);

        updateAccountBalance(oldTransaction.getAccountId(), oldTransaction.getTransactionType(), oldTransaction.getAmount());

        return oldTransaction;
    }

    @Override
    public void deleteTransaction(Long userId, Long id) {
        log.info("删除交易记录, userId: {}, id: {}", userId, id);

        Transaction transaction = getTransactionById(userId, id);

        revertAccountBalance(transaction.getAccountId(), transaction.getTransactionType(), transaction.getAmount());

        int rows = transactionMapper.deleteById(transaction.getId());
        if (rows == 0) {
            throw GlobalException.businessError("删除交易记录失败");
        }
    }

    @Override
    public String aiClassify(AiClassifyRequest request) {
        log.info("AI智能分类, text: {}", request.getText());
        return "待实现";
    }

    /**
     * 更新账户余额（创建或修改交易时调用）
     *
     * @param accountId 账户ID
     * @param type      交易类型
     * @param amount    交易金额
     */
    private void updateAccountBalance(Long accountId, TransactionType type, BigDecimal amount) {
        if (type == null || amount == null) {
            return;
        }

        BigDecimal balanceChange = switch (type) {
            case INCOME -> amount;
            case EXPENSE -> amount.negate();
            case TRANSFER -> BigDecimal.ZERO;
        };

        if (balanceChange.compareTo(BigDecimal.ZERO) != 0) {
            try {
                accountServiceClient.updateBalance(accountId, balanceChange);
            } catch (Exception e) {
                log.error("更新账户余额失败, accountId: {}, amount: {}", accountId, balanceChange, e);
                throw GlobalException.businessError("更新账户余额失败: " + e.getMessage());
            }
        }
    }

    /**
     * 回滚账户余额（删除或修改交易时调用）
     *
     * @param accountId 账户ID
     * @param type      交易类型
     * @param amount    交易金额
     */
    private void revertAccountBalance(Long accountId, TransactionType type, BigDecimal amount) {
        if (type == null || amount == null) {
            return;
        }

        BigDecimal balanceRevert = switch (type) {
            case INCOME -> amount.negate();
            case EXPENSE -> amount;
            case TRANSFER -> BigDecimal.ZERO;
        };

        if (balanceRevert.compareTo(BigDecimal.ZERO) != 0) {
            try {
                accountServiceClient.updateBalance(accountId, balanceRevert);
            } catch (Exception e) {
                log.error("回滚账户余额失败, accountId: {}, amount: {}", accountId, balanceRevert, e);
                throw GlobalException.businessError("回滚账户余额失败: " + e.getMessage());
            }
        }
    }

    @Override
    public Map<String, BigDecimal> getCategoryExpenseStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("统计分类支出, userId: {}, startTime: {}, endTime: {}", userId, startTime, endTime);

        List<Map<String, Object>> result = transactionMapper.selectCategoryExpenseSum(userId, startTime, endTime);
        return convertToCategoryAmountMap(result);
    }

    @Override
    public Map<String, BigDecimal> getCategoryIncomeStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("统计分类收入, userId: {}, startTime: {}, endTime: {}", userId, startTime, endTime);

        List<Map<String, Object>> result = transactionMapper.selectCategoryIncomeSum(userId, startTime, endTime);
        return convertToCategoryAmountMap(result);
    }

    private Map<String, BigDecimal> convertToCategoryAmountMap(List<Map<String, Object>> resultList) {
        Map<String, BigDecimal> map = new HashMap<>();
        if (resultList == null) {
            return map;
        }
        for (Map<String, Object> row : resultList) {
            String category = (String) row.get("category");
            Object totalObj = row.get("total");
            BigDecimal total = totalObj instanceof BigDecimal
                    ? (BigDecimal) totalObj
                    : new BigDecimal(totalObj.toString());
            map.put(category, total);
        }
        return map;
    }
}
