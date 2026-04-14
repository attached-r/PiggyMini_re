package ai.dto;

import lombok.Data;

@Data
public class ClassifyRequest {
    private String input;
    private Long userId;
}