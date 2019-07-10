package com.munch.module.test;

import android.Manifest;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.munch.lib.log.LogLog;
import com.munch.lib.result.ResultHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv).setOnClickListener(v -> {
            /*ResultHelperCompat.start4Result(this, new Intent(this, ResultActivity.class), 11)
                    .result((resultCode, intent) -> {
                        LogLog.log(resultCode);
                    });*/
            ResultHelper.requestPermission(this, 33, Manifest.permission.CALL_PHONE)
                    .result((permissions, grantResults) -> {
                        LogLog.log(permissions, grantResults);
                    });
        });
    }
}
