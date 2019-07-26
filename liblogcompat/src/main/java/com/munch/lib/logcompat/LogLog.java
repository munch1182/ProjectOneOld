package com.munch.lib.logcompat;

import android.util.Log;
import androidx.annotation.IntRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Created by Munch on 2019/7/25 9:58
 */
public class LogLog {

    public static void log(Object... objects) {
        printFinMessage(getFinBuilder(null), objects);
    }

    public static void log(Builder builder, Object... objects) {
        printFinMessage(getFinBuilder(builder), objects);
    }

    public static class Builder {

        final static String DEF_TAG = "LogLog";

        private ThreadLocal<String> tagThreadLocal = new ThreadLocal<>();

        boolean logStack = true;
        boolean logBorder = true;
        int logType = 1;
        int stackCount = 1;
        Class<?> stackClass = LogLog.class;

        /**
         * 调用的LogLog的类，如果直接调用了此类，则无需调用，如果使用了包装类，则需要传入包装类
         */
        public Builder stackClass(Class<?> clazz) {
            stackClass = clazz;
            return this;
        }

        /**
         * 显示的栈数量
         */
        public Builder stackCount(@IntRange(from = 0) int count) {
            stackCount = count;
            return this;
        }

        /**
         * v,d,i,w,e -> 0,1,2,3,4
         */
        public Builder logType(@IntRange(from = 0, to = 4) int type) {
            logType = type;
            return this;
        }

        public Builder tag(String tag) {
            tagThreadLocal.set(tag);
            return this;
        }

        /**
         * 是否显示调用的方法
         */
        public Builder logStack(boolean log) {
            logStack = log;
            return this;
        }

        /**
         * 是否显示边框
         */
        public Builder logBorder(boolean log) {
            logBorder = log;
            return this;
        }

        public String getTag() {
            String tag = tagThreadLocal.get();
            return tag == null ? DEF_TAG : tag;
        }
    }

    private static final int MAX_LENGTH = 3000;

    private static void printFinMessage(Builder builder, Object[] any) {
        String tag = builder.getTag();
        int type = builder.logType;
        boolean border = builder.logBorder;
        //上边框
        if (border) {
            print(tag, type, Border.BORDER_TOP);
        }
        //显示调用方法
        if (builder.logStack) {
            List<String> list = getStackMessage(builder.stackCount, builder.stackClass);
            if (list.size() > 0) {
                for (String s : list) {
                    print(tag, type, border ? (Border.BORDER_START + s) : s);
                }
            }
        }
        //内容
        if (any != null && any.length != 0) {
            String str;
            if (any.length == 1) {
                //一个参数时
                str = any2Str(any[0]);
            } else {
                //多个参数
                str = any2Str(any);
            }
            if (str.length() <= MAX_LENGTH) {
                print(tag, type, border ? (String.format("%s%s", Border.BORDER_START, str)) : str);
            } else {
                List<String> strings = splitByLength(str);
                for (String s : strings) {
                    print(tag, type, border ? (String.format("%s%s", Border.BORDER_START, s)) : s);
                }
            }
        }
        //下边框
        if (border) {
            print(tag, type, Border.BORDER_BOTTOM);
        }
    }

    private static List<String> splitByLength(String str) {
        int count = str.length() / MAX_LENGTH;
        if (count == 0) {
            ArrayList<String> strings = new ArrayList<>(1);
            strings.add(str);
            return strings;
        }
        ArrayList<String> list = new ArrayList<>(count + 1);
        for (int i = 0; i < count; i++) {
            list.add(str.substring(i * MAX_LENGTH, (i + 1) * MAX_LENGTH));
        }
        list.add(str.substring(count * MAX_LENGTH));
        return list;
    }

