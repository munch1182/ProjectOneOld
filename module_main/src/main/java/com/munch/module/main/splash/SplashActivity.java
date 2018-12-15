package com.munch.module.main.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.munch.lib.nativelib.base.BaseActivity;
import com.munch.lib.nativelib.base.Type;
import com.munch.module.main.R;

/**
 * Created by Munch on 2018/12/16.
 */
public class SplashActivity extends BaseActivity<SplashBean, SplashContract.Present>
        implements SplashContract.View, View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        bindPresenter(new SplashPresenter())
//                .manageView(this, this)
                .start(SplashContract.Type.TYPE_D);

        new TextView(this).setOnClickListener(this);
    }

    @Override
    public void syncView(int type, @Nullable Object... parameters) {
        super.syncView(type, parameters);
        switch (type) {
            case Type.TYPE_LOAD_FAIL:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        getPresenter().modifyData(SplashContract.Type.TYPE_D,v.getId(),v.getTag());
    }
}
