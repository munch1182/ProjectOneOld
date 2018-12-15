package com.munch.module.main.splash;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.base.model.IModel;
import com.munch.lib.nativelib.base.presenter.BasePresenter;

/**
 * Created by Munch on 2018/12/16.
 */
public class SplashPresenter extends BasePresenter<SplashBean,SplashContract.View, SplashContract.Model>
        implements SplashContract.Present {

    @Override
    public void start() {
        putModel(getModelInstance())
                .getData(new IModel.OnDataListener<SplashBean>() {
            @Override
            public void onDataSuccess(SplashBean splashBean) {
                getNonNullView().loadData(splashBean);
            }

            @Override
            public void onDataFail(int code, Object... parameters) {
                getNonNullView().loadDataFail(parameters);
            }
        });
    }

    @Override
    public void start(int type, @Nullable Object... parameters) {
        super.start(type, parameters);
        putModel(getModelInstance())
                .getData(new IModel.OnDataListener<SplashBean>() {
            @Override
            public void onDataSuccess(SplashBean splashBean) {

            }

            @Override
            public void onDataFail(int code, @Nullable Object... parameters) {

            }
        },parameters);
    }

    @NonNull
    private SplashModel getModelInstance() {
        return SplashModel.newInstance();
    }

    @Override
    public void modifyData(int type, @Nullable Object... parameters) {
        super.modifyData(type, parameters);
        getModel()
                .saveData(dataAdapter(type, parameters), new IModel.OnDataListener<SplashBean>() {
            @Override
            public void onDataSuccess(SplashBean splashBean) {
            }

            @Override
            public void onDataFail(int code, @Nullable Object... parameters) {
            }
        });
    }

    @Override
    public SplashBean dataAdapter(int type, @Nullable Object... parameters) {
        return super.dataAdapter(type, parameters);
    }
}
