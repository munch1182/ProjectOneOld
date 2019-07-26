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

    public static <T> ObservableTransformer<BDto<T>, T> dto() {
        return upstream -> upstream.lift((ObservableOperator<T, BDto<T>>) observer -> {
            return new ResDtoObserver<>(observer);
        });
    }
}
