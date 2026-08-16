package com.campus.trade.campustradeserver.common.exception;

import com.campus.trade.campustradeserver.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception){
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(exception.getCode(), exception.getMessage(),null)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public  ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception){
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = "请求参数不合法!";
        if (fieldError != null && fieldError.getDefaultMessage() != null){
            message = fieldError.getDefaultMessage();
        }

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(400,message,null)
        );
    }
}
