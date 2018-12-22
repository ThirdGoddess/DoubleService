package com.keep.process.process_service_java;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

public class LocalService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*第一个参数Intent
        第二个参数ServiceConnection*/

        /**
         *  第三个参数介绍：
        * Flag for {@link #bindService}: automatically create the service as long
        * as the binding exists.  Note that while this will create the service,
        * its {@link android.app.Service#onStartCommand}
        * method will still only be called due to an
        * explicit call to {@link #startService}.  Even without that, though,
        * this still provides you with access to the service object while the
        * service is created.
        *
        * <p>Note that prior to {@link android.os.Build.VERSION_CODES#ICE_CREAM_SANDWICH},
        * not supplying this flag would also impact how important the system
        * consider's the target service's process to be.  When set, the only way
        * for it to be raised was by binding from a service in which case it will
        * only be important when that activity is in the foreground.  Now to
        * achieve this behavior you must explicitly supply the new flag
        * {@link #BIND_ADJUST_WITH_ACTIVITY}.  For compatibility, old applications
        * that don't specify {@link #BIND_AUTO_CREATE} will automatically have
        * the flags {@link #BIND_WAIVE_PRIORITY} and
        * {@link #BIND_ADJUST_WITH_ACTIVITY} set for them in order to achieve
        * the same result.
        */
        bindService(new Intent(this,RemoteService.class),connection,Context.BIND_AUTO_CREATE);
        return super.onStartCommand(intent, flags, startId);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //绑定成功
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //当RemoteService所处进程被干掉就重新启动
            startService(new Intent(LocalService.this,RemoteService.class));
            bindService(new Intent(LocalService.this,RemoteService.class),connection,Context.BIND_IMPORTANT);
        }
    };

    private class LocalBinder extends Binder {

    }
}
