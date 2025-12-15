package com.example.tool.result;

/**
 * 返回码枚举
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    PARAM_ERROR(400, "参数错误"),
    TOKEN_EXPIRED(40101, "Token已过期"),
    TOKEN_INVALID(40102, "Token无效"),
    WX_LOGIN_FAILED(40001, "微信登录失败"),
    USER_NOT_FOUND(40002, "用户不存在"),
    DAILY_LIMIT_EXCEEDED(40003, "今日生成次数已达上限");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

