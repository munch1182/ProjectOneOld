package com.munch.module.main.splash;

import com.munch.lib.nativelib.base.BaseRootActivity;

/**
 * Created by Munch on 2018/12/16.
 */
public class SplashActivity extends BaseRootActivity<SplashBean, SplashContract.Present>
        implements SplashContract.View {

    @Override
    public void start() {
        bindPresenter(new SplashPresenter()).start();
    }

}
