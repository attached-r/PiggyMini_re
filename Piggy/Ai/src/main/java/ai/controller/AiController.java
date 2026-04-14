package ai.controller;

import ai.dto.*;
import ai.service.AIService;
import common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/ai")
@RequiredArgsConstructor
public class AiController {

    private final AIService aiService;

    /**
     * 智能记账 - 单条分类
     */
    @PostMapping("/classify")
    public Result classify(@RequestBody ClassifyRequest request) {
        ClassifyResponse response = aiService.classify(request);
        return Result.success(response);
    }

    /**
     * 批量流水解析
     */
    @PostMapping("/batch-classify")
    public Result batchClassify(@RequestBody BatchClassifyRequest request) {
        List<ClassifyResponse> responses = aiService.batchClassify(request);
        return Result.success(responses);
    }

    /**
     * 自然语言查询
     */
    @PostMapping("/query")
    public Result query(@RequestBody QueryRequest request) {
        String response = aiService.query(request);
        return Result.success(response);
    }

    /**
     * 消费分析报告
     */
    @PostMapping("/analyze")
    public Result analyze(@RequestBody AnalyzeRequest request) {
        String response = aiService.analyze(request);
        return Result.success(response);
    }
}
