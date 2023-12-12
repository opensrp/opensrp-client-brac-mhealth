package org.smartregister.brac.hnpp.utils;

import android.util.Log;

import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.model.RiskyPatientFilterType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RiskyPatientFilterUtils {

    /**
     * checking filter needed or not
     * @param riskyPatientFilterType
     * @return
     */
    public static boolean isFilterNeeded(RiskyPatientFilterType riskyPatientFilterType) {
        return riskyPatientFilterType.getVisitScheduleToday() == 1 ||
                riskyPatientFilterType.getVisitScheduleNextThree() == 1 ||
                riskyPatientFilterType.getVisitScheduleNextSeven() == 1 ||
                riskyPatientFilterType.getVisitScheduleLastDay() == 1 ||
                riskyPatientFilterType.getVisitScheduleLastThree() == 1 ||
                riskyPatientFilterType.getVisitScheduleLastSeven() == 1 ||
                riskyPatientFilterType.getVisitScheduleAllDue() == 1;
    }

    /**
     * checking all filer options
     * @param model
     * @param riskyPatientFilterType
     * @return
     */
    public static boolean checkFilter(long followUpDate, RiskyPatientFilterType riskyPatientFilterType) {
        if(riskyPatientFilterType.getVisitScheduleToday() == 1 && RiskyPatientFilterUtils.isToday(followUpDate)){
            return true;
        }else if(riskyPatientFilterType.getVisitScheduleNextThree() == 1 && RiskyPatientFilterUtils.isNextDays(followUpDate,3)){
            return true;
        }else if(riskyPatientFilterType.getVisitScheduleNextSeven() == 1 && RiskyPatientFilterUtils.isNextDays(followUpDate,7)){
            return true;
        } else if(riskyPatientFilterType.getVisitScheduleLastDay() == 1 && RiskyPatientFilterUtils.isLastDays(followUpDate,-1)){
            return true;
        }else if(riskyPatientFilterType.getVisitScheduleLastThree() == 1 && RiskyPatientFilterUtils.isLastDays(followUpDate,-3)){
            return true;
        }else if(riskyPatientFilterType.getVisitScheduleLastSeven() == 1 && RiskyPatientFilterUtils.isLastDays(followUpDate,-7)){
            return true;
        }else return riskyPatientFilterType.getVisitScheduleAllDue() == 1;
    }

    /**
     * checking today's followup
     * @param date long date
     * @return bool
     */
    public static boolean isToday(long date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String current = sdf.format(new Date());
        String followUpDate = sdf.format(new Date(date));

        return current.equals(followUpDate);
    }

    /**
     * checking next days followup
     * @param date long date
     * @return bool
     */
    public static boolean isNextDays(long date,int days){

        Date currentDate = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        Date daysFromNow = calendar.getTime();

        Calendar tomorrowCal = Calendar.getInstance();
        tomorrowCal.setTime(currentDate);
        tomorrowCal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = tomorrowCal.getTime();

        return date >= tomorrow.getTime() && date <= daysFromNow.getTime();
    }

    /**
     * checking last days followup
     * @param date long date
     * @return bool
     */

    public static boolean isLastDays(long date,int days){

        Date currentDate = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        Date daysFromNow = calendar.getTime();

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR,24);
        todayCal.set(Calendar.MINUTE,0);
        todayCal.set(Calendar.SECOND,0);
        todayCal.set(Calendar.AM_PM,Calendar.AM);

        todayCal.add(Calendar.DAY_OF_MONTH, -1);
        Date today = todayCal.getTime();

        return date < today.getTime() && date >= daysFromNow.getTime();
    }
}
