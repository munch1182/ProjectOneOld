package com.munch.lib.nativelib.exception;

import com.munch.lib.nativelib.constant.C;

/**
 * 方法调用层面的异常
 * 出现此异常：
 * 调用了错误的方法
 * 调用的时机错误
 *
 * Created by Munch on 2018/12/16.
 */
public class MethodCallException extends RuntimeException {

    public MethodCallException() {
        this(C.Exception.ERROR_DO_WRONG_TIME,null);
    }

    public MethodCallException(String message) {
        this(message,null);
    }

    public MethodCallException(String message, Throwable cause) {
        super(message, cause);
    }


}
