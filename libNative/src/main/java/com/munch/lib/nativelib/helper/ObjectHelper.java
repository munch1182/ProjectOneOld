package com.munch.lib.nativelib.helper;

/**
 * Created by Munch on 2018/12/13.
 */
public class ObjectHelper {

    public static boolean isEmpty(Object obj) {
        return null == obj;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 保证比较的时候不会引起空指针
     */
    public static boolean isEquals(Object obj1, Object obj2) {
        return !isEmpty(obj1) && obj1.equals(obj2);
    }
}
