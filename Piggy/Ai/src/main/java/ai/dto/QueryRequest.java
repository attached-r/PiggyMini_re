package ai.dto;

import lombok.Data;

@Data
public class QueryRequest {
    private String query;
    private Long userId;
}