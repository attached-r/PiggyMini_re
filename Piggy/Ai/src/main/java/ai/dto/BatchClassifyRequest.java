package ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BatchClassifyRequest {
    @NotBlank(message = "输入内容不能为空")
    @Size(max = 500, message = "输入内容不能超过500字符")
    private List<String> inputs;

    private Long userId;
}