package com.munch.common.base.activity.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.common.base.activity.Loglog;
import com.munch.lib.nativelib.helper.AppHelper;
import com.munch.lib.nativelib.helper.ObjectHelper;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Munch on 2018/12/17.
 */
public abstract class NetRootHelper {

    private Retrofit retrofit;

    public NetRootHelper() {
        retrofit = getRetrofit();
        if (ObjectHelper.isNonEmpty(retrofit)) {
            return;
        }
        retrofit = new Retrofit.Builder()
                .client(getClient())
                .baseUrl(getBaseUrl())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(getConvertFactory())
                .build();
    }

    public <T> T create(Class<T> clazz) {
        return retrofit.create(clazz);
    }

//    public <T> T create(Class<T> clazz, String baseUrl) {
//        return this.retrofit.newBuilder()
//                .baseUrl(baseUrl)
//                .build()
//                .create(clazz);
//    }

    @Nullable
    public Retrofit getRetrofit() {
        return null;
    }

    @NonNull
    private OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (AppHelper.isDebug()) {
            builder.addInterceptor(new HttpLoggingInterceptor(Loglog::log));
        }
        return builder.build();
    }

    public Converter.Factory getConvertFactory() {
        return GsonConverterFactory.create();
    }

    @NonNull
    abstract String getBaseUrl();
}
