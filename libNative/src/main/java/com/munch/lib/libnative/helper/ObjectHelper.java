package com.munch.lib.libnative.helper;

import android.support.annotation.Nullable;

import com.munch.lib.libnative.exception.ExceptionCatchHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 一些常用公共方法，另见{@link Objects}
 * <p>
 * Created by Munch on 2018/12/26 13:46.
 */
public class ObjectHelper {

    private ObjectHelper() {
    }

    /**
     * @param collection 被判定的集合
     * @return 避免空指针后判断是否为空
     */
    public static boolean isNullOrEmpty(@Nullable Collection collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * @param obj 被判定的值
     * @return 是否为null
     */
    public static boolean isNull(@Nullable Object obj) {
        return null == obj;
    }

    /**
     * param obj 被判定的值
     *
     * @return 是否不为null
     */
    public static boolean nonNull(@Nullable Object obj) {
        return null != obj;
    }

    /**
     * @param objects 一些类的数组
     * @return 避免空指针后判断是否为空
     */
    public static boolean isNullOrEmpty(@Nullable Object[] objects) {
        return objects == null || objects.length == 0;
    }

    /**
     * @param a 一个值
     * @param b 另一个值
     * @return 两个类是否相同，注意，当一个类为空是必定不会返回true
     * @see Object#hashCode()
     */
    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        return a != null && a.equals(b);
    }

    /**
     * @param o 需要hash值的参数
     * @return hash值
     * @see Objects#hashCode(Object)
     * @see Object#hashCode()
     */
    public static int haseCode(@Nullable Object o) {
        return o != null ? o.hashCode() : 0;
    }

    /**
     * @param objs 需要hash值的参数
     * @return hash值
     * @see Objects#hash(Object...)
     * @see Arrays#hashCode(Object[])
     * @see List#hashCode
     */
    public static int hase(@Nullable Objects... objs) {
        return Arrays.hashCode(objs);
    }

    /**
     * @param str 不知是否为空的字符串
     * @return 判断是否为null或为空
     */
    public static boolean isEmpty(@Nullable String str) {
        return isNull(str) || str.isEmpty();
    }

    /**
     * @param str 不知是否为空的字符串
     * @return 为空则返回""，否则返回原值
     */
    public static String requireNonNull(@Nullable String str) {
        if (isEmpty(str)) {
            return "";
        }
        return str;
    }

    /**
     * @param array 不知是否为空的数组
     * @param <T>   泛型
     * @return 为空则返回一个该类型空数组，否则返回原值
     */
    public static <T> List<T> requireNonNull(@Nullable List<T> array) {
        if (isNull(array)) {
            return new ArrayList<>(0);
        }
        return array;
    }

    public static boolean isNonNull(@Nullable Object obj) {
        return !isNull(obj);
    }
}
