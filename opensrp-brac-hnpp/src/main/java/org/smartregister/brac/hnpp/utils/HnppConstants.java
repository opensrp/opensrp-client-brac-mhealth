package org.smartregister.brac.hnpp.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.R;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HnppConstants extends CoreConstants {
    public static final String TEST_GU_ID = "test";
    public static final String MODULE_ID_TRAINING = "TRAINING";
    public static final int MEMBER_ID_SUFFIX = 11;
    public static final int HOUSE_HOLD_ID_SUFFIX = 9;
    public static final String IS_RELEASE = "is_release_build";
    public static final String IS_DEVICE_VERIFY = "is_device_verify";
    public static final String DEVICE_IMEI = "device_imei";

    public static SimpleDateFormat DDMMYY = new SimpleDateFormat("dd-MM-yyyy");
    public enum VisitType {DUE, OVERDUE, LESS_TWENTY_FOUR, VISIT_THIS_MONTH, NOT_VISIT_THIS_MONTH, EXPIRY, VISIT_DONE}
    public enum HomeVisitType {GREEN, YELLOW, RED, BROWN}
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
    }

    public static String getHomeVisitStatus(long lastHomeVisit , String dateCreatedStr){

        LocalDate lastVisitDate = new LocalDate(lastHomeVisit);

        LocalDate dateCreated = new LocalDate(TextUtils.isEmpty(dateCreatedStr) ? System.currentTimeMillis(): new DateTime(dateCreatedStr).toLocalDate());

        LocalDate todayDate = new LocalDate();
        int monthDiff = getMonthsDifference((lastHomeVisit != 0 ? lastVisitDate : dateCreated), todayDate);
        if(monthDiff >= 8) return HomeVisitType.BROWN.name();
        if(monthDiff >= 5) return HomeVisitType.RED.name();
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

    public static boolean isReleaseBuild() {
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        String isReleaseBuild = preferences.getPreference(IS_RELEASE);
        if (TextUtils.isEmpty(isReleaseBuild) || isReleaseBuild.equalsIgnoreCase("L")) {
            return true;
        }
        return false;
    }

    public static String getDeviceId(TelephonyManager mTelephonyManager, Context context,boolean fromSettings) {
        String deviceId = null;
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
        public static final String  ANC2_FORM = "hnpp_anc2_registration";
        public static final String  ANC3_FORM = "hnpp_anc3_registration";
        public static final String  GENERAL_DISEASE = "hnpp_anc_general_disease";
        public static final String  PREGNANCY_HISTORY = "hnpp_anc_pregnancy_history";
        public static final String  PREGNANCY_OUTCOME = "hnpp_anc_pregnancy_outcome";
        public static final String  MEMBER_REFERRAL = "hnpp_member_referral";
        public static final String  ELCO = "elco_register";
        public static final String  PNC_FORM = "hnpp_pnc_registration";
        public static final String  WOMEN_PACKAGE = "hnpp_women_package";
        public static final String  GIRL_PACKAGE = "hnpp_adolescent_package";
        public static final String  NCD_PACKAGE = "hnpp_ncd_package";
        public static final String  IYCF_PACKAGE = "hnpp_iycf_package";
        public static final String ENC_REGISTRATION = "hnpp_enc_child";
        public static final String HOME_VISIT_FAMILY = "hnpp_hh_visit";

    }

    public class EVENT_TYPE{
        public static final String ELCO = "ELCO Registration";
        public static final String MEMBER_REFERRAL = "Member Referral";
        public static final String ANC_PREGNANCY_HISTORY = "ANC Pregnancy History";
        public static final String ANC_GENERAL_DISEASE = "ANC General Disease";
        public static final String ANC1_REGISTRATION = "ANC1 Registration";
        public static final String ANC2_REGISTRATION = "ANC2 Registration";
        public static final String ANC3_REGISTRATION = "ANC3 Registration";
        public static final String ANC_REGISTRATION = "ANC Registration";
        public static final String UPDATE_ANC_REGISTRATION = "Update ANC Registration";
        public static final String PNC_REGISTRATION = "PNC Registration";
        public static final String WOMEN_PACKAGE = "Women package";
        public static final String GIRL_PACKAGE = "Adolescent package";
        public static final String NCD_PACKAGE = "NCD package";
        public static final String IYCF_PACKAGE = "IYCF package";
        public static final String ENC_REGISTRATION = "ENC Registration";
        public static final String HOME_VISIT_FAMILY = "HH visit";


    }
    public static final Map<String,String> eventTypeFormNameMapping = ImmutableMap.<String,String> builder()
            .put(EVENT_TYPE.ANC1_REGISTRATION,JSON_FORMS.ANC1_FORM)
            .put(EVENT_TYPE.ANC2_REGISTRATION,JSON_FORMS.ANC2_FORM)
            .put(EVENT_TYPE.ANC3_REGISTRATION,JSON_FORMS.ANC3_FORM)
            .put(EVENT_TYPE.ELCO,JSON_FORMS.ELCO)
            .put(EVENT_TYPE.PNC_REGISTRATION,JSON_FORMS.PNC_FORM)
            .build();
    public static final Map<String,Integer> iconMapping = ImmutableMap.<String,Integer> builder()
            .put("গর্ভবতী পরিচর্যা-১ম ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("গর্ভবতী পরিচর্যা - ২য় ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("গর্ভবতী পরিচর্যা - ৩য় ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("শারীরিক সমস্যা",R.mipmap.ic_anc_pink)
            .put( "পূর্বের গর্ভের ইতিহাস",R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.PNC_REGISTRATION,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.ANC1_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC2_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC3_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_GENERAL_DISEASE,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_PREGNANCY_HISTORY,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.UPDATE_ANC_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ELCO,R.drawable.ic_elco)
            .put(HnppConstants.EventType.FAMILY_REGISTRATION,R.drawable.ic_home)
            .put(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION,R.drawable.rowavatar_member)
            .put(HnppConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,R.drawable.rowavatar_member)
            .put(HnppConstants.EventType.CHILD_REGISTRATION,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.MEMBER_REFERRAL,R.mipmap.ic_refer)
            .put(EVENT_TYPE.WOMEN_PACKAGE,R.drawable.woman_placeholder)
            .put(EVENT_TYPE.GIRL_PACKAGE, R.drawable.woman_placeholder)
            .put(EVENT_TYPE.NCD_PACKAGE,R.drawable.ic_muac)
            .put(EVENT_TYPE.IYCF_PACKAGE, R.drawable.child_girl_infant)
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,R.mipmap.ic_anc_pink)
            .put(Constants.EVENT_TYPE.PNC_HOME_VISIT,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.ENC_REGISTRATION,R.mipmap.ic_child)
            .put("Member referral",R.mipmap.ic_refer)
            .put(EVENT_TYPE.HOME_VISIT_FAMILY, R.mipmap.ic_icon_home)
            .build();
    //need to show the title at row/option
    public static final Map<String,String> visitEventTypeMapping = ImmutableMap.<String,String> builder()
            .put(EVENT_TYPE.ANC1_REGISTRATION,"গর্ভবতী পরিচর্যা - ১ম ত্রিমাসিক")
            .put(EVENT_TYPE.ANC2_REGISTRATION,"গর্ভবতী পরিচর্যা - ২য় ত্রিমাসিক")
            .put(EVENT_TYPE.ANC3_REGISTRATION,"গর্ভবতী পরিচর্যা - ৩য় ত্রিমাসিক")
            .put(EVENT_TYPE.ANC_GENERAL_DISEASE,"শারীরিক সমস্যা")
            .put(EVENT_TYPE.ANC_PREGNANCY_HISTORY,"পূর্বের গর্ভের ইতিহাস")
            .put(EVENT_TYPE.ELCO,"সক্ষম দম্পতি নিবন্ধন")
            .put(JSON_FORMS.ANC1_FORM,"গর্ভবতী পরিচর্যা")
            .put(JSON_FORMS.GENERAL_DISEASE,"শারীরিক সমস্যা")
            .put(EVENT_TYPE.MEMBER_REFERRAL,"রেফারেল")
            .put("Member referral","রেফারেল")
            .put( JSON_FORMS.PREGNANCY_HISTORY,"পূর্বের গর্ভের ইতিহাস")
            .put( JSON_FORMS.PNC_FORM,"প্রসবোত্তর পরিচর্যা")
            .put( EVENT_TYPE.PNC_REGISTRATION,"প্রসবোত্তর পরিচর্যা")
            .put(EVENT_TYPE.WOMEN_PACKAGE,"নারী সেবা প্যাকেজ")
            .put(EVENT_TYPE.GIRL_PACKAGE, "কিশোরী সেবা প্যাকেজ")
            .put(EVENT_TYPE.NCD_PACKAGE, "ব্যাধি সেবা প্যাকেজ (এন সি ডি)")
            .put(EVENT_TYPE.IYCF_PACKAGE, "শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)")
            .put(EVENT_TYPE.ENC_REGISTRATION, "ENC")
            .put(EVENT_TYPE.HOME_VISIT_FAMILY, "খানা পরিদর্শন")
            .build();
    //needed for dashboard
    public static final Map<String,String> eventTypeMapping = ImmutableMap.<String,String> builder()
            .put(HnppConstants.EventType.FAMILY_REGISTRATION,"খানা নিবন্ধন")
            .put(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION,"সদস্য নিবন্ধন")
            .put(HnppConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,"সদস্য আপডেট")
            .put(HnppConstants.EventType.CHILD_REGISTRATION,"শিশু নিবন্ধন")
            .put(HnppConstants.EVENT_TYPE.MEMBER_REFERRAL,"রেফারেল")
            .put("Member referral","রেফারেল")
            .put(EVENT_TYPE.ELCO,"সক্ষম দম্পতি নিবন্ধন")
            .put(EVENT_TYPE.ANC_REGISTRATION,"গর্ভবতী রেজিস্ট্রেশন")
            .put(EVENT_TYPE.UPDATE_ANC_REGISTRATION,"গর্ভবতী রেজিস্ট্রেশন আপডেট")
            .put(EVENT_TYPE.WOMEN_PACKAGE,"নারী সেবা প্যাকেজ")
            .put(EVENT_TYPE.GIRL_PACKAGE, "কিশোরী সেবা প্যাকেজ")
            .put(EVENT_TYPE.NCD_PACKAGE, "ব্যাধি সেবা প্যাকেজ (এন সি ডি)")
            .put(EVENT_TYPE.IYCF_PACKAGE, "শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)")
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,"গর্ভবতী পরিচর্যা ভিজিট")
            .put(Constants.EVENT_TYPE.PNC_HOME_VISIT,"প্রসবোত্তর পরিচর্যা ভিজিট")
            .put(EVENT_TYPE.PNC_REGISTRATION,"প্রসবোত্তর পরিচর্যা")
            .put(EVENT_TYPE.ENC_REGISTRATION, "ENC")
            .put(EVENT_TYPE.HOME_VISIT_FAMILY, "খানা পরিদর্শন")
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
