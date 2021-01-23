package com.munch.project.testsimple.alive.mix;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.munch.lib.helper.LogLog;
import com.munch.project.testsimple.IGuardConnection;
import com.munch.project.testsimple.R;
import com.munch.project.testsimple.alive.TestDataHelper;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Create by munch1182 on 2020/12/15 17:35.
 */
public class WorkService extends Service {

    private final static int NOTIFICATION_ID = 1215;
    public static final String CHANNEL_ONE_ID = "WORK SERVICE";
    public static final String CHANNEL_ONE_NAME = "work service";

    public static Intent getIntent(Context context) {
        return new Intent(context, WorkService.class);
    }

    public static void start(@NotNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(getIntent(context));
        } else {
            context.startService(getIntent(context));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IGuardConnection.Stub() {
            @Override
            public void notifyAlive() {

            }
        };
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                IGuardConnection.Stub.asInterface(service).notifyAlive();
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogLog.log("loglog", "WorkService onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindRemoteService();
            LogLog.log("loglog", "WorkService onServiceDisconnected");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        String channerlId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            channerlId = CHANNEL_ONE_ID;
        }
        String content = "";
        try {
            content = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault()).format(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        startForeground(NOTIFICATION_ID,
                new NotificationCompat.Builder(this, channerlId)
                        .setContentTitle("后台运行中")
                        .setContentText("从" + content + "开始")
                        .setSmallIcon(R.mipmap.test_simple_ic_launcher)
                        .build());
        bindRemoteService();
        TestDataHelper.INSTANCE.testMix(this);
    }

    private void bindRemoteService() {
        Intent intent = RemoteService.getIntent(this);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }
}
