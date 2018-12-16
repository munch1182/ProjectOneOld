package com.munch.lib.nativelib.helper;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.munch.lib.nativelib.exception.MethodCallException;

/**
 * Created by Munch on 2018/12/13.
 */
public class ContextHelper {

    private static Application mApplication;

    /**
     * @see #getAppContext()
     */
    public static void init(@NonNull Application application) {
        mApplication = application;
    }

    /**
     * @see #init(Application)
     */
    @NonNull
    public static Context getAppContext() throws MethodCallException{
        if (ObjectHelper.isEmpty(mApplication)){
            throw new MethodCallException();
        }
        return mApplication;
    }
}
