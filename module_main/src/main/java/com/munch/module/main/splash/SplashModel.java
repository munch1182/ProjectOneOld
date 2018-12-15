package com.munch.module.main.splash;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.base.model.BaseModel;

/**
 * Created by Munch on 2018/12/16.
 */
public class SplashModel extends BaseModel<SplashBean> implements SplashContract.Model {

    @Nullable
    @Override
    public SplashBean getData(@Nullable OnDataListener<SplashBean> l, @Nullable Object... obj) {
        return super.getData(l, obj);
    }

    @Override
    public void saveData(@Nullable SplashBean splashBean, @NonNull OnDataListener<SplashBean> l) {
        super.saveData(splashBean, l);
    }

    private SplashModel() {
    }

    public static SplashModel newInstance(){
        return new SplashModel();
    }
}
