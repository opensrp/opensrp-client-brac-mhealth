package org.smartregister.brac.hnpp.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.FamilyProfileActivity;
import org.smartregister.brac.hnpp.activity.FamilyRegisterActivity;
import org.smartregister.brac.hnpp.listener.OnGpsDataGenerateListener;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.activity.TermAndConditionWebView;
import org.smartregister.brac.hnpp.fragment.COVIDJsonFormFragment;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.task.GenerateGPSTask;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.family.util.DBConstants;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.BaseProfileActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class HnppConstants extends CoreConstants {
    public static final String ACTION_STOCK_COME = "ACTION_STOCK_COME";
    public static final String ACTION_STOCK_END = "ACTION_STOCK_END";
    public static final String ACTION_EDD = "ACTION_EDD";
    public static final String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
    public static final String EXTRA_STOCK_COME = "EXTRA_STOCK_COME";
    public static final String EXTRA_STOCK_END = "EXTRA_STOCK_END";
    public static final String EXTRA_EDD = "EXTRA_EDD";
    public static final long STOCK_END_DEFAULT_TIME = 6*60*60*1000;//6 hr
    public static final long EDD_DEFAULT_TIME = 6*60*60*1000;//6 hr
    public static final String TEST_GU_ID = "test";
    public static final float VERIFY_THRESHOLD = 20;
    public static final String MODULE_ID_TRAINING = "TRAINING";
    public static final int MEMBER_ID_SUFFIX = 11;
    public static final int HOUSE_HOLD_ID_SUFFIX = 9;
    public static final int GUEST_MEMBER_ID_SUFFIX = 5;
    public static final String IS_RELEASE = "is_release_build";
    public static final String IS_DEVICE_VERIFY = "is_device_verify";
    public static final String DEVICE_IMEI = "device_imei";
    public static boolean isSortByLastVisit = false;
    public static boolean isViewRefresh = false;
    public static final String KEY_IS_SAME_MONTH = "is_same_month";
    public static final String KEY_NEED_TO_OPEN = "need_to_open_drawer";

    public static SimpleDateFormat DDMMYY = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
    public static SimpleDateFormat DDMMYYHM = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
    public enum VisitType {DUE, OVERDUE, LESS_TWENTY_FOUR, VISIT_THIS_MONTH, NOT_VISIT_THIS_MONTH, EXPIRY, VISIT_DONE}
    public enum HomeVisitType {GREEN, YELLOW, RED, BROWN}
    public enum SEARCH_TYPE {HH, ADO, WOMEN, CHILD,NCD,ADULT}
    public enum MIGRATION_TYPE {HH, Member}

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager conMgr = null;
        boolean connected = false;

        try {
            conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            connected = (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable() && conMgr
                    .getActiveNetworkInfo().isConnected());
            return connected;
        } catch (Exception e) {
            if (context == null) {
                context = HnppApplication.getHNPPInstance();
            }
            if (conMgr == null) {
                conMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            if (conMgr != null) {
                connected = (conMgr.getActiveNetworkInfo() != null
                        && conMgr.getActiveNetworkInfo().isAvailable() && conMgr
                        .getActiveNetworkInfo().isConnected());
            }
            return connected;
        }
    }

    public static void getGPSLocation(FamilyRegisterActivity activity, OnPostDataWithGps onPostDataWithGps){


        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    11111);
            return ;
        }
        GenerateGPSTask task = new GenerateGPSTask(new OnGpsDataGenerateListener() {
            @Override
            public void showProgressBar(int message) {
                activity.showProgressDialog(message);
            }

            @Override
            public void hideProgress() {
                activity.hideProgressDialog();

            }

            @Override
            public void onGpsDataNotFound() {
                HnppConstants.showOneButtonDialog(activity,"",activity.getString(R.string.gps_not_found));
            }

            @Override
            public void onGpsData(double latitude, double longitude) {
                onPostDataWithGps.onPost(latitude,longitude);

            }
        },activity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(task!=null) task.updateUi();
            }
        },2000);


    }
    public static void getGPSLocation(BaseProfileActivity activity, OnPostDataWithGps onPostDataWithGps){


        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    11111);
            return ;
        }
        GenerateGPSTask task = new GenerateGPSTask(new OnGpsDataGenerateListener() {
            @Override
            public void showProgressBar(int message) {
                try{
                    activity.showProgressDialog(message);
                }catch (Exception e){

                }
            }

            @Override
            public void hideProgress() {
                try{
                    activity.hideProgressDialog();
                }catch (Exception e){

                }

            }

            @Override
            public void onGpsDataNotFound() {
                try{
                    HnppConstants.showOneButtonDialog(activity,"",activity.getString(R.string.gps_not_found));
                }catch (Exception e){

                }
            }

            @Override
            public void onGpsData(double latitude, double longitude) {
                onPostDataWithGps.onPost(latitude,longitude);

            }
        },activity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(task!=null) task.updateUi();
            }
        },2000);


    }

    public static String addZeroForMonth(String month){
        if(TextUtils.isEmpty(month)) return "";
        if(month.length()==1) return "0"+month;
        return month;
    }
    public static String addZeroForDay(String day){
        if(TextUtils.isEmpty(day)) return "";
        if(day.length()==1) return "0"+day;
        return day;
    }
    public class ANC_REGISTER_COLUMNS {
        public static final String LAST_MENSTRUAL_PERIOD = "last_menstrual_period";
        public static final String EDD = "edd";
        public static final String NO_PREV_PREG = "no_prev_preg";
        public static final String NO_SURV_CHILDREN = "no_surv_children";
        public static final String HEIGHT = "height";
    }

    public static class TABLE_NAME {
        public static final String FAMILY = "ec_family";
        public static final String FAMILY_MEMBER = "ec_family_member";
        public static final String CHILD = "ec_child";
        public static final String ANC_PREGNANCY_OUTCOME = "ec_pregnancy_outcome";
        public static final String ANC_MEMBER = "ec_anc_register";
    }
    public class OTHER_SERVICE_TYPE{
        public static final int TYPE_WOMEN_PACKAGE = 1;
        public static final int TYPE_GIRL_PACKAGE = 2;
        public static final int TYPE_NCD = 3;
        public static final int TYPE_IYCF = 4;
        public static final int TYPE_EYE = 5;
        public static final int TYPE_BLOOD = 6;
        public static final int TYPE_REFERRAL = 7;
        public static final int TYPE_REFERRAL_FOLLOW_UP = 8;
    }

    public static String getPaymentIdFromUrl(String url){
        String paymentId = "";
        if(TextUtils.isEmpty(url)) return "";
        paymentId= url.substring(url.indexOf("paymentID")+10,url.indexOf("&"));
        return paymentId;
    }

    public static boolean isNeedToShowEDDPopup(){
        String lastEddTimeStr =  org.smartregister.Context.getInstance().allSharedPreferences().getPreference("LAST_EDD_TIME");
        if(TextUtils.isEmpty(lastEddTimeStr)){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("LAST_EDD_TIME",System.currentTimeMillis()+"");
            return true;
        }
        long diff = System.currentTimeMillis() - Long.parseLong(lastEddTimeStr);
        if(diff > EDD_DEFAULT_TIME){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("LAST_EDD_TIME",System.currentTimeMillis()+"");

            return true;
        }
        return false;
    }
    public static boolean isNeedToShowStockEndPopup(){
        String lastEddTimeStr =  org.smartregister.Context.getInstance().allSharedPreferences().getPreference("STOCK_END_TIME");
        if(TextUtils.isEmpty(lastEddTimeStr)){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("STOCK_END_TIME",System.currentTimeMillis()+"");
            return true;
        }
        long diff = System.currentTimeMillis() - Long.parseLong(lastEddTimeStr);
        if(diff > STOCK_END_DEFAULT_TIME){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("STOCK_END_TIME",System.currentTimeMillis()+"");

            return true;
        }
        return false;
    }
    public static void showSaveFormConfirmationDialog(Context context,String title, Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.save_confirm_dialog);
        TextView textViewTitle = dialog.findViewById(R.id.condirm_text);
        textViewTitle.setText(title);
        dialog.findViewById(R.id.no_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.yes_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                runnable.run();
            }
        });
        dialog.show();
    }
    public static void showDialogWithAction(Context context,String title, String text,Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_two_button);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                runnable.run();
            }
        });
        dialog.show();
    }
    public static void showTermConditionDialog(Context context,String title, String text,Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_term_condition);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        CheckBox checkBox = dialog.findViewById(R.id.term_check);
        LinearLayout payBtn = dialog.findViewById(R.id.bkash_pay);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    payBtn.setAlpha(1.0f);
                    payBtn.setEnabled(true);
                }else{
                    payBtn.setAlpha(0.3f);
                    payBtn.setEnabled(false);
                }
            }
        });
        dialog.findViewById(R.id.term_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, TermAndConditionWebView.class));
            }
        });
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                runnable.run();
            }
        });
        dialog.show();
    }
    public static void showButtonWithImageDialog(Context context, int type, String message, Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bkash_success);
        ImageView imageView = dialog.findViewById(R.id.image);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        TextView statusTxt = dialog.findViewById(R.id.status_tv);
        TextView messageTxt = dialog.findViewById(R.id.text_tv);
        if(type ==1){
            titleTxt.setText("আপনার পেমেন্ট টি সফল হয়েছে");
            imageView.setImageResource(R.drawable.success);
            statusTxt.setText("Payment successfully");
            statusTxt.setTextColor(context.getResources().getColor(R.color.alert_complete_green));
            messageTxt.setText(message);
            messageTxt.setTextColor(context.getResources().getColor(R.color.alert_complete_green));
        }else if(type == 2){
            titleTxt.setText("আপনার পেমেন্ট টি ফেইল্ড করেছে");
            imageView.setImageResource(R.drawable.failure);
            statusTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
            statusTxt.setText("Payment failed");
            messageTxt.setText(message);
            messageTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
        }else if(type == 3){
            titleTxt.setText("আপনার পেমেন্ট টি বাতিল হয়েছে");
            imageView.setImageResource(R.drawable.cancel);
            statusTxt.setText("Payment cancel");
            statusTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
            messageTxt.setText(message);
            messageTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
        }


        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                runnable.run();
            }
        });
        dialog.show();
    }
    public static void showOneButtonDialog(Context context,String title, String text){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public static void showDialog(Context context,String title, String text){
        Dialog dialog = new Dialog(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.new_stock_view);
        TextView textViewTitle = dialog.findViewById(R.id.stock_new_text);
        TextView titleTxt = dialog.findViewById(R.id.textview_detail_two);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public static void insertAtNotificationTable(String title, String details){
        long currentTime = System.currentTimeMillis();
        String notificationType = "In App";
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        String sendDate = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);

        Notification notification = new Notification();
        notification.setId((int) currentTime);
        notification.setTimestamp(currentTime);
        notification.setTitle(title);
        notification.setDetails(details);
        notification.setNotificationType(notificationType);
        notification.setHour(hour);
        notification.setMinute(minute);
        notification.setSendDate(sendDate);
        HnppApplication.getNotificationRepository().addOrUpdate(notification);
        Log.v("NOTIFICATION_JOB","insertAtNotificationTable:"+title);

    }
    public static boolean isEddImportant(String lmp){
        DateTime lmpDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp);

        LocalDate lastMenstrualPeriod = new LocalDate(lmpDate);
        LocalDate expectedDeliveryDate = lastMenstrualPeriod.plusDays(280);
        int dayDiff = Days.daysBetween(lastMenstrualPeriod, expectedDeliveryDate).getDays();
        return dayDiff <=30;
    }
    public static boolean isWrongDate(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        if(year<2018) return true;
        return false;
    }
    public static void appendLog(String text) {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {

        }
    }

    public static String getHomeVisitStatus(long lastHomeVisit , String dateCreatedStr){

        LocalDate lastVisitDate = new LocalDate(lastHomeVisit);

        LocalDate dateCreated = new LocalDate(TextUtils.isEmpty(dateCreatedStr) ? System.currentTimeMillis(): new DateTime(dateCreatedStr).toLocalDate());

        LocalDate todayDate = new LocalDate();
        int monthDiff = getMonthsDifference((lastHomeVisit != 0 ? lastVisitDate : dateCreated), todayDate);
        if(monthDiff > 7) return HomeVisitType.BROWN.name();
        if(monthDiff > 5) return HomeVisitType.RED.name();
        if(monthDiff > 3) return HomeVisitType.YELLOW.name();
        return HomeVisitType.GREEN.name();

    }
    private static int getMonthsDifference(LocalDate date1, LocalDate date2) {
        return Months.monthsBetween(
                date1.withDayOfMonth(1),
                date2.withDayOfMonth(1)).getMonths();
    }

    public static boolean isExistSpecialCharacter(String filters) {
        if (!TextUtils.isEmpty(filters) && filters.contains("/")) {
            return true;
        }
        return false;
    }

    public static void updateAppBackground(View view) {
        if (!isReleaseBuild()) {
            view.setBackgroundColor(Color.parseColor("#B53737"));
        }
    }

    public static void updateAppBackgroundOnResume(View view) {
        if (!isReleaseBuild()) {
            view.setBackgroundColor(Color.parseColor("#B53737"));
        } else {
            view.setBackgroundColor(Color.parseColor("#F6F6F6"));
        }
    }

    public static ArrayList<String> getClasterSpinnerArray() {

        return new ArrayList<>(getClasterNames().keySet());
    }

    public static String getClusterNameFromValue(String value) {
        HashMap<String, String> keys = getClasterNames();
        for (String key : keys.keySet()) {
            if (keys.get(key).equalsIgnoreCase(value)) {
                return key;
            }
        }
        return "";
    }

    public static HashMap<String, String> getClasterNames() {
        LinkedHashMap<String, String> clusterArray = new LinkedHashMap<>();
        clusterArray.put("ক্লাস্টার ১", "1st_Cluster");
        clusterArray.put("ক্লাস্টার ২", "2nd_Cluster");
        clusterArray.put("ক্লাস্টার ৩", "3rd_Cluster");
        clusterArray.put("ক্লাস্টার ৪", "4th_Cluster");
        return clusterArray;
    }

    public static final class DrawerMenu {
        public static final String ELCO_CLIENT = "Elco Clients";
        public static final String ALL_MEMBER = "All member";
    }

    public static final class FORM_KEY {
        public static final String SS_INDEX = "ss_index";
        public static final String VILLAGE_INDEX = "village_index";
    }
    public static String getGender(String value){
        if(value.equalsIgnoreCase("F")){
            return "মহিলা";
        }
        if(value.equalsIgnoreCase("M")){
            return "পুরুষ";
        }
        return value;
    }

    public static String getTotalCountBn(int count) {
        char[] bn_numbers = "০১২৩৪৫৬৭৮৯".toCharArray();
        String c = String.valueOf(count);
        String number_to_return = "";
        for (char ch : c.toCharArray()) {

            number_to_return += bn_numbers[Integer.valueOf(ch) % Integer.valueOf('0')];
        }
        return number_to_return;
    }
    public static boolean isPALogin(){
        String role = org.smartregister.Context.getInstance().allSharedPreferences().fetchRegisteredRole();
        if(TextUtils.isEmpty(role)) return false;
        if(role.equalsIgnoreCase("PA")) return true;
        return false;

    }

    public static boolean isReleaseBuild() {
        if(BuildConfig.IS_TRAINING){
            return false;
        }
        if(BuildConfig.DEBUG){
            return false;
        }
        return true;
//        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
//        String isReleaseBuild = preferences.getPreference(IS_RELEASE);
//        if (TextUtils.isEmpty(isReleaseBuild) || isReleaseBuild.equalsIgnoreCase("L")) {
//            return true;
//        }
//        return false;
    }

    public static String getDeviceId(TelephonyManager mTelephonyManager, Context context,boolean fromSettings) {
        String deviceId = "";
        try{
            if (mTelephonyManager != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    deviceId = mTelephonyManager.getDeviceId(1);
                    if(fromSettings){
                        deviceId = deviceId+"\n"+mTelephonyManager.getDeviceId(2);
                    }
                }else {
                    if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) { //For tablet
                        deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                    } else { //for normal phones
                        deviceId = mTelephonyManager.getDeviceId();
                    }
                }

            }
        }catch (SecurityException se){
            se.printStackTrace();
        }

        return deviceId;
    }
    public static boolean isDeviceVerified(){
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        String isDeviceVerif = preferences.getPreference(IS_DEVICE_VERIFY);
        if(!TextUtils.isEmpty(isDeviceVerif) && isDeviceVerif.equalsIgnoreCase("V")){
            return true;
        }
        return false;
    }
    public static String getDeviceImeiFromSharedPref(){
        String imei = Utils.getAllSharedPreferences().getPreference(DEVICE_IMEI);
        return TextUtils.isEmpty(imei)?"testimei":imei;
    }
    public static void updateLiveTest(String appMode){
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        preferences.savePreference(IS_RELEASE,appMode);
    }
    public static void updateDeviceVerified(boolean isVerify, String deviceImei){
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        preferences.savePreference(IS_DEVICE_VERIFY,isVerify?"V":"");
        preferences.savePreference(DEVICE_IMEI,deviceImei);
    }
    public static String getSimPrintsProjectId(){

        return isReleaseBuild()?BuildConfig.SIMPRINT_PROJECT_ID_RELEASE:BuildConfig.SIMPRINT_PROJECT_ID_TRAINING;
    }
    public static final class KEY {
        public static final String TOTAL_MEMBER = "member_count";
        public static final String VILLAGE_NAME = "village_name";
        public static final String CLASTER = "claster";
        public static final String MODULE_ID = "module_id";
        public static final String RELATION_WITH_HOUSEHOLD = "relation_with_household_head";
        public static final String GU_ID = "gu_id";
        public static final String MARITAL_STATUS = "marital_status";
        public static final String HOUSE_HOLD_ID = "house_hold_id";
        public static final String HOUSE_HOLD_NAME = "house_hold_name";
        public static final String SS_NAME = "ss_name";
        public static final String IS_RISK = "is_risk";
        public static final String SERIAL_NO = "serial_no";
        public static final String CHILD_MOTHER_NAME_REGISTERED = "mother_name";
        public static final String CHILD_MOTHER_NAME = "Mother_Guardian_First_Name_english";
        public static final String GENDER = "gender";
        public static final String NATIONAL_ID = "national_id";
        public static final String BIRTH_ID = "birth_id";
        public static final String IS_BITHDAY_KNOWN = "is_birthday_known";
        public static final String BLOOD_GROUP = "blood_group";
        public static final String LAST_HOME_VISIT = "last_home_visit";
        public static final String DATE_CREATED = "date_created";
    }

    public static class IDENTIFIER {
        public static final String FAMILY_TEXT = "Family";

        public IDENTIFIER() {
        }
    }

    public static String getRelationWithHouseholdHead(String value){
        String relationshipObject = "{" +
                "  \"খানা প্রধান\": \"Household Head\"," +
                "  \"মা/আম্মা\": \"Mother\"," +
                "  \"বাবা/আব্বা\": \"Father\"," +
                "  \"ছেলে\": \"Son\"," +
                "  \"মেয়ে\": \"Daughter\"," +
                "  \"স্ত্রী\": \"Wife\"," +
                "  \"স্বামী\": \"Husband\"," +
                "  \"ভাই\": \"Brother\"," +
                "  \"বোন\": \"Brother\"," +
                "  \"নাতি\": \"Grandson\"," +
                "  \"নাতনী\": \"GrandDaughter\"," +
                "  \"ছেলের বউ\": \"SonsWife\"," +
                "  \"মেয়ের স্বামী\": \"DaughtersHusband\"," +
                "  \"শ্বশুর\": \"Father in law\"," +
                "  \"শাশুড়ি\": \"Mother in law\"," +
                "  \"দাদা\": \"Grandpa\"," +
                "  \"দাদি\": \"Grandma\"," +
                "  \"নানা\": \"Grandfather\"," +
                "  \"নানী\": \"Grandmother\"," +
                "  \"অতিথি\": \"Guest\"," +
                "  \"অন্যান্য\": \"Others\"" +
                "}";
        return getKeyByValue(relationshipObject,value);
    }
    public class JSON_FORMS {
        public static final String  ANC_CARD_FORM = "anc_card_form";
        public static final String  IMMUNIZATION = "hv_immunization";
        public static final String  DANGER_SIGNS = "anc_hv_danger_signs";

        public static final String  ANC_FORM = "hnpp_anc_registration";
        public static final String  ANC1_FORM = "hnpp_anc1_registration";
        public static final String  ANC1_FORM_OOC = "hnpp_anc1_registration_ooc";
        public static final String  ANC2_FORM = "hnpp_anc2_registration";
        public static final String  ANC2_FORM_OOC = "hnpp_anc2_registration_ooc";
        public static final String  ANC3_FORM = "hnpp_anc3_registration";
        public static final String  ANC3_FORM_OOC = "hnpp_anc3_registration_ooc";
        public static final String  GENERAL_DISEASE = "hnpp_anc_general_disease";
        public static final String  PREGNANCY_HISTORY = "hnpp_anc_pregnancy_history";
        public static final String  PREGNANCY_OUTCOME = "hnpp_anc_pregnancy_outcome";
        public static final String  PREGNANCY_OUTCOME_OOC = "hnpp_anc_pregnancy_outcome_ooc";
        public static final String  MEMBER_REFERRAL = "hnpp_member_referral";
        public static final String  MEMBER_REFERRAL_PA = "hnpp_member_referral_pa";
        public static final String  WOMEN_REFERRAL = "hnpp_women_referral";
        public static final String  CHILD_REFERRAL = "hnpp_child_referral";
        public static final String  ELCO = "elco_register";
        public static final String  PNC_FORM = "hnpp_pnc_registration";
        public static final String  PNC_FORM_BEFORE_48_HOUR = "hnpp_pnc_registration_before48_hour";
        public static final String  PNC_FORM_AFTER_48_HOUR = "hnpp_pnc_registration_after48_hour";
        public static final String  PNC_FORM_OOC = "hnpp_pnc_registration_ooc";
        public static final String  PNC_FORM_BEFORE_48_HOUR_OOC = "hnpp_pnc_registration_before48_hour_ooc";
        public static final String  PNC_FORM_AFTER_48_HOUR_OOC = "hnpp_pnc_registration_after48_hour_ooc";
        public static final String  WOMEN_PACKAGE = "hnpp_women_package";
        public static final String  EYE_TEST = "eye_test";
        public static final String  BLOOD_TEST = "blood_test";
        public static final String  GIRL_PACKAGE = "hnpp_adolescent_package";
        public static final String  NCD_PACKAGE = "hnpp_ncd_package";
        public static final String  IYCF_PACKAGE = "hnpp_iycf_package";
        public static final String ENC_REGISTRATION = "hnpp_enc_child";
        public static final String HOME_VISIT_FAMILY = "hnpp_hh_visit";

        public static final String REFERREL_FOLLOWUP = "hnpp_member_referral_followup";
        public static final String CHILD_FOLLOWUP = "hnpp_child_followup";
        public static final String CHILD_INFO_EBF12 = "child_info_ebf12";
        public static final String CHILD_INFO_7_24_MONTHS = "child_info_7_24_months";
        public static final String CHILD_INFO_25_MONTHS = "child_info_25_months";
        public static final String CORONA_INDIVIDUAL = "corona_individual";
        public static final String SS_FORM = "ss_form";
        public static final String GUEST_MEMBER_FORM = "guest_member_register";

    }
    public class INDICATOR {
        public static final String FP_uses = "fp_user";
        public static final String FP_no_method_uses = "fp_no_method_user";
        public static final String ANC_OTHER_SOURCE = "anc_other_source";
        public static final String ANC_TT = "anc_tt";
        public static final String OUTCOME_TT = "is_tt_completed";
        public static final String FEEDING_UPTO_6_MONTH = "feeding_6_month";
        public static final String PP_PILL = "contraceptive_pill";
        public static final String PP_CONDOM = "condom";
        public static final String PP_IUD = "iud";
        public static final String PP_INJECTION = "injection";
        public static final String PP_Implant = "norplant";
        public static final String PP_Vesectomy = "vasectomy";
        public static final String PP_Tubectomy = "ligation";


    }

    public class EVENT_TYPE{
        public static final String ELCO = "ELCO Registration";
        public static final String MEMBER_REFERRAL = "Member Referral";
        public static final String WOMEN_REFERRAL = "Women Referral";
        public static final String CHILD_REFERRAL = "Child Referral";
        public static final String ANC_PREGNANCY_HISTORY = "ANC Pregnancy History";
        public static final String ANC_GENERAL_DISEASE = "ANC General Disease";
        public static final String ANC1_REGISTRATION = "ANC1 Registration";
        public static final String ANC1_REGISTRATION_OOC = "ANC1 Registration OOC";
        public static final String ANC2_REGISTRATION = "ANC2 Registration";
        public static final String ANC2_REGISTRATION_OOC = "ANC2 Registration OOC";
        public static final String ANC3_REGISTRATION = "ANC3 Registration";
        public static final String ANC3_REGISTRATION_OOC = "ANC3 Registration OOC";
        public static final String ANC_REGISTRATION = "ANC Registration";
        public static final String UPDATE_ANC_REGISTRATION = "Update ANC Registration";

        public static final String PNC_REGISTRATION_BEFORE_48_hour = "PNC Visit Within 48_hr";
        public static final String PNC_REGISTRATION_AFTER_48_hour = "PNC Visit After 48_hr";

        public static final String PNC_REGISTRATION_BEFORE_48_hour_OOC = "PNC Visit Within 48_hr OOC";
        public static final String PNC_REGISTRATION_AFTER_48_hour_OOC = "PNC Visit After 48_hr OOC";
        public static final String WOMEN_PACKAGE = "Women package";
        public static final String GIRL_PACKAGE = "Adolescent package";
        public static final String NCD_PACKAGE = "NCD package";//pa
        public static final String EYE_TEST = "Eye test";//pa
        public static final String BLOOD_GROUP = "Blood group";//pa
        public static final String IYCF_PACKAGE = "IYCF package";
        public static final String ENC_REGISTRATION = "ENC Registration";
        public static final String HOME_VISIT_FAMILY = "HH visit";
        public static final String VACCINATION = "Vaccination";
        public static final String SERVICES = "Recurring Service";
        public static final String PREGNANCY_OUTCOME = "Pregnancy Outcome";
        public static final String PREGNANCY_OUTCOME_OOC = "OOC Pregnancy Outcome";
        public static final String REFERREL_FOLLOWUP = "Member Referral Followup";
        public static final String CHILD_INFO_EBF12 = "Child Info EBF 1_2";
        public static final String CHILD_INFO_7_24_MONTHS = "Child Info 7-24 months";
        public static final String CHILD_INFO_25_MONTHS = "Child Info 25 Months";
        public static final String CHILD_FOLLOWUP = "Child Followup";
        public static final String PNC_CHILD_REGISTRATION = "PNC Child Registration";
        public static final String UPDATE_CHILD_REGISTRATION = "Update Child Registration";
        public static final String FORUM_CHILD = "Child Forum";
        public static final String FORUM_ADO = "Adolescent Forum";
        public static final String FORUM_WOMEN = "WOMEN Forum";
        public static final String FORUM_NCD = "NCD Forum";
        public static final String FORUM_ADULT = "ADULT Forum";
        public static final String CORONA_INDIVIDUAL = "corona individual";
        public static final String SS_INFO = "SS Form";
        //for target
        public static final String METHOD_USER = "Methods Users";
        public static final String ADO_METHOD_USER = "Adolescent Methods Users";
        public static final String PREGNANCY_IDENTIFIED = "Pregnancy Identified";
        public static final String INSTITUTIONALIZES_DELIVERY = "Institutionalized Delivery";
        public static final String PREGNANCY_VISIT= "Pregnant Visit";
        public static final String CHILD_VISIT_0_6= "Child Visit(0-6 months)";
        public static final String CHILD_VISIT_7_24= "Child Visit(7-24 months)";
        public static final String CHILD_VISIT_18_36= "Child Visit(18-36 months)";
        public static final String CHILD_IMMUNIZATION_0_59= "Immunization(0-59 months)";
        public static final String AVG_ATTEND_ADO_FORUM= "Avg. Attendance (Adolescent Forum)";
        public static final String AVG_ATTEND_NCD_FORUM= "Avg. Attendance (NCD Forum)";
        public static final String AVG_ATTEND_ADULT_FORUM= "Avg Attendance (Adult Forum)";
        public static final String AVG_ATTEND_IYCF_FORUM= "Avg Attendance (IYCF Forum)";
        public static final String AVG_ATTEND_WOMEN_FORUM= "Avg Attendance (Women Forum)";

        //for PA target
        public static final String ADULT_FORUM_ATTENDANCE = "Avg. Attendance (Adult Forum)";
        public static final String ADULT_FORUM_SERVICE_TAKEN = "Adult Forum Service Taken";
        public static final String MARKED_PRESBYOPIA = "Marked as presbyopia";
        public static final String PRESBYOPIA_CORRECTION = "Presbyopia correction";
        public static final String ESTIMATE_DIABETES = "Estimate diabetes";
        public static final String ESTIMATE_HBP = "Estimate HBP";
        public static final String CATARACT_SURGERY_REFER = "Cataract surgery refer";
        public static final String CATARACT_SURGERY = "Cataract surgery";
        //public static final String NCD_BY_PA = "NCD by PA";

        //PA stock

        public static final String GLASS = "glass";
        public static final String SUN_GLASS = "Sun glass";
        public static final String SV_1 = "Sv 1";
        public static final String SV_1_5 = "Sv 1.5";
        public static final String SV_2 = "Sv 2";
        public static final String SV_2_5 = "Sv 2.5";
        public static final String SV_3 = "Sv 3";
        public static final String BF_1 = "Bf 1";
        public static final String BF_1_5 = "Bf 1.5";
        public static final String BF_2 = "Bf 2";
        public static final String BF_2_5 = "Bf 2.5";
        public static final String BF_3 = "Bf 3";

        //service
        public static final String ANC_SERVICE = "ANC Service";
        public static final String PNC_SERVICE = "PNC Service";
        public static final String GUEST_MEMBER_REGISTRATION = "OOC Member Registration";
        public static final String GUEST_MEMBER_UPDATE_REGISTRATION = "OOC Member Update Registration";
    }
    public static long getLongDateFormatForFromMonth(String year,String month){
        String dateFormate = year+"-"+HnppConstants.addZeroForMonth(month)+"-01";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long startDate = System.currentTimeMillis();
        try{
            Date date = format.parse(dateFormate);
            startDate = date.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return startDate;
    }
    public static String getStringDateFormatForFromMonth(String year, String month){
        return  year+"-"+HnppConstants.addZeroForMonth(month)+"-01";
    }
    public static String getStringDateFormatForToMonth(String year, String month){
        return year+"-"+HnppConstants.addZeroForMonth(month)+"-"+getLastDateOfAMonth(month);
    }
    public static long getLongDateFormatForToMonth(String year,String month){
        String dateFormate = year+"-"+HnppConstants.addZeroForMonth(month)+"-"+getLastDateOfAMonth(month);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long startDate = System.currentTimeMillis();
        try{
            Date date = format.parse(dateFormate);
            startDate = date.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return startDate;
    }
    public static String getLastDateOfAMonth(String month){
        if (TextUtils.isEmpty(month)) return "";
        int m = Integer.parseInt(month);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH,m);
        int lastDate = c.getActualMaximum(Calendar.DATE);
        return HnppConstants.addZeroForMonth(lastDate+"");
    }
    public static long getLongDateFormate(String year,String month,String day){
        String dateFormate = year+"-"+HnppConstants.addZeroForMonth(month)+"-"+HnppConstants.addZeroForDay(day);

        Log.v("DAILY_TERGET","dateStr:"+dateFormate);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        long startDate = System.currentTimeMillis();
        try{
            Date date = format.parse(dateFormate);
            startDate = date.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return startDate;
    }
    public static String getStringFormatedDate(String year,String month,String day){
        return   year+"-"+HnppConstants.addZeroForMonth(month)+"-"+HnppConstants.addZeroForDay(day);

    }
    public static long getLongFromDateFormat(String dateTime){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long milliseconds =0;
        try{
            Date d = format.parse(dateTime);
            milliseconds  = d.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return milliseconds;
    }
    public static String getDateFormateFromLong(long dateTime){
        Date date = new Date(dateTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = null;
        try{
            dateString = format.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dateString;
    }
    public static String getDateWithHHMMFormateFromLong(long dateTime){
        Date date = new Date(dateTime);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        String dateString = null;
        try{
            dateString = format.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dateString;
    }
    public static String getDDMMYYYYFormateFromLong(long dateTime){
        Date date = new Date(dateTime);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = null;
        try{
            dateString = format.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dateString;
    }
    public static final Map<String,String> genderMapping = ImmutableMap.<String,String> builder()
            .put("নারী","F")
            .put("পুরুষ","M")
            .put("তৃতীয় লিঙ্গ","O")
            .build();
    public static final Map<String,String> vaccineNameMapping = ImmutableMap.<String,String> builder()
            .put("bcg","বিসিজি")
            .put("opv_1","পোলিও-১")
            .put("pcv_1","পিসিভি-১")
            .put("penta_1","পেন্টা-১")
            .put("opv_2","পোলিও-২")
            .put("pcv_2","পিসিভি-২")
            .put("penta_2","পেন্টা-২")
            .put("opv_3","পোলিও-৩")
            .put("ipv","আই পি ভি")
            .put("pcv_3","পিসিভি-৩")
            .put("penta_3","পেন্টা-৩")
            .put("mr_1","MR-১")
            .put("mr_2","MR-২")
            .put("vitamin_a1","ভিটামিন")
            .build();
    public static final Map<String,String> eventTypeFormNameMapping = ImmutableMap.<String,String> builder()
            .put(EVENT_TYPE.ANC_REGISTRATION,JSON_FORMS.ANC_FORM)
            .put(EVENT_TYPE.ANC1_REGISTRATION,JSON_FORMS.ANC1_FORM)
            .put(EVENT_TYPE.ANC2_REGISTRATION,JSON_FORMS.ANC2_FORM)
            .put(EVENT_TYPE.ANC3_REGISTRATION,JSON_FORMS.ANC3_FORM)
            .put(EVENT_TYPE.ELCO,JSON_FORMS.ELCO)

            .put(EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour,JSON_FORMS.PNC_FORM_AFTER_48_HOUR)
            .put(EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour,JSON_FORMS.PNC_FORM_BEFORE_48_HOUR)
            .put(EVENT_TYPE.CHILD_INFO_EBF12,JSON_FORMS.CHILD_INFO_EBF12)
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,JSON_FORMS.CHILD_INFO_7_24_MONTHS)
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,JSON_FORMS.CHILD_INFO_25_MONTHS)
            .build();
    public static final Map<String,String> formNameEventTypeMapping = ImmutableMap.<String,String> builder()
            .put(JSON_FORMS.ANC1_FORM,EventType.ANC_HOME_VISIT)
            .put(JSON_FORMS.ANC2_FORM,EventType.ANC_HOME_VISIT)
            .put(JSON_FORMS.ANC3_FORM,EventType.ANC_HOME_VISIT)
            .put(JSON_FORMS.PNC_FORM_AFTER_48_HOUR,EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)
            .put(JSON_FORMS.PNC_FORM_BEFORE_48_HOUR,EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)
            .put(JSON_FORMS.NCD_PACKAGE,EVENT_TYPE.NCD_PACKAGE)
            .put(JSON_FORMS.IYCF_PACKAGE,EVENT_TYPE.IYCF_PACKAGE)
            .put(JSON_FORMS.WOMEN_PACKAGE,EVENT_TYPE.WOMEN_PACKAGE)
            .put(JSON_FORMS.GIRL_PACKAGE,EVENT_TYPE.GIRL_PACKAGE)
            .build();
    public static final Map<String,String> guestEventTypeFormNameMapping = ImmutableMap.<String,String> builder()
            .put(EVENT_TYPE.ANC_REGISTRATION,JSON_FORMS.ANC_FORM)
            .put(EVENT_TYPE.ANC1_REGISTRATION,JSON_FORMS.ANC1_FORM_OOC)
            .put(EVENT_TYPE.ANC2_REGISTRATION,JSON_FORMS.ANC2_FORM_OOC)
            .put(EVENT_TYPE.ANC3_REGISTRATION,JSON_FORMS.ANC3_FORM_OOC)

            .put(EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC,JSON_FORMS.PNC_FORM_AFTER_48_HOUR_OOC)
            .put(EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC,JSON_FORMS.PNC_FORM_BEFORE_48_HOUR_OOC)

            .build();
    public static final Map<String,Integer> iconMapping = ImmutableMap.<String,Integer> builder()
            .put("গর্ভবতী পরিচর্যা-১ম ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("গর্ভবতী পরিচর্যা - ২য় ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("গর্ভবতী পরিচর্যা - ৩য় ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("শারীরিক সমস্যা",R.mipmap.ic_anc_pink)
            .put( "পূর্বের গর্ভের ইতিহাস",R.mipmap.ic_anc_pink)

            .put(EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.PREGNANCY_OUTCOME,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.ANC1_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC2_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC3_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_GENERAL_DISEASE,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_PREGNANCY_HISTORY,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.UPDATE_ANC_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ELCO,R.drawable.ic_elco)
            .put(EventType.REMOVE_FAMILY,R.drawable.ic_remove)
            .put(EventType.REMOVE_MEMBER,R.drawable.ic_remove)
            .put(HnppConstants.EventType.FAMILY_REGISTRATION,R.drawable.ic_home)
            .put(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION,R.drawable.rowavatar_member)
            .put(HnppConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,R.drawable.rowavatar_member)
            .put(HnppConstants.EventType.CHILD_REGISTRATION,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.MEMBER_REFERRAL,R.mipmap.ic_refer)
            .put(EVENT_TYPE.WOMEN_REFERRAL,R.mipmap.ic_refer)
            .put(EVENT_TYPE.CHILD_REFERRAL,R.mipmap.ic_refer)
            .put(EVENT_TYPE.WOMEN_PACKAGE,R.drawable.ic_women)
            .put(EVENT_TYPE.GIRL_PACKAGE, R.drawable.ic_adolescent)
            .put(EVENT_TYPE.NCD_PACKAGE,R.drawable.ic_muac)
            .put(EVENT_TYPE.BLOOD_GROUP,R.drawable.ic_blood)
            .put(EVENT_TYPE.EYE_TEST,R.drawable.ic_eye)
            .put(EVENT_TYPE.IYCF_PACKAGE, R.drawable.child_girl_infant)
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,R.mipmap.ic_anc_pink)

            .put(EVENT_TYPE.ENC_REGISTRATION,R.mipmap.ic_child)
            .put("Member referral",R.mipmap.ic_refer)
            .put(EVENT_TYPE.HOME_VISIT_FAMILY, R.mipmap.ic_icon_home)
            .put(EventType.CHILD_HOME_VISIT, R.mipmap.ic_icon_home)
            .put(EVENT_TYPE.VACCINATION, R.drawable.ic_muac)
            .put(EVENT_TYPE.SERVICES, R.mipmap.form_vitamin)
            .put(EVENT_TYPE.REFERREL_FOLLOWUP,R.mipmap.ic_refer)
            .put(EVENT_TYPE.CHILD_FOLLOWUP,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_INFO_EBF12,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.PNC_CHILD_REGISTRATION,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.UPDATE_CHILD_REGISTRATION,R.drawable.rowavatar_child)
            .put("Update Family Registration",R.mipmap.ic_icon_home)
            .put(EVENT_TYPE.CORONA_INDIVIDUAL,R.drawable.ic_virus)
            .put(EVENT_TYPE.FORUM_ADULT,R.drawable.ic_familiar)
            .put(EVENT_TYPE.FORUM_WOMEN,R.drawable.ic_women)
            .put(EVENT_TYPE.FORUM_ADO,R.drawable.ic_adolescent)
            .put(EVENT_TYPE.FORUM_CHILD,R.drawable.ic_child)
            .put(EVENT_TYPE.FORUM_NCD,R.drawable.ic_sugar_blood_level)
            .put("ANC",R.mipmap.ic_anc_pink)
            .put("pnc",R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.GLASS,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SUN_GLASS,R.drawable.ic_sun_glasses)
            .put(EVENT_TYPE.SV_1,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_1_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_2,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_2_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_3,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_1,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_1_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_2,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_2_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_3,R.drawable.ic_glasses)
            .build();
    //need to show the title at row/option
    public static final Map<String,String> visitEventTypeMapping = ImmutableMap.<String,String> builder()
            .put(EVENT_TYPE.ANC_REGISTRATION,"গর্ভবতী রেজিস্ট্রেশন")
            .put(EVENT_TYPE.ANC1_REGISTRATION,"গর্ভবতী পরিচর্যা - ১ম ত্রিমাসিক")
            .put(EVENT_TYPE.ANC2_REGISTRATION,"গর্ভবতী পরিচর্যা - ২য় ত্রিমাসিক")
            .put(EVENT_TYPE.ANC3_REGISTRATION,"গর্ভবতী পরিচর্যা - ৩য় ত্রিমাসিক")
            .put(EVENT_TYPE.ANC_GENERAL_DISEASE,"শারীরিক সমস্যা")
            .put(EVENT_TYPE.ANC_PREGNANCY_HISTORY,"পূর্বের গর্ভের ইতিহাস")
            .put(EVENT_TYPE.ELCO,"সক্ষম দম্পতি পরিদর্শন")
            .put(JSON_FORMS.ANC_FORM,"গর্ভবতী পরিচর্যা")
            .put(JSON_FORMS.ANC1_FORM,"গর্ভবতী পরিচর্যা - ১ম ত্রিমাসিক")
            .put(JSON_FORMS.ANC2_FORM,"গর্ভবতী পরিচর্যা - ২য় ত্রিমাসিক")
            .put(JSON_FORMS.ANC3_FORM,"গর্ভবতী পরিচর্যা - ৩য় ত্রিমাসিক")
            .put(JSON_FORMS.GENERAL_DISEASE,"শারীরিক সমস্যা")
            .put(EVENT_TYPE.MEMBER_REFERRAL,"রেফারেল")
            .put(EVENT_TYPE.WOMEN_REFERRAL,"রেফারেল")
            .put(EVENT_TYPE.CHILD_REFERRAL,"রেফারেল")
            .put("Member referral","রেফারেল")
            .put( JSON_FORMS.PREGNANCY_HISTORY,"পূর্বের গর্ভের ইতিহাস")
            .put( EVENT_TYPE.PREGNANCY_OUTCOME,"প্রসবের ফলাফল")
            .put( EVENT_TYPE.PREGNANCY_OUTCOME_OOC,"প্রসবের ফলাফল")
            .put( JSON_FORMS.PNC_FORM,"প্রসবোত্তর পরিচর্যা")

            .put( EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour,"পি.এন.সি. (প্রথম ৪৮ ঘন্টা পর)")
            .put( EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour,"পি.এন.সি.(প্রথম ৪৮ ঘন্টার মধ্য)")
            .put( EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC,"পি.এন.সি. (প্রথম ৪৮ ঘন্টা পর)")
            .put( EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC,"পি.এন.সি.(প্রথম ৪৮ ঘন্টার মধ্য)")
            .put(EVENT_TYPE.WOMEN_PACKAGE,"নারী সেবা প্যাকেজ")
            .put(EVENT_TYPE.GIRL_PACKAGE, "কিশোরী সেবা প্যাকেজ")
            .put(EVENT_TYPE.NCD_PACKAGE, "অসংক্রামক রোগের সেবা")
            .put(EVENT_TYPE.BLOOD_GROUP,"ব্লাড গ্রুপ")
            .put(EVENT_TYPE.EYE_TEST,"চক্ষু পরীক্ষা")
            .put(EVENT_TYPE.IYCF_PACKAGE, "শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)")
            .put(EVENT_TYPE.ENC_REGISTRATION, "নবজাতকের সেবা")
            .put(EVENT_TYPE.HOME_VISIT_FAMILY, "খানা পরিদর্শন")
            .put(EVENT_TYPE.VACCINATION, "ভ্যাকসিনেশন")
            .put(EVENT_TYPE.SERVICES, "ভিটামিন সার্ভিস")
            .put(EVENT_TYPE.REFERREL_FOLLOWUP,"রেফারেল ফলোআপ")
            .put(EVENT_TYPE.CHILD_FOLLOWUP,"শিশু ফলোআপ")
            .put(EVENT_TYPE.CHILD_INFO_EBF12,"শিশু তথ্য")
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,"শিশু তথ্য")
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,"শিশু তথ্য")
            .put(EVENT_TYPE.PNC_CHILD_REGISTRATION,"প্রসবের ফলাফল-শিশু")
            .put(EVENT_TYPE.UPDATE_CHILD_REGISTRATION,"শিশু নিবন্ধন আপডেট")
            .put("Update Family Registration","খানা নিবন্ধন আপডেট")
            .put(EventType.REMOVE_FAMILY,"খানা বাতিল")
            .put(EventType.REMOVE_MEMBER,"সদস্যকে বাতিল")
            .put(EventType.REMOVE_CHILD,"শিশু বাতিল")
            .put(EVENT_TYPE.CORONA_INDIVIDUAL,"করোনা তথ্য")
            .put(EVENT_TYPE.SS_INFO,"স্বাস্থ্য সেবিকা তথ্য")
            .put(EVENT_TYPE.FORUM_ADO,"কিশোরী ফোরাম")
            .put(EVENT_TYPE.FORUM_WOMEN,"নারী ফোরাম")
            .put(EVENT_TYPE.FORUM_CHILD,"শিশু ফোরাম")
            .put(EVENT_TYPE.FORUM_NCD,"অসংক্রামক রোগের ফোরাম")
            .put(EVENT_TYPE.FORUM_ADULT,"অ্যাডাল্ট ফোরাম")
            .build();

    //for dashboard poridorshon
    public static final Map<String,String> targetTypeMapping = ImmutableMap.<String,String> builder()

            .put(EVENT_TYPE.HOME_VISIT_FAMILY,"খানা পরিদর্শন")
            .put(EVENT_TYPE.ELCO,"সক্ষম দম্পতি পরিদর্শন")
            .put(EVENT_TYPE.METHOD_USER,"পদ্ধতি ব্যবহারকারী")
            .put(EVENT_TYPE.ADO_METHOD_USER,"পদ্ধতি ব্যবহারকারী (কিশোরী)")
            .put(EVENT_TYPE.PREGNANCY_IDENTIFIED,"গর্ভবতী চিহ্নিত")
            .put(EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,"প্রাতিষ্ঠানিক প্রসব সংখ্যা")
            .put(EVENT_TYPE.CHILD_VISIT_0_6,"০-৬ মাস বয়সী শিশু পরিদর্শন")
            .put(EVENT_TYPE.CHILD_VISIT_7_24,"৭-২৪ মাস বয়সী শিশু প্রদর্শন")
            .put(EVENT_TYPE.CHILD_VISIT_18_36,"১৮-৩৬ মাস বয়সী শিশু পরিদর্শন")
            .put(EVENT_TYPE.CHILD_IMMUNIZATION_0_59,"০-৫৯ মাস বয়সী শিশুর টিকা")
            .put(EVENT_TYPE.FORUM_ADO,"কিশোরী ফোরাম")
            .put(EVENT_TYPE.FORUM_WOMEN,"নারী ফোরাম")
            .put(EVENT_TYPE.FORUM_CHILD,"শিশু ফোরাম")
            .put(EVENT_TYPE.FORUM_NCD,"অসংক্রামক রোগের ফোরাম")
            .put(EVENT_TYPE.FORUM_ADULT,"অ্যাডাল্ট ফোরাম")
            .put(EVENT_TYPE.PREGNANCY_OUTCOME,"প্রসব")
            .put(EVENT_TYPE.GIRL_PACKAGE,"কিশোরী সেবা")
            .put(EVENT_TYPE.WOMEN_PACKAGE,"নারী সেবা")
            .put(EVENT_TYPE.IYCF_PACKAGE,"শিশু সেবা")
            .put(EVENT_TYPE.NCD_PACKAGE,"অসংক্রামক রোগের সেবা")
            .put(EVENT_TYPE.ANC_SERVICE,"গর্ভবতী সেবা")
            .put(EVENT_TYPE.PNC_SERVICE,"প্রসব-পরবর্তী সেবা")
            .put(EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,"অংশগ্রহণকারী সংখ্যা")
            .put(EVENT_TYPE.ADULT_FORUM_ATTENDANCE,"অংশগ্রহণকারী সংখ্যা")
           // .put(EVENT_TYPE.NCD_BY_PA,"অসংক্রামক রোগের সেবা")
            .put(EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN,"সেবা গ্রহীতার সংখ্যা")
            .put(EVENT_TYPE.MARKED_PRESBYOPIA,"চিহ্নিত প্রেসবায়োপিয়া")
            .put(EVENT_TYPE.PRESBYOPIA_CORRECTION,"প্রেসবায়োপিয়া কারেকশন")
            .put(EVENT_TYPE.ESTIMATE_DIABETES,"সম্ভাব্য ডায়াবেটিস")
            .put(EVENT_TYPE.ESTIMATE_HBP,"সম্ভাব্য উচ্চ রক্তচাপ")
            .put(EVENT_TYPE.CATARACT_SURGERY_REFER,"ছানি অপারেশন এ রেফার")
            .put(EVENT_TYPE.CATARACT_SURGERY,"ছানি অপারেশন")
            .build();
    //for dashboard workSummery
    public static final Map<String,String> workSummeryTypeMapping = ImmutableMap.<String,String> builder()

            .put(HnppConstants.EventType.FAMILY_REGISTRATION,"খানা রেজিস্ট্রেশন")
            .put(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION,"সদস্য রেজিস্ট্রেশন")
            .put(EVENT_TYPE.HOME_VISIT_FAMILY,"খানা ভিজিট")
            .put(HnppConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,"সদস্য রেজিস্ট্রেশন")
            .put(EVENT_TYPE.ANC1_REGISTRATION,"গর্ভবতী পরিচর্যা - ১ম ত্রিমাসিক")
            .put(EVENT_TYPE.ANC2_REGISTRATION,"গর্ভবতী পরিচর্যা - ২য় ত্রিমাসিক")
            .put(EVENT_TYPE.ANC3_REGISTRATION,"গর্ভবতী পরিচর্যা - ৩য় ত্রিমাসিক")
            .put("ANC","গর্ভবতী পরিচর্যা(এএনসি)")
            .put("pnc","প্রসবোত্তর পরিচর্যা(পিএনসি)")
            .put(EVENT_TYPE.ELCO,"সক্ষম দম্পতি পরিদর্শন")
            .put(HnppConstants.EventType.CHILD_REGISTRATION,"শিশু রেজিস্ট্রেশন")
            .put(EVENT_TYPE.ANC_REGISTRATION,"গর্ভবতী রেজিস্ট্রেশন")
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,"গর্ভবতী পরিচর্যা ভিজিট(এএনসি)")

            .put(EVENT_TYPE.PREGNANCY_OUTCOME,"প্রসব")
            .put(EVENT_TYPE.ENC_REGISTRATION, "নবজাতকের সেবা")
            .put(EVENT_TYPE.CHILD_FOLLOWUP,"শিশু ফলোআপ")
            .put(EVENT_TYPE.CHILD_INFO_EBF12,"শিশু তথ্য")
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,"শিশু তথ্য")
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,"শিশু তথ্য")
            .put(EVENT_TYPE.FORUM_ADO,"কিশোরী ফোরাম")
            .put(EVENT_TYPE.FORUM_WOMEN,"নারী ফোরাম")
            .put(EVENT_TYPE.FORUM_CHILD,"শিশু ফোরাম")
            .put(EVENT_TYPE.FORUM_NCD,"অসংক্রামক রোগের ফোরাম")
            .put(EVENT_TYPE.FORUM_ADULT,"অ্যাডাল্ট ফোরাম")
            .put(EVENT_TYPE.WOMEN_PACKAGE,"নারী সেবা প্যাকেজ")
            .put(EVENT_TYPE.GIRL_PACKAGE, "কিশোরী সেবা প্যাকেজ")
            .put(EVENT_TYPE.NCD_PACKAGE, "অসংক্রামক রোগের সেবা")
            .put(EVENT_TYPE.BLOOD_GROUP,"ব্লাড গ্রুপ")
            .put(EVENT_TYPE.EYE_TEST,"চক্ষু পরীক্ষা")
            .put(EVENT_TYPE.GLASS,"মোট চশমা")
            .put(EVENT_TYPE.SUN_GLASS,"সানগ্লাস")
            .put(EVENT_TYPE.SV_1,"SV- 1.00")
            .put(EVENT_TYPE.SV_1_5,"SV- 1.50")
            .put(EVENT_TYPE.SV_2,"SV- 2.00")
            .put(EVENT_TYPE.SV_2_5,"SV- 2.50")
            .put(EVENT_TYPE.SV_3,"SV- 3.00")
            .put(EVENT_TYPE.BF_1,"BF- 1.00")
            .put(EVENT_TYPE.BF_1_5,"BF- 1.50")
            .put(EVENT_TYPE.BF_2,"BF- 2.00")
            .put(EVENT_TYPE.BF_2_5,"BF- 2.50")
            .put(EVENT_TYPE.BF_3,"BF- 3.00")

            .put(EVENT_TYPE.IYCF_PACKAGE, "শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)")
            .put("familyplanning_method_known", "পরিবার পরিকল্পনা পদ্ধতি ব্যবহারকারী")
            .put(EVENT_TYPE.ANC_SERVICE,"গর্ভবতী সেবা")
            .put(EVENT_TYPE.PNC_SERVICE,"প্রসব-পরবর্তী সেবা")
            .put(EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour,"পি.এন.সি.(প্রথম ৪৮ ঘন্টার মধ্য)")
            .put(EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour,"পি.এন.সি.(প্রথম ৪৮ ঘন্টা পর)")

            .build();
    //for dashboard countSummery
    public static final Map<String,String> countSummeryTypeMapping = ImmutableMap.<String,String> builder()

            .put(HnppConstants.EventType.FAMILY_REGISTRATION,"খানা সংখ্যা")
            .put(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION,"সদস্য সংখ্যা")
            .put(HnppConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,"সদস্য সংখ্যা")
            .put(HnppConstants.EventType.CHILD_REGISTRATION,"শিশু সংখ্যা")
            .put(EVENT_TYPE.ANC_REGISTRATION,"গর্ভবতী রেজিস্ট্রেশন")
            .put(EVENT_TYPE.HOME_VISIT_FAMILY,"খানা ভিজিট")
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,"গর্ভবতী পরিচর্যা ভিজিট(এএনসি)")
            .put(EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour,"পি.এন.সি.(প্রথম ৪৮ ঘন্টার মধ্য)")
            .put(EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour,"পি.এন.সি.(প্রথম ৪৮ ঘন্টা পর)")

            .put(EVENT_TYPE.PREGNANCY_OUTCOME,"প্রসব")
            .build();



    public static final Map<String,String> eventTypeMapping = ImmutableMap.<String,String> builder()
            .put(HnppConstants.EventType.FAMILY_REGISTRATION,"খানা নিবন্ধন")
            .put(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION,"সদস্য নিবন্ধন")
            .put(HnppConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,"সদস্য আপডেট")
            .put(HnppConstants.EventType.CHILD_REGISTRATION,"শিশু নিবন্ধন")
            .put(HnppConstants.EVENT_TYPE.MEMBER_REFERRAL,"রেফারেল")
            .put(EVENT_TYPE.WOMEN_REFERRAL,"রেফারেল")
            .put(EVENT_TYPE.CHILD_REFERRAL,"রেফারেল")
            .put(EVENT_TYPE.PREGNANCY_OUTCOME,"প্রসব")
            .put("Member referral","রেফারেল")
            .put(EVENT_TYPE.ELCO,"সক্ষম দম্পতি পরিদর্শন")
            .put(EVENT_TYPE.ANC_REGISTRATION,"গর্ভবতী রেজিস্ট্রেশন")
            .put(EVENT_TYPE.UPDATE_ANC_REGISTRATION,"গর্ভবতী রেজিস্ট্রেশন আপডেট")
            .put(EVENT_TYPE.WOMEN_PACKAGE,"নারী সেবা প্যাকেজ")
            .put(EVENT_TYPE.GIRL_PACKAGE, "কিশোরী সেবা প্যাকেজ")
            .put(EVENT_TYPE.NCD_PACKAGE, "অসংক্রামক রোগের সেবা")
            .put(EVENT_TYPE.BLOOD_GROUP,"ব্লাড গ্রুপ")
            .put(EVENT_TYPE.EYE_TEST,"চক্ষু পরীক্ষা")
            .put(EVENT_TYPE.IYCF_PACKAGE, "শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)")
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,"গর্ভবতী পরিচর্যা ভিজিট(এএনসি)")


            .put( EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour,"পি.এন.সি. (প্রথম ৪৮ ঘন্টা পর)")
            .put( EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour,"পি.এন.সি.(প্রথম ৪৮ ঘন্টার মধ্য)")
            .put( EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC,"পি.এন.সি. (প্রথম ৪৮ ঘন্টা পর)")
            .put( EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC,"পি.এন.সি.(প্রথম ৪৮ ঘন্টার মধ্য)")
            .put(EVENT_TYPE.ENC_REGISTRATION, "নবজাতকের সেবা")
            .put(EVENT_TYPE.HOME_VISIT_FAMILY, "খানা পরিদর্শন")
            .put(EventType.CHILD_HOME_VISIT, "শিশু হোম ভিজিট")
            .put(EVENT_TYPE.VACCINATION, "ভ্যাকসিনেশন")
            .put(EVENT_TYPE.SERVICES, "ভিটামিন সার্ভিস")
            .put(EVENT_TYPE.REFERREL_FOLLOWUP,"রেফারেল ফলোআপ")
            .put(EVENT_TYPE.CHILD_FOLLOWUP,"শিশু ফলোআপ")
            .put(EVENT_TYPE.CHILD_INFO_EBF12,"শিশু তথ্য")
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,"শিশু তথ্য")
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,"শিশু তথ্য")
            .put(EVENT_TYPE.PNC_CHILD_REGISTRATION,"প্রসবের ফলাফল-শিশু")
            .put(EVENT_TYPE.UPDATE_CHILD_REGISTRATION,"শিশু নিবন্ধন আপডেট")
            .put("Update Family Registration","খানা নিবন্ধন আপডেট")
            .put(EventType.REMOVE_FAMILY,"খানা বাতিল")
            .put(EventType.REMOVE_MEMBER,"সদস্যকে বাতিল")
            .put(EventType.REMOVE_CHILD,"শিশু বাতিল")
            .put(EVENT_TYPE.CORONA_INDIVIDUAL,"করোনা তথ্য")
            .put(EVENT_TYPE.SS_INFO,"স্বাস্থ্য সেবিকা তথ্য")
            .put(EVENT_TYPE.FORUM_ADO,"কিশোরী ফোরাম")
            .put(EVENT_TYPE.FORUM_WOMEN,"নারী ফোরাম")
            .put(EVENT_TYPE.FORUM_CHILD,"শিশু ফোরাম")
            .put(EVENT_TYPE.FORUM_NCD,"অসংক্রামক রোগের ফোরাম")
            .put(EVENT_TYPE.FORUM_ADULT,"অ্যাডাল্ট ফোরাম")
            .put(EVENT_TYPE.ANC_SERVICE,"গর্ভবতী সেবা")
            .put(EVENT_TYPE.PNC_SERVICE,"প্রসব-পরবর্তী সেবা")

            .put(EVENT_TYPE.ANC1_REGISTRATION,"গর্ভবতী পরিচর্যা - ১ম ত্রিমাসিক")
            .put(EVENT_TYPE.ANC2_REGISTRATION,"গর্ভবতী পরিচর্যা - ২য় ত্রিমাসিক")
            .put(EVENT_TYPE.ANC3_REGISTRATION,"গর্ভবতী পরিচর্যা - ৩য় ত্রিমাসিক")
            .put("Guest Member Registration","বহিরাগত রেজিস্ট্রেশন")
            .put("OOC Member Registration","বহিরাগত রেজিস্ট্রেশন")
            .build();
    public static final Map<String,String> immunizationMapping = ImmutableMap.<String,String> builder()
            .put("PENTA 1","পেন্টা-১")
            .put("PENTA 2","পেন্টা-২")
            .put("PENTA 3","পেন্টা-৩")
            .put("OPV 1","পোলিও-১")
            .put("OPV 2","পোলিও-২")
            .put("OPV 3","পোলিও-৩")
            .put("PCV 1","পিসিভি-১")
            .put("PCV 2","পিসিভি-২")
            .put("PCV 3","পিসিভি-৩")
            .put("BCG","বিসিজি")
            .put("VITAMIN A1","ভিটামিন এ")
            .build();
    public static final Map<String,String> referealResonMapping = ImmutableMap.<String,String> builder()
            .put("child_problems","শিশু বিষয়ক সমস্যা")
            .put("pregnancy_problems","গর্ভাবস্থার সমস্যা")
            .put("delivery_problems","প্রসবে সমস্যা")
            .put("pnc_problem","প্রসব পরবর্তী সমস্যা")
            .put("problems_eyes","চোখে সমস্যা")
            .put("diabetes","ডায়বেটিস")
            .put("high_blood_pressure","উচ্চ রক্তচাপ")
            .put("problems_with_birth_control","জন্মবিরতিকরণ পদ্ধতি সংক্রান্ত সমস্যা")
            .put("cataract_problem","চোখে ক্যাটারেক্ট (ছানি) সমস্যা")
            .put("other","অন্যান্য")
            .build();
    public static final Map<String,String> referealPlaceMapping = ImmutableMap.<String,String> builder()
            .put("brac_maternity_center", "ব্র্যাক ম্যাটারনিটি সেন্টার")
            .put("union_health_center","ইউনিয়ন উপ-স্বাস্থ্য কেন্দ্র")
            .put("upozzila_health_complex","উপজেলা স্বাস্থ্য কমপ্লেক্স")
            .put("union_family_kollan_center","ইউনিয়ন পরিবার কল্যাণ কেন্দ্র")
            .put("union_health_and_family_kollan_center","ইউনিয়ন স্বাস্থ্য ও পরিবার কল্যাণ কেন্দ্র")
            .put("mother_child_kollan_center","মা ও শিশু কল্যাণ কেন্দ্র")
            .put("center_hospital","সদর হাসপাতাল")
            .put("medical_collage_hospital","মেডিকেল কলেজ হাসপাতাল")
            .put("private_clinic","বেসরকারি ক্লিনিক")
            .put("spacial_hospital","বিশেষায়িত হাসপাতাল")
            .put("other_option","অন্যান্য")
            .build();

    private static String getKeyByValue(String mapperObj, String value){
        try {
            JSONObject choiceObject = new JSONObject(mapperObj);
            for (int i = 0; i < choiceObject.names().length(); i++) {
                if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                    value = choiceObject.names().getString(i);
                    return value;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }
}
