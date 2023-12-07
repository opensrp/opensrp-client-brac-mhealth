package org.smartregister.brac.hnpp.risk_alarm_manager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.RiskyPatientActivity;
import org.smartregister.brac.hnpp.enums.FollowUpType;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.repository.AncFollowUpRepository;
import org.smartregister.chw.core.repository.AncRegisterRepository;

import java.util.ArrayList;

public class RiskAlarmReceiver extends BroadcastReceiver {
    static int REQUEST_CODE = 2023;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        String channelId = "alarm_id";
        StringBuilder detailsStr = new StringBuilder("Anc Followup for bellow Patients \n\n");

        AncFollowUpRepository ancFollowUpRepository = HnppApplication.getAncFollowUpRepository();

        ArrayList<AncFollowUpModel> specialList =  ancFollowUpRepository.getAncFollowUpData(FollowUpType.special,true);
        ArrayList<AncFollowUpModel> telephonicList =  ancFollowUpRepository.getAncFollowUpData(FollowUpType.telephonic,true);

        if(specialList.size() > 0){
            for(AncFollowUpModel data : specialList){
                detailsStr.append("Special-> ").append(data.memberName).append(" -> ").append(data.memberPhoneNum).append("\n");
            }
        }

        if(telephonicList.size() > 0){
            for(AncFollowUpModel data : telephonicList){
                detailsStr.append("Telephonic-> ").append(data.memberName).append(" -> ").append(data.memberPhoneNum).append("\n");
            }
        }

        if(telephonicList.size()>0 || specialList.size()>0){
            if(context != null){
                Intent notificationIntent = new Intent(context, RiskyPatientActivity.class ) ;
                PendingIntent resultIntent = PendingIntent. getActivity (context, 0 , notificationIntent ,   PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE ) ;

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelId)
                        .setSmallIcon(R.drawable.ic_app_icon)
                        .setContentTitle("Anc Followup Remainder")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(detailsStr))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(resultIntent);
                notificationManager.notify(1,builder.build());
            }
        }
    }
}
