package auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * 用户实体类
 *
 * @author: rj
 *
 * 绑定表名称：user
 */
@Data
@Builder  // 生成builder对象
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String phone;

    private String email;

    private String avatar;

    private String nickname;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
