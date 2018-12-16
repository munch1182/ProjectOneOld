package com.munch.lib.nativelib.helper;

import android.support.annotation.NonNull;

/**
 * Created by Munch on 2018/12/13.
 */
public class ObjectHelper {

    public static boolean isEmpty(Object obj) {
        return null == obj;
    }

    /**
     * @return 参数为null、为空或者某个参数为null时返回true
     */
    public static boolean hasEmpty(Object... objs) {
        if (isEmpty(objs) || objs.length == 0) {
            return true;
        }
        for (Object obj : objs) {
            if (isEmpty(obj)) {
                return true;
            }
        }
        return false;
    }

    public static boolean haveNonEmpty(Object... objs) {
        return !hasEmpty(objs);
    }

    public static boolean isNonEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 保证比较的时候不会引起空指针
     */
    public static boolean isEquals(Object obj1, Object obj2) {
        return (obj1 == obj2) || (obj1 != null && obj1.equals(obj2));
    }

    /**
     * 只有当确定参数不为null为编辑器不理解时才能调用此方法
     */
    @NonNull
    public  static <T> T requireNonNull(T obj){
        return obj;
    }
}
