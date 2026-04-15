package ai.controller;

import ai.dto.*;
import ai.service.AIService;
import common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/internal/ai")
@RequiredArgsConstructor
public class AiController {

    private final AIService aiService;

    /**
     * 智能记账 - 单条流水分类（同步）
     */
    @PostMapping("/classify")
    public Result classify(@RequestHeader("X-User-Id") String userId,
                           @RequestBody ClassifyRequest request) {
        request.setUserId(Long.parseLong(userId));
        ClassifyResponse response = aiService.classify(request);
        return Result.success(response);
    }

    /**
     * 批量流水解析（异步MQ）
     */
    @PostMapping("/batch-classify")
    public Result batchClassify(@RequestHeader("X-User-Id") String userId,
                                @RequestBody BatchClassifyRequest request) {
        request.setUserId(Long.parseLong(userId));
        String taskId = aiService.submitBatchClassifyTask(request);
        return Result.success("任务已提交", Map.of("taskId", taskId));
    }

    /**
     * 查询批量任务进度
     */
    @GetMapping("/batch-task/{taskId}/progress")
    public Result getProgress(@PathVariable String taskId) {
        Integer progress = aiService.getTaskProgress(taskId);
        Object total = aiService.getTaskTotal(taskId);
        return Result.success(Map.of("progress", progress, "total", total));
    }

    /**
     * 自然语言查询
     */
    @PostMapping("/query")
    public Result query(@RequestHeader("X-User-Id") String userId,
                        @RequestBody QueryRequest request) {
        request.setUserId(Long.parseLong(userId));
        String response = aiService.query(request);
        return Result.success(response);
    }

    /**
     * 消费分析报告
     */
    @PostMapping("/analyze")
    public Result analyze(@RequestHeader("X-User-Id") String userId,
                          @RequestBody AnalyzeRequest request) {
        request.setUserId(Long.parseLong(userId));
        String response = aiService.analyze(request);
        return Result.success(response);
    }

    /**
     * 流式对话 - SSE 服务器推送事件
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestHeader("X-User-Id") String userId,
                                   @RequestParam String message) {
        return aiService.streamChat(userId, message);
    }

    /**
     * 清除对话历史
     */
    @DeleteMapping("/chat/history")
    public Result clearHistory(@RequestHeader("X-User-Id") String userId) {
        aiService.clearHistory(userId);
        return Result.success("对话历史已清除");
    }
}
