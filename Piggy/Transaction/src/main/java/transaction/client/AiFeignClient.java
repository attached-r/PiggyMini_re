package transaction.client;

import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
/**
 * AI 服务 Feign 客户端
 */
@FeignClient(name = "Piggy-AI", path = "/internal/ai")
public interface AiFeignClient {

    @GetMapping("/batch-task/{taskId}/progress")
    Result updateProgress(@PathVariable("taskId") String taskId);
}
