package com.munch.lib.libnative.helper;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.munch.lib.libnative.exception.MethodException;

import java.util.ArrayList;
import java.util.List;

/**
 * 一些各类View中常用的方法
 * <p>
 * Created by Munch on 2018/12/26 14:25.
 */
public class ViewHelper {

    private ViewHelper() {
    }

    /**
     * 一般用于一个页面需要多个输入的值的判断和获取，注意，返回值是根据传入的顺序返回的
     * <p>
     * 对传入的TextView或其子类的getText()值进行判断，
     * 如不为空，已照TextView传入的顺序放入{@link OnCheckTextViewListener#onNoEmpty(List)}的参数中并在检查完后回调
     * 如有值为空，则回调{@link OnCheckTextViewListener#onCheckEmpty(TextView)}并根据其返回值判断是否跳出
     *
     * @param listener 处理回调
     * @param views    被判断和获取的Textview及其子类
     */
    public static void checkTextViewEmpty(@NonNull OnCheckTextViewListener listener, @NonNull TextView... views) {
        String val;
        List<String> vals = new ArrayList<>(views.length);
        for (TextView view : views) {
            val = view.getText().toString().trim();
            if (val.isEmpty() && listener.onCheckEmpty(view)) {
                break;
            }
            vals.add(val);
        }
        listener.onNoEmpty(vals);
    }

    /**
     * 对传入的TextView数值根据String数组进行赋值，两个数组需要对等
     * <p>
     * 若vals中有空值，则调用{@link ObjectHelper#requireNonNull(String)}使其不为空
     *
     * @param views 需要被设置值得TextView或其子类的数组
     * @param vals  值的数组
     */
    public static void setTextViewNonVal(TextView[] views, String[] vals) {
        int len = views.length;
        if (len != vals.length) {
            throw MethodException.defaultException("View与数据不对等");
        }
        for (int i = 0; i < len; i++) {
            views[i].setText(ObjectHelper.requireNonNull(vals[i]));
        }
    }

    /**
     * 对传入的TextView数值根据String数组进行赋值，两个数组需要对等
     * <p>
     * 未进行空值判断
     *
     * @param views 需要被设置值得TextView或其子类的数组
     * @param vals  值的数组
     */
    public static void setTextViewVal(TextView[] views, String[] vals) {
        int len = views.length;
        if (len != vals.length) {
            throw MethodException.defaultException("View与数据不对等");
        }
        for (int i = 0; i < len; i++) {
            views[i].setText(vals[i]);
        }
    }


    public interface OnCheckTextViewListener {

        /**
         * @param view 数据为空的Textview或其子类
         * @return 是否跳出，不再继续检查剩余的View
         */
        boolean onCheckEmpty(TextView view);

        /**
         * @param vals 按顺序获取的Textview或其子类的值的集合
         */
        void onNoEmpty(List<String> vals);
    }
}
