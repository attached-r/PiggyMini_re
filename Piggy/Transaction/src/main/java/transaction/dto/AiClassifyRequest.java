package transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * @author: rj
 * Agent 分类请求参数
 */
@Data
public class AiClassifyRequest {

    @NotBlank(message = "交易文本不能为空")
    @Size(max = 500, message = "交易文本不能超过500个字符")
    private String text;
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
