package common.exception;

import lombok.Getter;

/**
 * 全局异常处理
 *
 * @author: rj
 */
@Getter // 自动生成getter方法
public class GlobalException extends RuntimeException {

    private Integer code;

    /**
     * 构造全局异常（默认500）
     *
     * @param message 异常消息
     */
    public GlobalException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造全局异常（自定义状态码）
     *
     * @param code    状态码
     * @param message 异常消息
     */
    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造全局异常（带原始异常）
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public GlobalException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    /**
     * 构造全局异常（自定义状态码和原始异常）
     *
     * @param code    状态码
     * @param message 异常消息
     * @param cause   原始异常
     */
    public GlobalException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 快速创建业务异常
     *
     * @param message 异常消息
     * @return GlobalException实例
     */
    public static GlobalException businessError(String message) {
        return new GlobalException(400, message);
    }

    /**
     * 快速创建未授权异常
     *
     * @param message 异常消息
     * @return GlobalException实例
     */
    public static GlobalException unauthorized(String message) {
        return new GlobalException(401, message);
    }

    /**
     * 快速创建禁止访问异常
     *
     * @param message 异常消息
     * @return GlobalException实例
     */
    public static GlobalException forbidden(String message) {
        return new GlobalException(403, message);
    }

    /**
     * 快速创建资源未找到异常
     *
     * @param message 异常消息
     * @return GlobalException实例
     */
    public static GlobalException notFound(String message) {
        return new GlobalException(404, message);
    }

    /**
     * 快速创建服务不可用异常（用于熔断降级场景）
     *
     * @param message 异常消息
     * @return GlobalException实例
     */
    public static GlobalException serviceUnavailable(String message) {
        return new GlobalException(503, message);
    }
}
