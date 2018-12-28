package com.munch.lib.libnative.helper;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.libnative.exception.MethodException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 不按格式转换的见 {@link ConvertHelper}
 * Created by Munch on 2018/12/28 14:57.
 */
public class FormatHelper {

    private final static String PATTERN_DEFAULT_FOR_DATE = "yyyy/MM/dd HH:mm:ss";
    private final static String PATTERN_DEFAULT_FOR_NUM = ",####,####.00";

    /**
     * 如Thu Dec 14 00:00:00 CST 2017
     */
    public final static String DATE_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

    /**
     * 输入大数字会转换为对应地区的金钱表示，并带符号
     * 例：10000000 --> ￥10,000,000.00
     * 100435000,Locale.FRANCE--> 100 435 000,00 €
     *
     * @param num    大数字
     * @param locale 地区
     * @return 格式化字符串并带金钱符号
     */
    public static String num2Money(@NonNull BigDecimal num, @Nullable Locale locale) {
        return NumberFormat.getCurrencyInstance(null == locale ? Locale.getDefault() : locale).format(num);
    }

    /**
     * 将输入的数字格式化
     * 例：787812331434 --> 7878,1233,1434.00
     *
     * @param num     数字
     * @param pattern 格式
     * @return 数字字符串
     */
    public static String num2Str(@Nullable Object num, @Nullable String pattern) {
        return new DecimalFormat(null == pattern ? PATTERN_DEFAULT_FOR_NUM : pattern).format(num);
    }

    public static final int LEVEL_BYTE = 1;
    public static final int LEVEL_KB = 2;
    public static final int LEVEL_MB = 3;
    public static final int LEVEL_GB = 4;
    public static final int LEVEL_TB = 5;
    public static final int LEVEL_AUTO = 6;

    @IntDef({LEVEL_AUTO, LEVEL_TB, LEVEL_GB, LEVEL_MB, LEVEL_KB, LEVEL_BYTE})
    public @interface Level {
    }

    /**
     * 将数字格式化为单字节格式
     *
     * @param bytes 大小
     * @param level 最大显示层级 5/0: TB, 4：GB,  3:MB,  2:KB,  1:Byte=》分布对应等级，若不够返回小数
     *              6: =>最大等级，不够则自动下降
     * @return 根据层级显示大小，如传入3，则可能返回1025MB而不会返回1GB
     * @see String#toLowerCase()
     * @see String#toUpperCase()
     */
    public static String num2Kb(BigDecimal bytes, @Level int level) {
        return num2Kb(bytes, level, level != LEVEL_AUTO);
    }

    /**
     * @param bytes 大小
     * @return 将bytes大小转为对应的最大单位
     */
    public static String num2Kb(BigDecimal bytes) {
        return num2Kb(bytes, LEVEL_AUTO);
    }

    private static String num2Kb(BigDecimal bytes, @Level int level, boolean allowLetter) {
        List<BigDecimal> bigDecimalArray = new ArrayList<>();
        switch (level) {
            case LEVEL_AUTO:
            case LEVEL_TB:
                bigDecimalArray.add(TB);
            case LEVEL_GB:
                bigDecimalArray.add(GB);
            case LEVEL_MB:
                bigDecimalArray.add(MB);
            case LEVEL_KB:
                bigDecimalArray.add(KB);
            case LEVEL_BYTE:
                bigDecimalArray.add(BYTE);
                break;
            default:
                throw MethodException.defaultException("level参数错误");
        }
        List<BigDecimal> decimals = str2Kb(bytes, bigDecimalArray, allowLetter);
        return decimals.get(decimals.size() - 1).toString() + getUnit(decimals.size());
    }

    /**
     * @param size BYTE:1 ==> TB:5
     * @return 单位值
     */
    public static String getUnit(@IntRange(from = 1, to = 5) int size) {
        String unit = "";
        switch (size) {
            case 5:
                unit = "TB";
                break;
            case 4:
                unit = "GB";
                break;
            case 3:
                unit = "MB";
                break;
            case 2:
                unit = "KB";
                break;
            case 1:
                unit = "BYTE";
                break;
        }
        return unit;
    }

