package com.munch.project.one;

import android.os.Bundle;

import com.munch.lib.imageload.ImageLoadHelper;
import com.munch.lib.imageload.glide.GlideStrategy;
import com.munch.lib.libnative.helper.BarHelper;
import com.munch.project.one.base.BaseActivity;


/**
 * Created by Munch on 2019/8/24
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BarHelper.with(this).setTransparent();
        ImageLoadHelper.res(getString(R.string.test_png_3))
                .strategy(new GlideStrategy())
                .into(findViewById(R.id.img));
    }
}
