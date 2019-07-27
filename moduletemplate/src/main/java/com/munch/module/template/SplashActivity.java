package com.munch.module.template;

import android.os.Bundle;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.munch.lib.logcompat.LogLog;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ViewGroup view = findViewById(R.id.content);
        LogLog.log(view.getChildCount());
        LogLog.log(view.getChildAt(0), view.getChildAt(1));
    }
}
