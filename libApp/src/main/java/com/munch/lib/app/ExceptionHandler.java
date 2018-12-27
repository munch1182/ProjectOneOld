package com.munch.lib.app;

import com.munch.lib.expand.Loglog;
import com.munch.lib.libnative.exception.ExceptionCatchHandler;

/**
 * Created by Munch on 2018/12/27 20:34.
 */
public class ExceptionHandler extends ExceptionCatchHandler {

    @Override
    protected boolean handleException(Thread t, Throwable e) {
        Loglog.log(e);
        return true;
    }

    public static ExceptionCatchHandler getInstance() {
        return Singleton.INSTANCE;
    }

    private ExceptionHandler() {
    }

    private static class Singleton {
        private static ExceptionCatchHandler INSTANCE = new ExceptionHandler();
    }
}
