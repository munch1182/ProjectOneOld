package com.munch.lib.nativelib.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;

/**
 * Created by Munch on 2018/12/16.
 */
public class ActivityStackHelper {

    private Stack<Activity> mActivityStack;

    private ActivityStackHelper() {
        mActivityStack = new Stack<>();
    }

    /**
     * registerActivityLifecycleCallbacks 此方法在Activity的super.onCreate()中调用
     */
    public void init(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                push(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                pop(activity);
            }
        });
    }

    /**
     * 从栈中移除Activity
     */
    private void pop(Activity activity) {
        mActivityStack.remove(activity);
    }

    /**
     * 结束Activity
     */
    public void finish(Activity activity) {
        pop(activity);
        activity.finish();
    }

    /**
     * 结束指定类名的Activity（如果存在）
     */
    public void finish(Class<? extends Activity> clazz) {
        Activity activity = getActivity(clazz);
        if (ObjectHelper.isNonEmpty(activity)) {
            finish(activity);
        }
    }

    public void push(Activity activity) {
        mActivityStack.push(activity);
    }

    /**
     * 获取栈顶的Activity
     */
    public Activity currentActivity() {
        return mActivityStack.peek();
    }

    public Activity getActivity(Class<? extends Activity> clazz) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass().equals(clazz)) {
                return activity;
            }
        }
        return null;
    }

    public void finishAll() {
        for (Activity activity : mActivityStack) {
            finish(activity);
        }
    }


    public static ActivityStackHelper getInstance() {
        return Singleton.sHelper;
    }

    private static class Singleton {
        private static final ActivityStackHelper sHelper = new ActivityStackHelper();
    }
}
