package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.repository.IndicatorRepository;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

import java.text.MessageFormat;

public class IndicatorDashBoardModel implements DashBoardContract.Model {

    private Context context;

    public IndicatorDashBoardModel(Context context){
        this.context = context;
    }



    //for jonosonkha sarsonkhep compilation sheet
    public DashBoardData getVerifiedBySimprints(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("বায়োমেট্রিক দ্বারা যাচাইকৃত","is_verified","true",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIdentifiedBySimprints(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("বায়োমেট্রিক দ্বারা খোজকৃত","is_identified","true",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFamilyMethodKnown(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("পরিবার পরিকল্পনা পদ্ধতি ব্যবহারকারী","familyplanning_method_known","yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNoFamilyMethodUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("পদ্ধতি ব্যবহারের বাইরে","familyplanning_method_known","no",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFillUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("পিল ব্যবহারকারী","familyplanning_method","contraceptive_pill",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFillFromSS(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("স্বাস্থ্যসেবিকা থেকে পিল,কনডম ব্যবহারকারী","distribution_location","brac_health_worker",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFillFromOther(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("অন্যান্য উৎস থেকে পিল,কনডম ব্যবহারকারী","distribution_other","other",ssName,fromMonth,toMonth);
    }
    public DashBoardData getCondomUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("কনডম ব্যবহারকারী","familyplanning_method","condom",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIudUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("আই.ইউ.ডি. ব্যবহারকারী","familyplanning_method","iud",ssName,fromMonth,toMonth);
    }
    public DashBoardData getInjectionUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("ইনজেকশন ব্যবহারকারী","familyplanning_method","injection",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNorplantUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("ইমপ্ল্যান্ট ব্যবহারকারী","familyplanning_method","norplant",ssName,fromMonth,toMonth);
    }
    public DashBoardData getVasectomyUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("ভ্যাসেকটমি ব্যবহারকারী","familyplanning_method","vasectomy",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTubeUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("টিউব্যাকটোমি ব্যবহারকারী","familyplanning_method","ligation",ssName,fromMonth,toMonth);
    }
    //ss info dashboard
    public DashBoardData getHHVisitCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum("খানা পরিদর্শন","no_of_hh_visit",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPregnancyIdentity(String ssName, String fromMonth, String toMonth){
        return getAncRegistrationCount("গর্ভবতী সনাক্ত ",ssName,fromMonth,toMonth);
    }
    public DashBoardData getAfter1hrBirthAdviceCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum("জন্মের ১ ঘন্টার মধ্যে মায়ের বুকে লাগানোর পরামর্শ","one_hour_after_birth",ssName,fromMonth,toMonth);
    }
    public DashBoardData getServiceTakenMemberCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum("সেবাপ্রাপ্ত মানুষের সংখ্যা","m",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIncomeFromMedicineCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum("ওষুধ থেকে আয়","income_from_medicine",ssName,fromMonth,toMonth);
    }
    public DashBoardData getGlassSellCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum("চশমা বিক্রির সংখ্যা","g",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPresentEpiCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum("ই.পি.এই সেন্টার এ উপস্থিত","no_of_epi_present",ssName,fromMonth,toMonth);
    }


    //work activity dashboard
    public DashBoardData getAnotherSource(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("এ\u200C এন সি অন্যান্য",HnppConstants.INDICATOR.ANC_OTHER_SOURCE,"true",ssName,fromMonth,toMonth);
    }
    public DashBoardData get4PlusAnc(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("4+ এ এন সি","no_anc_at_pregnant","4",ssName,fromMonth,toMonth);
    }
    public DashBoardData getGlassSell(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("চশমা বিতরণ","glasses_sell","",ssName,fromMonth,toMonth);
    }
    public DashBoardData getCigerDelivery(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("প্রসবের ফলাফল(সিজার)","delivery_method_c_section","c_section",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNormalDelivery(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("প্রসবের ফলাফল(স্বাভাবিক)","delivery_method_general","normal",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTTAncWomen(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("টিটি টিকা প্রাপ্ত গর্ভবতী মায়ের সংখ্যা",HnppConstants.INDICATOR.ANC_TT,"yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTTPNCWomen(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("টিটি টিকা প্রাপ্ত প্রসূতি মায়ের সংখ্যা",HnppConstants.INDICATOR.OUTCOME_TT,"yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPncService48Hrs(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("৪৮ ঘণ্টার মধ্যে পি এন সি সেবা","is_delay","false",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPnc1to2(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("1-2 পি এন সি","number_of_pnc_1_2","2",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPnc3to4(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("3-3+পি এন সি","number_of_pnc_3","3",ssName,fromMonth,toMonth);
    }
    public DashBoardData getReferrelByPregnency(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("গর্ভ সংক্রান্ত সমস্যার জন্য রেফার","cause_of_referral_woman","pregnancy_problems",ssName,fromMonth,toMonth);
    }
    public DashBoardData getVaccineChild(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("শিশুর টিকা","Vaccination",ssName,fromMonth,toMonth);
    }
    public DashBoardData getVitaminChild(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("ভিটামিন প্রাপ্ত শিশুর সংখ্যা","Recurring Service",ssName,fromMonth,toMonth);
    }
    public DashBoardData getBcgChild(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("বিসিজি টিকা প্রাপ্ত শিশুর সংখ্যা","bcg","",ssName,fromMonth,toMonth);
    }
    public DashBoardData getBrestFeedingByBirth(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("জন্মের এক ঘণ্টার মধ্যে বুকের দুধ খাওয়া শিশুর সংখ্যা","breastfeeding_time","1",ssName,fromMonth,toMonth);
    }
    public DashBoardData getOnlyBrestFeeding(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("ছয় মাস পর্যন্ত শুধুমাত্র বুকের দুধ খাওয়া শিশুর সংখ্যা",HnppConstants.INDICATOR.FEEDING_UPTO_6_MONTH,"true",ssName,fromMonth,toMonth);
    }
    public DashBoardData getChildSevenMonth(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("সাত মাসে বাড়তি খাবার খাওয়া শিশুর সংখ্যা","solid_food_month","7",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTotalDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("সদস্য মৃত্যু নিবন্ধন","remove_reason","died",ssName,fromMonth,toMonth);
    }
    public DashBoardData getDeathBirth(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("নবজাতক মৃত্যু","preg_outcome","born_alive_died",ssName,fromMonth,toMonth);
    }
    public DashBoardData getMotherDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("মাতৃমৃত্যু","cause_of_death","c",ssName,fromMonth,toMonth);
    }
    public DashBoardData getChildDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("শিশুর মৃত্যু","cause_of_death_child","c",ssName,fromMonth,toMonth);
    }
    public DashBoardData getOtherDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("অন্যান্য মৃত্যু","cause_of_death_other","c",ssName,fromMonth,toMonth);
    }
    public DashBoardData getEstimatedCoronaPatient(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("সাম্ভাব্য করোনা রোগীর সংখ্যা","is_affected_member","yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getCoronaPatient(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("করোনা পজিটিভ রোগীর সংখ্যা","corona_test_result","positive",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIsolationPatient(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("করেন্টাইন পরিবারের সংখ্যা","isolation","Yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNoOfMemberVisited(String ssName, long fromMonth, long toMonth){
        String fromMonthStr= HnppConstants.getDateFormateFromLong(fromMonth);
        String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
        return getVisitTypeSum("জনসংখ্যা পরিদর্শন","member_count",ssName,fromMonthStr,toMonthStr);
    }
    public DashBoardData getRemoveMemberCount(String title,String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();

        String query;
     /*   if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
            query = "select count(*) as count from ec_family_member where date_removed is not null";
        }else {
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_family_member", getRemoveFilterCondition(ssName,month,year));

        }*/
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_family_member where date_removed is not null";
            }else{
                query = MessageFormat.format(
                        "select count(*) as count from {0} inner join {1} on {2}.{3} = {4}.{5} where {6}.{7} is not null {8}",
                        "ec_family_member",
                        CoreConstants.TABLE_NAME.FAMILY,CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getSSCondition(ssName));
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_family_member", getRemoveFilterCondition(ssName,fromMonth,toMonth));
        }
        Log.v("WORK_QUERY","member:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setTitle(title);

                dashBoardData1.setImageSource(R.drawable.rowavatar_member);
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }

    public String getRemoveFilterCondition(String ssName, long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));
        /*if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" where {0}.{1} is not null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is not null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is not null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%Y', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" where {0}.{1} is not null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is not null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%Y', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} is not null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));

        }*/
        if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} is not null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is not null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth)));

        }
        else{
            build.append(MessageFormat.format(" where {0}.{1} is not null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth)));
        }
        return build.toString();
    }
    public String getBetweenMemberCondition(long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        if(fromMonth == -1){
            build.append(MessageFormat.format(" and {0} = {1} ",CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,"'"+Long.toString(toMonth)+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,Long.toString(fromMonth),Long.toString(toMonth)));
        }
        return build.toString();
    }

    public DashBoardData getRemoveHHCount(String title, String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();

        String query = null, compareDate = DBConstants.KEY.LAST_INTERACTED_WITH;
        /*if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month) & TextUtils.isEmpty(year) ){
            query = "select count(*) as count from ec_family where date_removed is not null ";
        }else if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            query = "select count(*) as count from ec_family where ss_name = '"+ssName+"' and strftime('%m', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+month+"' and strftime('%Y', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+year+"' and date_removed is not null ";

        }
        else if(!TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family where ss_name = '"+ssName+"' and date_removed is not null ";
        }else {
            query = "select count(*) as count from ec_family where strftime('%m', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+month+"' and strftime('%Y', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+year+"' and date_removed is not null ";
        }*/

        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is not null", "ec_family");
            }else{
                query = MessageFormat.format("select count(*) as count from {0}  where date_removed is not null {1}", "ec_family",getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is not null {1}", "ec_family",getBetweenCondition(fromMonth,toMonth,compareDate));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is not null {1} {2}", "ec_family",getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,compareDate));

            }
        }
        Log.v("WORK_QUERY","member:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setTitle(title);
                dashBoardData1.setImageSource(R.drawable.ic_home);

                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }

    public DashBoardData getMigrateMemberCount(String title, String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();
        String query;
       /* if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
            query = "select count(*) as count from ec_family_member where date_removed ='1'";
        }else {
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_family_member", getMigrateFilterCondition(ssName,month,year));

        }*/
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_family_member where date_removed ='1'";
            }else{
                query = MessageFormat.format(
                        "select count(*) as count from {0} inner join {1} on {2}.{3} = {4}.{5} where {6}.{7} = {9} {8}",
                        "ec_family_member",
                        CoreConstants.TABLE_NAME.FAMILY,CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getSSCondition(ssName),"'1'");
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_family_member", getMigrateFilterCondition(ssName,fromMonth,toMonth));
        }
        Log.v("WORK_QUERY","member:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setTitle(title);
                dashBoardData1.setImageSource(R.drawable.rowavatar_member);
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public String getMigrateFilterCondition(String ssName, long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));
  /*      if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" where {0}.{1} ={4} and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'",'1'));
            build.append(MessageFormat.format(" and {0}.{1} ={4} and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'","'1'"));
            build.append(MessageFormat.format(" and {0}.{1} ={4} and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%Y', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+year+"'","'1'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" where {0}.{1} ={4} and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'","'1'"));
            build.append(MessageFormat.format(" and {0}.{1} ={4} and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%Y', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+year+"'","'1'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} ={4} and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'","'1'"));

        }*/
        if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} ={4} and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'","'1'"));
            build.append(MessageFormat.format(" and {0}.{1} ={3} {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth),"'1'"));

        }
        else{
            build.append(MessageFormat.format(" where {0}.{1} ={3} {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth),"'1'"));
        }

        return build.toString();
    }

    public DashBoardData getMigratedHHCount(String title, String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();

        String query = null,compareDate = DBConstants.KEY.LAST_INTERACTED_WITH;
       /* if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month) & TextUtils.isEmpty(year) ){
            query = "select count(*) as count from ec_family where date_removed ='1' ";
        }else if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            query = "select count(*) as count from ec_family where ss_name = '"+ssName+"' and strftime('%m', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+month+"' and strftime('%Y', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+year+"' and date_removed ='1' ";

        }
        else if(!TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family where ss_name = '"+ssName+"' and date_removed ='1' ";
        }else {
            query = "select count(*) as count from ec_family where strftime('%m', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+month+"' and strftime('%Y', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+year+"' and date_removed ='1' ";
        }*/
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed ='1'", "ec_family");
            }else{
                query = MessageFormat.format("select count(*) as count from {0}  where date_removed ='1' {1}", "ec_family",getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed ={2} {1}", "ec_family",getBetweenCondition(fromMonth,toMonth,compareDate),"'1'");
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where date_removed ={3} {1} {2}", "ec_family",getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,compareDate),"'1'");

            }
        }
        Log.v("WORK_QUERY","member:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setTitle(title);
                dashBoardData1.setImageSource(R.drawable.ic_home);
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public DashBoardData getAncRegistrationCount(String title, String ssName, String fromMonth, String toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition= " where visit_type ='"+HnppConstants.EVENT_TYPE.ANC_REGISTRATION+ "'";
        String query = null, compareDate = "visit_date";
        long fromMonthLong = HnppConstants.getLongFromDateFormat(fromMonth);
        long toMonthLong = HnppConstants.getLongFromDateFormat(toMonth);
        if(TextUtils.isEmpty(fromMonth)&& TextUtils.isEmpty(toMonth)){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", mainCondition);
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition,getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition, getBetweenCondition(fromMonthLong,toMonthLong,compareDate));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2} {3}", "ec_visit_log", mainCondition,getSSCondition(ssName),getBetweenCondition(fromMonthLong,toMonthLong,compareDate));

            }
        }
        Log.v("SS_DASHBOARD","query:"+query+":toMonth:"+toMonth+":fromMonth:"+fromMonth);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EVENT_TYPE.ANC_REGISTRATION);
                dashBoardData1.setTitle(title);
                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }

public DashBoardData getVisitTypeSum(String title,String indicatorKey,String ssName, String fromMonth, String toMonth){
    DashBoardData dashBoardData1 = new DashBoardData();
    String mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"'";
    if(indicatorKey.equalsIgnoreCase("m")){
        mainCondition = " where (indicator_name = 'male_patient' or indicator_name = 'female_patient')";
    }else if(indicatorKey.equalsIgnoreCase("g")){
        mainCondition = " where (indicator_name = 'glass_metal_count' or indicator_name = 'glass_plastic_count' or indicator_name = 'glass_sunglass_count')";
    }
    String query,returnColumn = "sum("+IndicatorRepository.INDICATOR_VALUE+")";
    if(TextUtils.isEmpty( fromMonth)&& TextUtils.isEmpty(toMonth)){
        if(TextUtils.isEmpty(ssName)){
            query = MessageFormat.format("select "+returnColumn+" as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE,mainCondition);
        }else{
            query = MessageFormat.format("select "+returnColumn+" as count from {0} {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName));

        }
    }
    else{
        if(TextUtils.isEmpty(ssName)){
            query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT "+returnColumn+" as count from t1 {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getBetweenCondition(fromMonth,toMonth,"date"));
        }else{
            query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT "+returnColumn+" as count from t1 {1} {2} {3}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,"date"));

        }
    }
    Log.v("getVisitTypeSum","query:"+query);

    Cursor cursor = null;
    // try {
    cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
    if(cursor !=null && cursor.getCount() > 0){
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            dashBoardData1.setCount(cursor.getInt(cursor.getColumnIndex("count")));
            dashBoardData1.setTitle(title);
            dashBoardData1.setImageSource(R.drawable.rowavatar_member);

            cursor.moveToNext();
        }
        cursor.close();

    }


    return dashBoardData1;
}

    public DashBoardData getVisitTypeCount(String title,String indicatorKey,String indicatorValue, String ssName,long fromMonth, long toMonth){
        String fromMonthStr = HnppConstants.getDateFormateFromLong(fromMonth);
        String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"' COLLATE NOCASE";
        if(TextUtils.isEmpty(indicatorValue)){
            mainCondition =" where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"'";
        }
        String returnColumn = "count(*)";
//        if(indicatorKey.equalsIgnoreCase(HnppConstants.INDICATOR.ANC_OTHER_SOURCE)){
//            mainCondition = " where (("+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='Govt') or ("+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='other'))";
//        }
        if(indicatorKey.equalsIgnoreCase("distribution_other")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='distribution_location' and "+IndicatorRepository.INDICATOR_VALUE+" !='brac_health_worker'";
        }
        else if(indicatorKey.equalsIgnoreCase("no_anc_at_pregnant")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" >="+indicatorValue;
        }
//        else if(indicatorKey.equalsIgnoreCase("delivery_method_general")){
//            mainCondition = " where (("+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"') or ("+IndicatorRepository.INDICATOR_NAME+" ='delivery_method_c_section' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"'))";
//        }
        else if(indicatorKey.equalsIgnoreCase("number_of_pnc_1_2")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='number_of_pnc' and "+IndicatorRepository.INDICATOR_VALUE+" <="+indicatorValue;
        }
        else if(indicatorKey.equalsIgnoreCase("number_of_pnc_3")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='number_of_pnc' and "+IndicatorRepository.INDICATOR_VALUE+" >="+indicatorValue;
        }
        else if(indicatorKey.equalsIgnoreCase("breastfeeding_time")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" <="+indicatorValue;
        }
        else if(indicatorKey.equalsIgnoreCase("solid_food_month")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ="+indicatorValue;
        }
        else if(indicatorKey.equalsIgnoreCase("cause_of_death")){
            mainCondition = " where (("+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='preterm_death') or ("+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='childbirth_death') or ("+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='postnatal_death'))";
        }
        else if(indicatorKey.equalsIgnoreCase("cause_of_death_child")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" ='infant_death' ";
        }
        else if(indicatorKey.equalsIgnoreCase("cause_of_death_other")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='preterm_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='childbirth_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='postnatal_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='infant_death'";

        }else if(indicatorKey.equalsIgnoreCase("bcg")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='bcg'";

        }else if(indicatorKey.equalsIgnoreCase("member_count")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='member_count'";
            returnColumn = "sum("+IndicatorRepository.INDICATOR_VALUE+")";
        }else if(indicatorKey.equalsIgnoreCase("preg_outcome")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='preg_outcome' and instr("+IndicatorRepository.INDICATOR_VALUE+", 'born_alive_died') >0 ";
        }
        String query = null;

//        if(fromMonth == -1 && toMonth == -1){
//            if(TextUtils.isEmpty(ssName)){
//                query = MessageFormat.format("select "+returnColumn+" as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE,mainCondition);
//            }else{
//                query = MessageFormat.format("select "+returnColumn+" as count from {0} {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName));
//
//            }
//        }
//        else{
//            if(TextUtils.isEmpty(ssName)){
//                query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT count(*) as count,strftime(''%s'', strftime(''%s'', date), ''unixepoch'') as longtime from t1 {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getBetweenCondition(fromMonth,toMonth,"longtime"));
//            }else{
//                query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT count(*) as count,strftime(''%s'', strftime(''%s'', date), ''unixepoch'') as longtime from t1 {1} {2} {3}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,"longtime"));
//
//            }
//        }
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select "+returnColumn+" as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE,mainCondition);
            }else{
                query = MessageFormat.format("select "+returnColumn+" as count from {0} {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT "+returnColumn+" as count from t1 {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getBetweenCondition(fromMonthStr,toMonthStr,"date"));
            }else{
                query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT "+returnColumn+" as count from t1 {1} {2} {3}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName),getBetweenCondition(fromMonthStr,toMonthStr,"date"));

            }
        }
        if(indicatorKey.equalsIgnoreCase("delivery_method_general")){
            Log.v("DASHBOARD_QUERY","query:"+query);
        }

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(cursor.getColumnIndex("count")));
                dashBoardData1.setTitle(title);
                if(indicatorKey.equalsIgnoreCase("glasses_sell")){
                    dashBoardData1.setImageSource(R.drawable.ic_eye);
                }else{
                    dashBoardData1.setImageSource(R.drawable.rowavatar_member);
                }


                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public String getBetweenCondition(long fromMonth, long toMonth, String compareDate){
        StringBuilder build = new StringBuilder();
        if(fromMonth == -1){
            build.append(MessageFormat.format(" and {0} = {1} ",compareDate,"'"+Long.toString(toMonth)+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",compareDate,Long.toString(fromMonth),Long.toString(toMonth)));
        }
        return build.toString();
    }
    public String getBetweenCondition(String fromMonth, String toMonth, String compareDate){
        StringBuilder build = new StringBuilder();
        if(TextUtils.isEmpty(fromMonth)){
            build.append(MessageFormat.format(" and {0} = {1} ",compareDate,"'"+toMonth+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",compareDate,"'"+fromMonth+"'","'"+toMonth+"'"));
        }
        return build.toString();
    }

    public String getSSCondition(String ssName){
        String ssCondition;
        ssCondition = " and "+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        return ssCondition;
    }


    public String getVisitFilterCondition(String ssName, String month, String year, String mainCondition){
        StringBuilder build = new StringBuilder();

        build.append(mainCondition);
        if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.MONTH ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.YEAR  ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.MONTH,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.YEAR ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));

        }

        return build.toString();
    }
    public DashBoardData getVisitTypeCount(String title, String visitType, String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition= " where visit_type ='"+visitType+"'";

        String query;
      /*  if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", mainCondition);
        }else{
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", getVisitLogFilterCondition(ssName,month,year,mainCondition));

        }*/
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", mainCondition);
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition,getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition, getBetweenCondition(fromMonth,toMonth,"visit_date"));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition,getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,"visit_date"));

            }
        }
        Log.v("WORK_SUMMERY","visit_type:"+query);

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(visitType);
                dashBoardData1.setTitle(title);

                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }

    public String getVisitLogFilterCondition(String ssName, String month, String year, String mainCondition){
        StringBuilder build = new StringBuilder();
//        if(!TextUtils.isEmpty(ssName)){
//            build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER));
//            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
//                    "visits", DBConstants.KEY.BASE_ENTITY_ID));
//            build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
//            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
//                    CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));
//        }

        build.append(mainCondition);
        if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));

        }

        return build.toString();
    }

    @Override
    public DashBoardContract.Model getDashBoardModel() {
        return this;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
