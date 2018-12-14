package com.munch.lib.nativelib.helper;

import android.support.annotation.NonNull;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Munch on 2018/12/13.
 */
public class ViewHelper {

    /**
     * @param l 当检查到空值时的回调
     * @return 按照传入TextView的顺序返回其值的集合，当有空值时返回空数组
     */
    @NonNull
    public static String[] checkEmptyAndGetValue2(OnCheckListener l, TextView... views) {
        int len = views.length;
        String[] strings = new String[len];
        TextView viewTemp;
        for (int i = 0; i < len; i++) {
            viewTemp = views[i];
            String val = viewTemp.getText().toString();
            if (StringHelper.isEmpty(val)) {
                l.onEmpty(viewTemp);
                return new String[0];
            } else {
                strings[i] = val;
            }
        }
        return strings;
    }

    /**
     * @param l 当检查到空值时的回调，根据返回值决定是否继续检查，最后返回值的集合
     */
    public static void checkEmptyAndGetValue(OnCheckListener l, TextView... views) {
        List<String> strings = new ArrayList<>(views.length);
        for (TextView view : views) {
            String val = view.getText().toString();
            if (StringHelper.isEmpty(val) && !l.onEmpty(view)) {
                return;
            } else {
                strings.add(val);
            }
        }
        l.onVals(strings);
    }

    public interface OnCheckListener {
        /**
         * 当检查到空值时的处理回调
         *
         * @return 是否继续检测
         */
        boolean onEmpty(TextView view);

        void onVals(List<String> vals);
    }
}
