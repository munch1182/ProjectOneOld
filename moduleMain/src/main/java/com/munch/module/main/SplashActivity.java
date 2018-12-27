package com.munch.module.main;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.munch.lib.libnative.helper.PackHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_main_activity_splash);
        try {
            ((TextView) findViewById(R.id.tv)).setText(
                    String.format("%s+%s",
                            String.valueOf(PackHelper.getVersionCode(this)),
                            PackHelper.getVersionName(this)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ((ImageView) findViewById(R.id.iv)).setImageDrawable(PackHelper.getAppIcon(this));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}
