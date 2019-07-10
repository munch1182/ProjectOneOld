package com.munch.module.test.compat;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.munch.lib.log.LogLog;
import com.munch.lib.result.compat.PermissionListener;
import com.munch.lib.result.compat.PermissionLoopListener;
import com.munch.lib.result.compat.ResultHelperCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResultHelperCompat.requestPermission(MainActivity.this, 11, Manifest.permission.CALL_PHONE,
                        Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS)
                        .isLoop(true)
                        .loop(new PermissionLoopListener() {
                            @Override
                            public boolean loop(String permissions, int grantResults) {
                                LogLog.log(permissions, grantResults);
                                switch (permissions) {
                                    case Manifest.permission.CALL_PHONE:
                                        break;
                                    case Manifest.permission.CAMERA:
                                        return true;
                                    case Manifest.permission.READ_CONTACTS:
                                        break;
                                }
                                return false;
                            }
                        })
                        .result(new PermissionListener() {
                            @Override
                            public void result(String[] permissions, int[] grantResults) {
                                LogLog.log(permissions, grantResults);
                            }
                        });
            }
        });
    }
}
