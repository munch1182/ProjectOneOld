package com.munch.module.template.account;

import com.munhc.lib.libnative.helper.SpHelper;

/**
 * Created by Munch on 2019/7/27 9:45
 */
public class AccountManager {

    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    private AccountBean mAccount;

    public void login(AccountBean bean) {
        SpHelper.put(KEY_ACCOUNT, convertBean2Str(bean));
        this.mAccount = bean;
    }

    public void logout() {
        this.mAccount = null;
        SpHelper.remove(KEY_ACCOUNT);
    }

    public AccountBean getAccount() {
        if (mAccount == null) {
            mAccount = convertStr2Bean(SpHelper.getVal(KEY_ACCOUNT, ""));
        }
        return mAccount;
    }

    // TODO: 2019/7/27 json转换
    private AccountBean convertStr2Bean(String val) {
        return null;
    }

    // TODO: 2019/7/27 json转换
    private String convertBean2Str(AccountBean bean) {
        return bean.toString();
    }

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final AccountManager INSTANCE = new AccountManager();
    }
}
