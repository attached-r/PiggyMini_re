package ai.dto;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClassifyResponse {
    private BigDecimal amount;
    private String category;
    private String description;
    private String merchant;
    private String transactionType;
    private Double confidence;
}