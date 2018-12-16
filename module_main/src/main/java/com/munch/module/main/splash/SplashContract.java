package com.munch.module.main.splash;

import com.munch.lib.nativelib.mpvpack.IModel;
import com.munch.lib.nativelib.mpvpack.IPresenter;
import com.munch.lib.nativelib.mpvpack.IView;

/**
 * Created by Munch on 2018/12/16.
 */
public class SplashContract {

    interface Model extends IModel<SplashBean> {
    }

    interface View extends IView<SplashBean> {
    }

    interface Present extends IPresenter<SplashBean,View, Model> {
    }

    public static class Type {

        static final int TYPE_D = -1;
    }
}
