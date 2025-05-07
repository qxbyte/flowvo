package org.xue.chat.rest;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public Result() {}
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }
    public static <T> Result<T> error(String msg) {
        return new Result<>(-1, msg, null);
    }
}
