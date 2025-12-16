package com.llzzhh.study.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultVO<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ResultVO<T> ok(T data) {
        return new ResultVO<>(200, "success", data);
    }

    public static <T> ResultVO<T> fail(String msg) {
        return new ResultVO<>(400, msg, null);
    }

    // 新增权限相关错误方法
    public static <T> ResultVO<T> unauthorized(String msg) {
        return new ResultVO<>(401, msg, null);
    }

    public static <T> ResultVO<T> forbidden(String msg) {
        return new ResultVO<>(403, msg, null);
    }

    // 新增服务端错误方法
    public static <T> ResultVO<T> serverError(String msg) {
        return new ResultVO<>(500, msg, null);
    }
}
