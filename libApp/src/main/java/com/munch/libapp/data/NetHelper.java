package com.munch.libapp.data;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Munch on 2018/12/17.
 */
public class NetHelper<T> {

    public static <T> void convert(Observable<BaseResBean<T>> observable) {
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Disposable::dispose)
                .subscribe(res -> {
                    switch (res.code) {
                        case 0:

                            break;
                        default:
                            break;
                    }
                }, throwable -> {

                });
    }
}
