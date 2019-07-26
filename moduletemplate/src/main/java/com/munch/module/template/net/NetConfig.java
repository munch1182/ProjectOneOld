package com.munch.module.template.net;

import com.munch.module.template.BuildConfig;

/**
 * Created by Munch on 2019/7/26 13:50
 */
public class NetConfig {

    public static final int PAGE_ROWS = 20;
    public static final int PAGE_FIRST = 1;
    final static int CONNECT_TIMEOUT = 180000;
    final static int READ_TIMEOUT = 180000;
    final static int WRITE_TIMEOUT = 180000;

    private static final String BASE_URL = "";
    private static final String BASE_URL_4_TEST = "";
    /**
     * 通过编译环境取值
     */
    private static final boolean isTest = BuildConfig.BUILD_TYPE == "test";

    public static String getBaseUrl() {
        return isTest ? BASE_URL_4_TEST : BASE_URL;
    }

    public static class Code {
        public static final String SUCCESS = "0";
    }
}
