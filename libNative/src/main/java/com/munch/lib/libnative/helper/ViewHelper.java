package com.munch.lib.libnative.helper;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.munch.lib.libnative.MethodException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Munch on 2018/12/26 14:25.
 */
public class ViewHelper {

    private ViewHelper() {
    }

    public static void checkEmpty(@NonNull OnCheckListener listener, @NonNull TextView... views) {
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

    public static void setVal(TextView[] views, String[] vals) {
        int len = views.length;
        if (len != vals.length) {
            throw MethodException.defaultException("View与数据不对等");
        }
        for (int i = 0; i < len; i++) {
            views[i].setText(vals[i]);
        }
    }


    public interface OnCheckListener {

        /**
         * @param view 数据为空的Textview或其子类
         * @return 是否继续检查剩余的View
         */
        boolean onCheckEmpty(TextView view);

        /**
         * @param vals 按顺序获取的Textview或其子类的值的集合
         */
        void onNoEmpty(List<String> vals);
    }
}
