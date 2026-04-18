package ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassifyRequest {

    @NotBlank(message = "输入内容不能为空")
    @Size(max = 500, message = "输入内容不能超过500字符")
    private String input;

    private Long userId;
}
