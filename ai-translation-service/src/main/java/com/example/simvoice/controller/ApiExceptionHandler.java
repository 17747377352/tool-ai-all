package com.example.simvoice.controller;

import com.example.simvoice.result.Result;
import com.example.simvoice.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBadRequest(IllegalArgumentException e) {
        log.warn("Bad request", e);
        return Result.error(HttpStatus.BAD_REQUEST.value(), "请求参数错误");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalState(IllegalStateException e) {
        log.warn("Illegal state", e);
        return Result.error(HttpStatus.BAD_REQUEST.value(), "请求参数错误");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return Result.error(ResultCode.ERROR.getCode(), "系统异常，请稍后重试");
    }
}



