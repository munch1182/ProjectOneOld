package com.munch.lib.libnative;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.SupportActivity;

import com.munch.lib.libnative.exception.MethodException;
import com.munch.lib.libnative.helper.ObjectHelper;

import java.util.Stack;

/**
 * 对Activity进行栈管理，
 * 无需考虑是否为null是否为空的问题，只需判断返回值
 * 注意内存泄露
 * <p>
 * 并不对Activity的生命周期进行操作
 * <p>
 * 仅限FragmentActivity及其子类使用，对{@link Lifecycle.State}有了判断
 *
 * @see SupportActivity
 * Created by Munch on 2018/12/28 10:48.
 */
public class ActivitySupportStack {

    private Stack<FragmentActivity> mStack;

    /**
     * 自动添加和移除实例
     * <p>
     * 使用后不要重复添加或者移除
     *
     * @param application app
     * @see Application#registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks)
     */
    public void manageAuto(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (!(activity instanceof FragmentActivity)) {
                    throw MethodException.defaultException("仅限FragmentActivity子类使用");
                }
                addCurrent((FragmentActivity) activity);
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
                removeCurrent();
            }
        });
    }

    /**
     * 注意内存泄露
     *
     * @see #removeCurrent()
     */
    public void addCurrent(FragmentActivity activity) {
        mStack.push(activity);
    }

    public void clear() {
        mStack.clear();
    }


    // TODO: 2018/12/28 有没有这个必要？

    /**
     * 注意：根据类型判断无法精确寻找被启动多次的实例
     * <p>
     * 当找到的Activity处于 {@link Lifecycle.State#DESTROYED}时也会返回null
     *
     * @param clazz 获取实例的类
     * @param <T>   泛型
     * @return 获取某个activity的实例，如果不存在或者处于DESTROYED，则返回null
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends FragmentActivity> T get(@NonNull Class<T> clazz) {
        T target = null;
        for (FragmentActivity activity : mStack) {
            if (activity.getClass() == clazz) {
                target = (T) activity;
                break;
            }
        }
        if (target != null) {
            Lifecycle.State currentState = target.getLifecycle().getCurrentState();
            if (currentState == Lifecycle.State.DESTROYED) {
                target = null;
            }
        }
        return target;
    }

    /**
     * 注意内存泄露
     * 自动移除栈顶Activity
     *
     * @see #addCurrent(FragmentActivity)
     */
    public void removeCurrent() {
        if (mStack.size() == 0) {
            return;
        }
        mStack.pop();
    }

    /**
     * @return 当前栈顶Activity的引用，理论上来讲可以当作当前Context使用
     */
    @Nullable
    public SupportActivity getCurrent() {
        if (mStack.size() == 0) {
            return null;
        }
        return mStack.peek();
    }

    public static ActivitySupportStack getInstance() {
        return Singleton.INSTANCE;
    }

    private ActivitySupportStack() {
        mStack = new Stack<>();
    }

    private static class Singleton {
        private static ActivitySupportStack INSTANCE = new ActivitySupportStack();
    }
}
