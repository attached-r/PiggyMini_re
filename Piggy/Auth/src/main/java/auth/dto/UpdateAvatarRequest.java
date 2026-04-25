package auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新头像请求
 *
 * @author: rj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarRequest {

    /**
     * 头像图片URL地址
     * 支持 Base64 编码的图片数据或外部图片URL
     */
    @NotBlank(message = "头像URL不能为空")
    private String avatar;
}