    public static final BigDecimal BYTE = BigDecimal.ONE;
    public static final BigDecimal KB = BigDecimal.valueOf(1024);
    public static final BigDecimal MB = BigDecimal.valueOf(1024 * 1024);
    public static final BigDecimal GB = BigDecimal.valueOf(1024 * 1024 * 1024);
    public static final BigDecimal TB = BigDecimal.valueOf(1024 * 1024 * 1024).multiply(KB);

    /**
     * 例：1024*35+1024*1024*34+1024*1024*1024*67 = 71976389632 -> [0, 35, 34, 67]即67G34M35K0BYTE
     *
     * @param bytes       数据大小
     * @param bigDecimals 比较的单位的集合
     * @param allowLitter 当数比单位小时允许返回小数如0.0654TB，最大返回值小数点后4位，且四舍五入
     * @return 根据数据大小返回的值的集合, 从KB到TB排列，若无对应位置的数则表示比其小
     */
    @NonNull
    public static List<BigDecimal> str2Kb(BigDecimal bytes, List<BigDecimal> bigDecimals, boolean allowLitter) {
        List<BigDecimal> val = new ArrayList<>();
        for (BigDecimal unit : bigDecimals) {
            bytes = downLevel(val, bytes, unit, allowLitter);
        }
        Collections.reverse(val);
        return val;
    }

    /**
     * @param val         当前获取的值的集合
     * @param lastVal     当前经调整后符合单位值的大小
     * @param unit        单位大小
     * @param allowLitter 允许返回小数
     * @return 经当前单位后剩余的值的大小
     */
    public static BigDecimal downLevel(List<BigDecimal> val, BigDecimal lastVal, BigDecimal unit, boolean allowLitter) {
        //已经除尽，用来补充位数
        if (lastVal.compareTo(BigDecimal.ZERO) == 0) {
            val.add(lastVal);
            return lastVal;
        }
        //比单位大或等于单位
        if (lastVal.compareTo(unit) >= 0) {
            //整除值
            BigDecimal divide = lastVal.divide(unit, BigDecimal.ROUND_DOWN);
            val.add(divide);
            //去掉单位部分
            lastVal = lastVal.subtract(divide.multiply(unit));
            //当允许返回小于1的小数时
        } else if (allowLitter) {
            BigDecimal divide = lastVal.divide(unit, 4, BigDecimal.ROUND_UP);
            val.add(divide);
            return lastVal;
        }
        return lastVal;
    }


    /**
     * 解析字符串为{@link Date}
     * <p>
     * 当对date不确定时建议调用这个方法
     *
     * @param date    格式字符串
     * @param pattern 解析成的样式，不能为null
     * @return 如果解析失败，则返回当前日期
     * @throws ParseException 字符串与模板不符
     */
    @NonNull
    public static Date str2Date(@NonNull String date, @NonNull String pattern) throws ParseException {
        return new SimpleDateFormat(pattern, Locale.getDefault()).parse(date);
    }

    /**
     * 简易实现，当解析失败时返回当前日期
     * 当传入null并且不符合{@link #PATTERN_DEFAULT_FOR_DATE}时就会抛出异常
     */
    @NonNull
    public static Date str2DateNoException(@NonNull String date, @Nullable String pattern) {
        try {
            return str2Date(date, checkPatternIsNull(pattern));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    private static String checkPatternIsNull(String pattern) {
        if (null != pattern) {
            return pattern;
        }
        return PATTERN_DEFAULT_FOR_DATE;
    }

    public static String date2Str(@NonNull Date date, @Nullable String pattern) {
        return new SimpleDateFormat(checkPatternIsNull(pattern), Locale.getDefault()).format(date);
    }

    public static String time2Date(Long time, @Nullable String pattern) {
        return date2Str(new Date(time), checkPatternIsNull(pattern));
    }

    public static Long str2Time(@NonNull String date, @NonNull String pattern) throws ParseException {
        return str2Date(date, checkPatternIsNull(pattern)).getTime();
    }

    public static Long str2TimeNoException(@NonNull String date, @Nullable String pattern) {
        return str2DateNoException(date, checkPatternIsNull(pattern)).getTime();
    }

    public static FormatHelper getInstance() {
        return Singleton.INSTANCE;
    }

    private FormatHelper() {
    }

    private static class Singleton {
        private static FormatHelper INSTANCE = new FormatHelper();
    }
}
