package com.munch.module.template.net;

import com.munch.lib.logcompat.LogLog;
import com.munhc.lib.libnative.helper.AppHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by Munch on 2019/7/26 13:43
 */
public class NetManager {

    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;
    private final Service mService;

    /**
     * 请求的取消：{@link retrofit2.adapter.rxjava2.CallEnqueueObservable.CallCallback#dispose()}
     */
    @SuppressWarnings("JavadocReference")
    private NetManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .writeTimeout(NetConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(NetConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(NetConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
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
                });

        if (AppHelper.isDebug()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> LogLog.log(new LogLog.Builder().logBorder(false).logStack(false), message));
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        mOkHttpClient = builder.build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(NetConfig.getBaseUrl())
                .client(mOkHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = getRetrofit().create(Service.class);
    }

    public Service getService() {
        return mService;
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
