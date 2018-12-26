package com.munch.lib.libnative;

/**
 * Created by Munch on 2018/12/25 23:59.
 */
public class MethodException extends RuntimeException {

    private MethodException() {
        this("不应该以某种状态调用此方法触发了此异常.");
    }

    private MethodException(String message) {
        super(message);
    }

    public static MethodException defaultException() {
        return new MethodException("不应该以某种状态调用此方法触发了此异常.");
    }

    public static MethodException defaultException(String reason) {
        return new MethodException("不应该以某种状态调用此方法触发了此异常:" + reason);
    }

    public static MethodException wrongTimeException() {
        return new MethodException("在错误的时机调用此方法触发了此异常.");
    }

    public static MethodException wrongTimeException(String reason) {
        return new MethodException("在错误的时机调用此方法触发了此异常:" + reason);
    }

    public static MethodException unkownException() {
        return new MethodException("不知道发生了啥触发了此异常.");
    }
}
