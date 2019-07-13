package com.munch.module.test;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.munch.lib.image.ImageHelper;
import com.munch.lib.image.glide.GlideStrategy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageHelper.setupStrategy(new GlideStrategy());

        String res = "http://www.ratoo.net/uploads/allimg/170823/15-1FR31H604.jpg";
        String gif = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1562996738387&di=83afac842b45b901decc0bcc9a13cb16&imgtype=0&src=http%3A%2F%2Fpic.962.net%2Fup%2F2018-5%2F15262691097592176.gif";
        ImageHelper.res(gif)
                .asGif(true)
                .transformation(new CenterCrop(), new RoundedCorners(90))
                .into(findViewById(R.id.iv));
    }
}
