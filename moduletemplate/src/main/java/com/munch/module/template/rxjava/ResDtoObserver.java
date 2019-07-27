package com.munch.module.template.rxjava;

import com.munch.module.template.dto.BDto;
import com.munch.module.template.dto.CodeException;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

/**
 * Created by Munch on 2019/7/26 16:09
 */
public class ResDtoObserver<D> implements Observer<BDto<D>> {

    private final Observer<? super D> mDownObserver;

    public static <D> ResDtoObserver<D> newInstance(Observer<? super D> observer) {
        return new ResDtoObserver<>(observer);
    }

    public ResDtoObserver(Observer<? super D> observer) {
        mDownObserver = observer;
    }

    @Override
    public void onSubscribe(Disposable d) {
        mDownObserver.onSubscribe(d);
    }

    @Override
    public void onNext(BDto<D> t) {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onError(Throwable e) {
        if (e instanceof CodeException) {
            if (((CodeException) e).canHandle()) {
                if (DefErrorHandle.handle(((CodeException) e).getBDto())) {
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
        mDownObserver.onComplete();
    }
}
