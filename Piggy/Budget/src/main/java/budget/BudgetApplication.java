package budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"budget", "common"})
public class BudgetApplication {
    public static void main(String[] args) {
        SpringApplication.run(BudgetApplication.class, args);
    }
}
