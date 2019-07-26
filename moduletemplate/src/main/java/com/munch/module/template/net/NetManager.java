package com.munch.module.template.net;

import com.munch.lib.logcompat.LogLog;
import com.munhc.lib.libnative.helper.AppHelper;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Munch on 2019/7/26 13:43
 */
public class NetManager {

    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;

    private NetManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .writeTimeout(NetConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(NetConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(NetConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Request.Builder requestBuilder = request.newBuilder();
                        /*添加公共参数*/
                        /*requestBuilder.addHeader(NetConfig.Parameter.LANGUAGE, LanguageHelper.getLanguageStr4Request());
                        String token = App.getInstance().getToken();
                        if (StringHelper.isEmpty(token)) {
                            token = App.getInstance().getTempToken();
                        }
                        if (!StringHelper.isEmpty(token)) {
                            requestBuilder = requestBuilder.url(request.url().newBuilder()
                                    .addQueryParameter(NetConfig.Parameter.TOKEN, token)
                                    .build());
                        }*/
                        return chain.proceed(requestBuilder.build());
                    }
                });

        if (AppHelper.isDebug()) {
            builder.addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(@SuppressWarnings("NullableProblems") String message) {
                    LogLog.log(new LogLog.Builder().logBorder(false).logStack(false), message);
                }
            }));
        }
        mOkHttpClient = builder.build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(NetConfig.getBaseUrl())
                .client(mOkHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Service getService() {
        return getRetrofit().create(Service.class);
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public static NetManager getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final NetManager INSTANCE = new NetManager();
    }
}