    private static String any2Str(Object any) {
        if (any == null) {
            return Str.STR_NULL;
        }
        if (any instanceof Double) {
            return String.format("%sD", any);
        } else if (any instanceof Float) {
            return String.format("%sF", any);
        } else if (any instanceof Character) {
            return String.format("'%s'", any);
        } else if (any instanceof String) {
            return String.format("\"%s\"", any);
        } else if (any instanceof Iterable<?>) {
            StringBuilder builderTemp = new StringBuilder();
            String lastSplit = String.format("%s%s", Str.STR_SPLIT, Str.STR_BLANK);
            Iterable<?> iterable = (Iterable<?>) any;
            for (Object i : iterable) {
                builderTemp.append(any2Str(i))
                        .append(lastSplit);
            }
            String string = builderTemp.toString();
            if (string.endsWith(lastSplit)) {
                string = string.substring(0, string.lastIndexOf(lastSplit));
            }
            return String.format("[%s%s%s]", Str.STR_BLANK, string, Str.STR_BLANK);
        } else if (any instanceof int[]) {
            Integer[] objs = new Integer[((int[]) any).length];
            for (int i = 0; i < ((int[]) any).length; i++) {
                objs[i] = ((int[]) any)[i];
            }
            return any2Str(objs);
        } else if (any instanceof char[]) {
            char[] chars = (char[]) any;
            Character[] objs = new Character[chars.length];
            for (int i = 0; i < chars.length; i++) {
                objs[i] = chars[i];
            }
            return any2Str(objs);
        } else if (any instanceof double[]) {
            double[] doubles = (double[]) any;
            Double[] objs = new Double[doubles.length];
            for (int i = 0; i < doubles.length; i++) {
                objs[i] = doubles[i];
            }
            return any2Str(objs);
        } else if (any instanceof float[]) {
            float[] floats = (float[]) any;
            Float[] objs = new Float[floats.length];
            for (int i = 0; i < floats.length; i++) {
                objs[i] = floats[i];
            }
            return any2Str(objs);
        } else if (any instanceof Object[]) {
            return any2Str(Arrays.asList((Object[]) any));
        } else {
            return any.toString();
        }
    }

    /**
     * 获取调用的栈信息
     */
    private static List<String> getStackMessage(int stackCount, Class<?> stackClass) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length == 0) {
            return emptyList();
        }
        List<StackTraceElement> callStackTraceElement = getCallStackTraceElement(trace, stackCount, stackClass);
        if (callStackTraceElement.isEmpty()) {
            return emptyList();
        }
        List<String> list = new ArrayList<>(callStackTraceElement.size());
        StringBuilder builder = new StringBuilder();
        StackTraceElement stackTraceElement;
        for (int i = 0; i < callStackTraceElement.size(); i++) {
            for (int j = 0; j < i; j++) {
                builder.append(Str.STR_BLANK);
            }
            stackTraceElement = callStackTraceElement.get(i);
            list.add(
                    builder.append(stackTraceElement.getClassName())
                            .append("#")
                            .append(stackTraceElement.getMethodName())
                            .append("(")
                            .append(stackTraceElement.getFileName())
                            .append(":")
                            .append(stackTraceElement.getLineNumber())
                            .append(")")
                            .append(" in thread: ")
                            .append(Thread.currentThread().getName())
                            .toString()
            );
        }
        return list;
    }

    private static List<StackTraceElement> getCallStackTraceElement(StackTraceElement[] trace, int stackCount, Class<?> stackClass) {
        int lastIndex = -1;
        StackTraceElement element;
        for (int i = 0; i < trace.length; i++) {
            element = trace[i];
            if (element.getClassName().equals(stackClass.getCanonicalName())) {
                lastIndex = i;
            } else if (lastIndex != -1) {
                break;
            }
        }

        if (lastIndex == -1) {
            return emptyList();
        }
        lastIndex++;
        List<StackTraceElement> list;
        if (stackCount == 1) {
            list = new ArrayList<>(1);
            list.add(trace[lastIndex]);
            return list;
        }
        list = new ArrayList<>(stackCount);
        for (int i = 0; i < stackCount; i++) {
            if (trace.length <= lastIndex + i) {
                return list;
            }
            list.add(trace[lastIndex + i]);
        }
        return list;
    }

    private static void print(String tag, int type, String str) {
        switch (type) {
            case 0:
                Log.v(tag, str);
                break;
            case 1:
                Log.d(tag, str);
                break;
            case 2:
                Log.i(tag, str);
                break;
            case 3:
                Log.w(tag, str);
                break;
            case 4:
                Log.e(tag, str);
                break;
            default:
                Log.d(tag, str);
                break;
        }
    }

    private static Builder getFinBuilder(Builder builder) {
        return builder == null ? BuilderSingleton.INSTANCE : builder;
    }


    private static class BuilderSingleton {
        private static Builder INSTANCE = new Builder();
    }

    private static class Border {
        static final String BORDER_TOP =
                "╔═══════════════════════════════════════════════════════════════════════════════════════════════════";
        static final String BORDER_START = "║ ";
        static final String BORDER_BOTTOM =
                "╚═══════════════════════════════════════════════════════════════════════════════════════════════════";
    }

    private static class Str {
        static final String STR_NULL = "null";
        static final String STR_SPLIT = ",";
        static final String STR_BLANK = "  ";
    }
}
