package ai.dto;

import lombok.Data;

@Data
public class AnalyzeRequest {
    private String date;
    private Long userId;
    private String period;
}