package com.munch.module.template.rxjava;

import com.munch.module.template.dto.BDto;
import io.reactivex.ObservableOperator;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Munch on 2019/7/26 16:04
 */
public class RxTransHelper {

    public static <U> ObservableTransformer<U, U> formIo() {
        return upstream -> upstream.subscribeOn(Schedulers.io());
    }

    public static <U> ObservableTransformer<U, U> toMain() {
        return upstream -> upstream.observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 将非成功的请求回调到onError中统一处理，并拦截因此的{@link io.reactivex.exceptions.OnErrorNotImplementedException}
     * 将成功的请求去除外部包装，转换为真实的类型数据
     *
     * @see DefErrorHandle
     * @see ResDtoObserver
     */
    @SuppressWarnings("JavadocReference")
    public static <T> ObservableTransformer<BDto<T>, T> dto() {
        return upstream -> upstream.lift((ObservableOperator<T, BDto<T>>) ResDtoObserver::newInstance);
    }

    public static <U> ObservableTransformer<U, U> manager(RxManager manager) {
        return upstream -> upstream.doOnSubscribe(manager::add).doFinally(manager.onFinally());
    }
}
