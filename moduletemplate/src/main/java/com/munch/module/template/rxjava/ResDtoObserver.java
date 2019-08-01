package com.munch.module.template.rxjava;

import com.munch.module.template.dto.BDto;
import com.munch.module.template.dto.CodeException;
import com.munch.module.template.mvp.BasePresenter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

/**
 * Created by Munch on 2019/7/26 16:09
 */
public class ResDtoObserver<D> implements Observer<BDto<D>> {

    private final Observer<? super D> mDownObserver;
    private BasePresenter mPresenter;
    private DisposableManager mManager;

    public static <D> ResDtoObserver<D> newInstance(Observer<? super D> observer, DisposableManager manager) {
        return newInstance(observer, null, manager);
    }

    public static <D> ResDtoObserver<D> newInstance(Observer<? super D> observer, BasePresenter presenter, DisposableManager manager) {
        return new ResDtoObserver<>(observer, presenter, manager);
    }

    public ResDtoObserver(Observer<? super D> observer, BasePresenter presenter, DisposableManager manager) {
        mDownObserver = observer;
        mPresenter = presenter;
        mManager = manager;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (null != mManager) {
            mManager.add(d);
        }
        mDownObserver.onSubscribe(d);
    }

    @Override
    public void onNext(BDto<D> t) {
        if (noView()) {
            return;
        }
        if (t.isSuccess()) {
            if (t.getData() == null) {
                mDownObserver.onError(new CodeException(false, "-1", "服务器返回为null"));
                return;
            }
            mDownObserver.onNext(t.getData());
        } else {
            onError(new CodeException(true, t));
        }
    }

    /**
     * 判读P与V是否已经分离，如果已经分离，则无需传递数据
     * 理论上来说绑定了生命周期在生命周期结束时取消Disposable会取消rxJava而不触发P与V的空指针
     */
    private boolean noView() {
        return mPresenter != null && mPresenter.isViewAttach();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onError(Throwable e) {
        if (noView()) {
            return;
        }
        if (e instanceof CodeException) {
            CodeException codeException = (CodeException) e;
            if (codeException.canHandle()) {
                if (DefErrorHandle.handle(codeException.getBDto())) {
                    mDownObserver.onError(e);
                }
            }
            //拦截OnErrorNotImplementedException
        } else if (e instanceof OnErrorNotImplementedException) {
            //do nothing
        } else {
            if (DefErrorHandle.handle(e)) {
                mDownObserver.onError(e);
            }
        }
    }

    @Override
    public void onComplete() {
        if (noView()) {
            return;
        }
        mDownObserver.onComplete();
    }
}
