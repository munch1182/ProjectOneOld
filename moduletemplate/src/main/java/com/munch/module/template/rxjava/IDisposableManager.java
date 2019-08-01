package com.munch.module.template.rxjava;

import io.reactivex.disposables.Disposable;

/**
 * Created by Munch on 2019/8/1 15:41
 */
public interface IDisposableManager {

    void add(Disposable... disposable);

    void clear();
}
