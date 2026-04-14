package ai.dto;

import lombok.Data;

@Data
public class AnalyzeRequest {
    private String data;
    private Long userId;
    private String period;
}