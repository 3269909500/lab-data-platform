package com.sewage.common.result;

import lombok.Data;


@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private Boolean success;

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        result.setSuccess(true);
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setSuccess(true);
        return result;
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> failure(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    /**
     * 参数错误响应
     */
    public static <T> Result<T> badRequest(String message) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
}