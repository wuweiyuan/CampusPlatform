package com.campus.trade.campustradeserver.common.exception;

import com.campus.trade.campustradeserver.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.dao.QueryTimeoutException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({RedisConnectionFailureException.class, RedisSystemException.class,
            QueryTimeoutException.class})
    public ResponseEntity<ApiResponse<Void>> handleServiceUnavailable(Exception exception) {
        log.warn("依赖服务不可用：{}", exception.getClass().getSimpleName());
        return ResponseEntity.status(503).body(
                new ApiResponse<>(503, "服务暂时不可用，请稍后重试", null)
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception){
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(exception.getCode(), exception.getMessage(),null)
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ){
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(400,"请求参数不合法",null)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequestBodyException(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(400, "请求体不能为空或 JSON 格式不正确", null)
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public  ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception){
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = "请求参数不合法!";
        if (fieldError != null && !fieldError.isBindingFailure() && fieldError.getDefaultMessage() != null){
            message = fieldError.getDefaultMessage();
        }

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(400,message,null)
        );
    }
}
