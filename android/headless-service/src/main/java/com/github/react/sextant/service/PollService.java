package com.github.react.sextant.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {
    private static final long POLL_INTERVAL_MD = TimeUnit.MINUTES.toMillis(1);

    public PollService(String name) {
        super(name);
    }

    // 启动服务
    public static Intent getInstance(Context context){
        return new Intent(context, PollService.class);
    }

    // 响应intent
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //用SharedPreferences存储状态
    }

    // 检查网路是否可用
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    // 使用AlarmManager延迟运行服务 setTimeout
    public static void setServiceAlarm(Context context, boolean isOn){
        Intent intent = PollService.getInstance(context);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),POLL_INTERVAL_MD, pi);
        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
}
