package common.config;

import common.exception.GlobalException;
import common.model.Result;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author: rj
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(GlobalException.class)
    public Result handleGlobalException(GlobalException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(400, message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        return Result.error(400, message);
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}
