package common.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class AiClassifyMessage implements Serializable {
    private String taskId;
    private Long userId;
    private List<String> inputs;
}