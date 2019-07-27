package com.munch.module.template.rxjava;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

/**
 * Created by Munch on 2019/7/27 9:06
 */
public interface RxManager {

    void add(Disposable disposable);

    void remove();

    Action onFinally();
}
