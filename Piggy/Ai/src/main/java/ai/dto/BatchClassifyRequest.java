package ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchClassifyRequest {
    private List<String> inputs;
    private Long userId;
}