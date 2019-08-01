package com.munch.module.template.rxjava;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Munch on 2019/7/29 17:59
 */
public class DisposableManager implements IDisposableManager{

    private final CompositeDisposable mCompositeDisposable;

    private DisposableManager() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void add(Disposable... disposable) {
        mCompositeDisposable.addAll(disposable);
    }

    @Override
    public void clear() {
        mCompositeDisposable.clear();
    }

    public static DisposableManager newInstance() {
        return new DisposableManager();
    }

    public static DisposableManager getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final DisposableManager INSTANCE = new DisposableManager();
    }
}
