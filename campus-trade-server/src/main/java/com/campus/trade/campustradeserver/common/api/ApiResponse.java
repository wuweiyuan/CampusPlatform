package com.campus.trade.campustradeserver.common.api;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private  Integer code;
    private  String message;
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
