package common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果
 *
 * @author: rj
 * 包含状态码，数据，消息，时间戳
 */
@Data // getter setter toString
@NoArgsConstructor // 无参构造
@AllArgsConstructor // 有参构造
public class Result {

    private Integer code;
    private Object data;
    private String message;
    private Long timestamp;

    /**
     * 成功返回（无数据）
     *
     * @return Result对象
     */
    public static Result success() {
        return new Result(200, null, "操作成功", System.currentTimeMillis());
    }

    /**
     * 成功返回（带数据）
     *
     * @param data 返回数据
     * @return Result对象
     */
    public static Result success(Object data) {
        return new Result(200, data, "操作成功", System.currentTimeMillis());
    }

    /**
     * 成功返回（自定义消息和数据）
     *
     * @param message 返回消息
     * @param data    返回数据
     * @return Result对象
     */
    public static Result success(String message, Object data) {
        return new Result(200, data, message, System.currentTimeMillis());
    }

    /**
     * 失败返回（默认消息）
     *
     * @return Result对象
     */
    public static Result error() {
        return new Result(500, null, "操作失败", System.currentTimeMillis());
    }

    /**
     * 失败返回（自定义消息）
     *
     * @param message 错误消息
     * @return Result对象
     */
    public static Result error(String message) {
        return new Result(500, null, message, System.currentTimeMillis());
    }

    /**
     * 失败返回（自定义状态码和消息）
     *
     * @param code    状态码
     * @param message 错误消息
     * @return Result对象
     */
    public static Result error(Integer code, String message) {
        return new Result(code, null, message, System.currentTimeMillis());
    }

    /**
     * 自定义返回
     *
     * @param code    状态码
     * @param data    返回数据
     * @param message 返回消息
     * @return Result对象
     */
    public static Result build(Integer code, Object data, String message) {
        return new Result(code, data, message, System.currentTimeMillis());
    }

    /**
     * 判断是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }

    /**
     * 重写toString方法
     * 避免输出对象地址
     * @return Result对象字符串
     */
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
