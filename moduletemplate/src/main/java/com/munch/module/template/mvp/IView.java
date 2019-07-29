package com.munch.module.template.mvp;

import android.content.Context;
import androidx.annotation.Nullable;
import com.munch.module.template.rxjava.DisposableManager;

/**
 * Created by Munch on 2019/7/29 17:13
 */
public interface IView<P extends IPresenter> {

    P bindPresenter(P p);

    @Nullable
    P getPresenter();

    Context getContext();

    DisposableManager getManager();
}
