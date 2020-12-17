package com.munch.project.testsimple.alive.mix;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.munch.lib.helper.LogLog;
import com.munch.project.testsimple.IGuardConnection;

/**
 * Create by munch1182 on 2020/12/15 17:35.
 */
public class RemoteService extends Service {

    public static Intent getIntent(Context context) {
        return new Intent("alive.mix.RemoteService").setPackage(context.getPackageName());
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
            LogLog.log("loglog", "RemoteService onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindWorkService();
            LogLog.log("loglog", "RemoteService onServiceDisconnected");
        }
    };

    private void bindWorkService() {
        startService(WorkService.getIntent(this));
        bindService(WorkService.getIntent(this), conn, Context.BIND_AUTO_CREATE);
    }
}
