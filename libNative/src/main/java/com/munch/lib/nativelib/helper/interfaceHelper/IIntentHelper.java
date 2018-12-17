package com.munch.lib.nativelib.helper.interfaceHelper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.munch.lib.nativelib.exception.MethodCallException;
import com.munch.lib.nativelib.helper.ActivityStackHelper;

/**
 * @see ActivityStackHelper#init(Application)
 * <p>
 * Created by Munch on 2018/12/17.
 */
public interface IIntentHelper {

    default void startActivity(Class<? extends Activity> clazz) {
        Context context = getContext();
        context.startActivity(new Intent(context, clazz));
    }

    default void startActivity(Class<? extends Activity> clazz, Intent intent) {
        Context context = getContext();
        intent.setClass(context, clazz);
        context.startActivity(intent);
    }

    @NonNull
    default Intent getIntent() {
        Activity context = getContext();
        Intent intent = context.getIntent();
        if (null == intent) {
            throw new MethodCallException();
        }
        return intent;
    }

    default Activity getContext() {
        return ActivityStackHelper.getInstance().currentActivity();
    }
}
