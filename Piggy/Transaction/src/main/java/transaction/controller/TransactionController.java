package transaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import transaction.dto.AiClassifyRequest;
import transaction.dto.CreateTransactionRequest;
import transaction.dto.TransactionQueryRequest;
import transaction.dto.UpdateTransactionRequest;
import transaction.entity.Transaction;
import transaction.service.TransactionService;

/**
 * 交易记录控制器
 *
 * @author: rj
 * @TODO: 用户ID获取应该会从token里面获取 这里写死为1
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 新增交易记录
     *
     * @param request 新增交易记录请求参数
     * @return 创建的交易记录
     */
    @PostMapping
    public Result createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        Long userId = 1L;
        Transaction transaction = transactionService.createTransaction(userId, request);
        return Result.success("新增交易记录成功", transaction);
    }

    /**
     * 分页查询交易记录
     *
     * @param queryRequest 查询参数
     * @return 交易记录列表
     */
    @GetMapping
    public Result getTransactions(TransactionQueryRequest queryRequest) {
        Long userId = 1L;
        Page<Transaction> transactionPage = transactionService.getTransactions(userId, queryRequest);
        return Result.success("查询成功", transactionPage);
    }

    /**
     * 查询单条交易记录
     *
     * @param id 交易ID
     * @return 交易记录详情
     */
    @GetMapping("/{id}")
    public Result getTransactionById(@PathVariable("id") Long id) {
        Long userId = 1L;
        Transaction transaction = transactionService.getTransactionById(userId, id);
        return Result.success("查询成功", transaction);
    }

    /**
     * 修改交易记录
     *
     * @param id 交易ID
     * @param request 修改交易记录请求参数
     * @return 修改后的交易记录
     */
    @PutMapping("/{id}")
    public Result updateTransaction(@PathVariable("id") Long id,
                                    @Valid @RequestBody UpdateTransactionRequest request) {
        Long userId = 1L;
        Transaction transaction = transactionService.updateTransaction(userId, id, request);
        return Result.success("修改交易记录成功", transaction);
    }

    /**
     * 删除交易记录
     *
     * @param id 交易ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteTransaction(@PathVariable("id") Long id) {
        Long userId = 1L;
        transactionService.deleteTransaction(userId, id);
        return Result.success("删除交易记录成功");
    }

    /**
     * AI智能分类（异步）
     *
     * @param request 交易文本
     * @return 分类结果
     */
    @PostMapping("/ai-classify")
    public Result aiClassify(@Valid @RequestBody AiClassifyRequest request) {
        String result = transactionService.aiClassify(request);
        return Result.success("AI分类成功", result);
    }
}
