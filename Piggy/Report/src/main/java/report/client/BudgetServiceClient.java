package report.client;

import common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import report.dto.BudgetResponse;

import java.util.List;

@FeignClient(name = "Piggy-Budget", path = "/api/budgets")
public interface BudgetServiceClient {

    @GetMapping("/current")
    Result getCurrentBudgets(@RequestParam("userId") Long userId);
}
