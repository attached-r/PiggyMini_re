package report.client;

import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import report.dto.BudgetResponse;

import java.util.List;

@FeignClient(name = "Piggy-Budget", path = "/api/budgets",
    fallbackFactory = BudgetServiceFallbackFactory.class)
public interface BudgetServiceClient {

    @GetMapping("/current")
    Result getCurrentBudgets(@RequestHeader("X-User-Id") Long userId);
}
