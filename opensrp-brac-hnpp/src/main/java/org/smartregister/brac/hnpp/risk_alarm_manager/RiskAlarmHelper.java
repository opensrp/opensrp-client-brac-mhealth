package org.smartregister.brac.hnpp.risk_alarm_manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.M)
public class RiskAlarmHelper {
    Context context;
    private AlarmManager alarmManager;

    public RiskAlarmHelper(Context context) {
        this.context = context;
        alarmManager = context.getSystemService(AlarmManager.class);
    }

    public void scheduleAlarm(String msg){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,9);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.AM_PM,Calendar.AM);

        Log.d("LLLLL",calendar.get(Calendar.DAY_OF_MONTH)+" "+calendar.get(Calendar.HOUR_OF_DAY)+" "+calendar.get(Calendar.MINUTE)+" "+calendar.get(Calendar.AM_PM));

        Intent intent = new Intent(context, RiskAlarmReceiver.class);
        intent.putExtra("EXTRA_MESSAGE", msg);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),//9 am
                86400000,//24 h
                PendingIntent.getBroadcast(
                        context,
                        RiskAlarmReceiver.REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
    }

    public void cancelAlarm(){
        alarmManager.cancel(
                PendingIntent.getBroadcast(
                        context,
                        RiskAlarmReceiver.REQUEST_CODE,
                        new Intent(context, RiskAlarmReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
            )
        );
    }

}
