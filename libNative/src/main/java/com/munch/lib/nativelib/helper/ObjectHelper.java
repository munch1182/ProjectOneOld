package com.munch.lib.nativelib.helper;

/**
 * Created by Munch on 2018/12/13.
 */
public class ObjectHelper {

    public static boolean isEmpty(Object obj) {
        return null == obj;
    }

    /**
     * @return qi
     */
    public static boolean haveEmpty(Object... objs) {
        for (Object obj : objs) {
            if (isEmpty(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return qi
     */
    public static boolean haveNonEmpty(Object... objs) {
        return !haveEmpty(objs);
    }

    public static boolean isNonEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 保证比较的时候不会引起空指针
     */
    public static boolean isEquals(Object obj1, Object obj2) {
        return !isEmpty(obj1) && obj1.equals(obj2);
    }
}
