package com.LLZZHH.study.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultVO<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ResultVO<T> ok(T data) {
        return new ResultVO<T>(200, "success", data);
    }

    public static <T> ResultVO<T> fail(String msg) {
        return new ResultVO<T>(400, msg, null);
    }

    public static <T> ResultVO<T> unauthorized(String msg) {
        return new ResultVO<T>(401, msg, null);
    }

    public static <T> ResultVO<T> forbidden(String msg) {
        return new ResultVO<T>(403, msg, null);
    }

    public static <T> ResultVO<T> serverError(String msg) {
        return new ResultVO<T>(500, msg, null);
    }
}
