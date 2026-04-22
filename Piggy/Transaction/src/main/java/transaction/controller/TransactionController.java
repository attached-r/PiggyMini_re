package transaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import transaction.dto.AiClassifyRequest;
import transaction.dto.CreateTransactionRequest;
import transaction.dto.TransactionQueryRequest;
import transaction.dto.UpdateTransactionRequest;
import transaction.entity.Transaction;
import transaction.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 交易记录控制器
 *
 * @author: rj
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
    public Result createTransaction(@Valid @RequestBody CreateTransactionRequest request,
                                    @RequestHeader("X-User-Id") Long userId) {
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
    public Result getTransactions(TransactionQueryRequest queryRequest,
                                    @RequestHeader("X-User-Id") Long userId) {
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
    public Result getTransactionById(@PathVariable("id") Long id,
                                        @RequestHeader("X-User-Id") Long userId) {
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
                                    @Valid @RequestBody UpdateTransactionRequest request,
                                    @RequestHeader("X-User-Id") Long userId) {
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
    public Result deleteTransaction(@PathVariable("id") Long id,
                                    @RequestHeader("X-User-Id") Long userId) {
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

    /**
     * 按分类统计支出（内部 RPC 接口）
     * <p>
     * 此接口供 Budget , Report服务通过 Feign 调用
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分类支出统计
     */
    @GetMapping("/statistics/category-expense")
    public Result getCategoryExpenseStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, BigDecimal> statistics = transactionService.getCategoryExpenseStatistics(userId, startTime, endTime);
        return Result.success("统计支出成功", statistics);
    }

    /**
     * 按分类统计收入（内部RPC接口）
     * <p>
     * 此接口供 Budget , Report服务通过 Feign 调用
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分类支出统计
     */
    @GetMapping("/statistics/category-income")
    public Result getCategoryIncomeStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, BigDecimal> statistics = transactionService.getCategoryIncomeStatistics(userId, startTime, endTime);
        return Result.success("统收入计成功", statistics);
    }


}