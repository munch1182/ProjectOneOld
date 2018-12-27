package com.munch.lib.libnative.exception;

import com.munch.lib.libnative.helper.AppHelper;

/**
 * Created by Munch on 2018/12/27 16:11.
 */
public abstract class ExceptionCatchHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mExceptionHandler;

    public void init() {
        mExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //未处理则让系统处理
        if (!handleException(t, e)) {
            mExceptionHandler.uncaughtException(t, e);
            return;
        }
        //自行处理完退出应用
        AppHelper.exit();
    }

    protected abstract boolean handleException(Thread t, Throwable e);
}
