package com.munch.module.main.splash;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.rootmvp.BaseRootPresenter;
import com.munch.lib.nativelib.mpvpack.OnDataListener;
import com.munch.lib.nativelib.helper.ConvertHelper;

/**
 * Created by Munch on 2018/12/16.
 */
public class SplashPresenter extends BaseRootPresenter<SplashBean,SplashContract.View,
        SplashContract.Model> implements SplashContract.Present{

    @Override
    public void start() {
        putModel(getModelInstance())
                .getData(new OnDataListener<SplashBean>() {
                    @Override
                    public void onDataSuccess(SplashBean splashBean) {
                    }

                    @Override
                    public void onDataFail(int code, Object... parameters) {
                    }
                });
    }

    @Override
    public void start(int type, @Nullable Object... parameters) {
        super.start(type, parameters);
        putModel(getModelInstance())
                .getData(new OnDataListener<SplashBean>() {
                    @Override
                    public void onDataSuccess(SplashBean splashBean) {

                    }

                    @Override
                    public void onDataFail(int code, @Nullable Object... parameters) {

                    }
                }, parameters);
    }

    @NonNull
    private SplashRootModel getModelInstance() {
        return SplashRootModel.newInstance();
    }

    @Override
    public void modifyData(int type, @Nullable Object... parameters) {
        super.modifyData(type, parameters);
        getModel().saveData(ConvertHelper.<SplashBean>dataConvert(type, parameters), new OnDataListener<SplashBean>() {
            @Override
            public void onDataSuccess(SplashBean splashBean) {
            }

            @Override
            public void onDataFail(int code, @Nullable Object... parameters) {
            }
        });
    }

}
