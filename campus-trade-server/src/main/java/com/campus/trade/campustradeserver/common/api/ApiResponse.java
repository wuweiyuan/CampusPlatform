package com.campus.trade.campustradeserver.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一接口响应")
public class ApiResponse<T> {
    @Schema(description = "业务状态码：0 表示成功，非 0 表示错误；与 HTTP 状态码区分", example = "0")
    private  Integer code;
    @Schema(description = "操作结果或错误提示", example = "ok")
    private  String message;
    @Schema(description = "响应数据；无返回数据时为 null")
    private  T data;

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(0,"ok",data);
    }



}
