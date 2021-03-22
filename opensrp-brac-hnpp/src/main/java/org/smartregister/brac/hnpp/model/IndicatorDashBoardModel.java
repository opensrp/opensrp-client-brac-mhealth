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
        return getVisitTypeCount("স্বাস্থ্যসেবিকা থেকে পিল ব্যবহারকারী","distribution_location","brac_health_worker",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFillFromOther(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("অন্যান্য উৎস থেকে পিল ব্যবহারকারী","distribution_location","other",ssName,fromMonth,toMonth);
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

    //work activity dashboard
    public DashBoardData getAnotherSource(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("এ\u200C এন সি অন্যান্য",HnppConstants.INDICATOR.ANC_OTHER_SOURCE,"govt",ssName,fromMonth,toMonth);
    }
    public DashBoardData get4PlusAnc(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("4+ এ এন সি","anc_count","3",ssName,fromMonth,toMonth);
    }
    public DashBoardData getCigerDelivery(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("প্রসবের ফলাফল(সিজার)","delivery_method_c_section","c_section",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNormalDelivery(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("প্রসবের ফলাফল(স্বাভাবিক)","delivery_method_general","normal",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTTWomen(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("টিটি টিকা প্রাপ্ত প্রসূতি মায়ের সংখ্যা",HnppConstants.INDICATOR.ANC_TT,"yes",ssName,fromMonth,toMonth);
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
        return getVisitTypeCount("মোট মৃত্যু","remove_reason","died",ssName,fromMonth,toMonth);
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

//    public DashBoardData getVisitTypeCount(String title,String indicatorKey,String indicatorValue, String ssName, String month, String year){
//        DashBoardData dashBoardData1 = new DashBoardData();
//        String mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"' COLLATE NOCASE";
//        if(indicatorKey.equalsIgnoreCase(HnppConstants.INDICATOR.ANC_OTHER_SOURCE)){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='Govt' or "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='other'";
//        }
//        else if(indicatorKey.equalsIgnoreCase("anc_count")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" >="+indicatorValue;
//        }
//        else if(indicatorKey.equalsIgnoreCase("delivery_method_general")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"' or "+IndicatorRepository.INDICATOR_NAME+" ='delivery_method_c_section' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"'";
//        }
//        else if(indicatorKey.equalsIgnoreCase("number_of_pnc_1_2")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='number_of_pnc' and "+IndicatorRepository.INDICATOR_VALUE+" <="+indicatorValue;
//        }
//        else if(indicatorKey.equalsIgnoreCase("number_of_pnc_3")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='number_of_pnc' and "+IndicatorRepository.INDICATOR_VALUE+" >="+indicatorValue;
//        }
//        else if(indicatorKey.equalsIgnoreCase("breastfeeding_time")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" <="+indicatorValue;
//        }
//        else if(indicatorKey.equalsIgnoreCase("solid_food_month")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ="+indicatorValue;
//        }
//        else if(indicatorKey.equalsIgnoreCase("cause_of_death")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='preterm_death' or "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='childbirth_death' or "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='postnatal_death'";
//        }
//        else if(indicatorKey.equalsIgnoreCase("cause_of_death_child")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" ='infant_death' ";
//        }
//        else if(indicatorKey.equalsIgnoreCase("cause_of_death_other")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='preterm_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='childbirth_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='postnatal_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='infant_death'";
//
//        }else if(indicatorKey.equalsIgnoreCase("bcg")){
//            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='bcg'";
//
//        }
//        String query;
//        if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
//            query = MessageFormat.format("select count(*) as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE, mainCondition);
//        }else{
//            query = MessageFormat.format("select count(*) as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE, getVisitFilterCondition(ssName,month,year,mainCondition));
//
//        }
//        Log.v("WORK_SUMMERY","visit_type:"+query);
//
//        Cursor cursor = null;
//        // try {
//        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
//        if(cursor !=null && cursor.getCount() > 0){
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//
//                dashBoardData1.setCount(cursor.getInt(0));
//                dashBoardData1.setTitle(title);
//                dashBoardData1.setImageSource(R.drawable.rowavatar_member);
//
//                cursor.moveToNext();
//            }
//            cursor.close();
//
//        }
//
//
//        return dashBoardData1;
//    }
    public DashBoardData getVisitTypeCount(String title,String indicatorKey,String indicatorValue, String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"' COLLATE NOCASE";
        if(indicatorKey.equalsIgnoreCase(HnppConstants.INDICATOR.ANC_OTHER_SOURCE)){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='Govt' or "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='other'";
        }
        else if(indicatorKey.equalsIgnoreCase("anc_count")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" >="+indicatorValue;
        }
        else if(indicatorKey.equalsIgnoreCase("delivery_method_general")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"' or "+IndicatorRepository.INDICATOR_NAME+" ='delivery_method_c_section' and "+IndicatorRepository.INDICATOR_VALUE+" ='"+indicatorValue+"'";
        }
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
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='preterm_death' or "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='childbirth_death' or "+IndicatorRepository.INDICATOR_NAME+" ='"+indicatorKey+"' and "+IndicatorRepository.INDICATOR_VALUE+" ='postnatal_death'";
        }
        else if(indicatorKey.equalsIgnoreCase("cause_of_death_child")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" ='infant_death' ";
        }
        else if(indicatorKey.equalsIgnoreCase("cause_of_death_other")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='preterm_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='childbirth_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='postnatal_death' and "+IndicatorRepository.INDICATOR_NAME+" ='cause_of_death' and "+IndicatorRepository.INDICATOR_VALUE+" !='infant_death'";

        }else if(indicatorKey.equalsIgnoreCase("bcg")){
            mainCondition = " where "+IndicatorRepository.INDICATOR_NAME+" ='bcg'";

        }
        String query = null;
       /* if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
            query = MessageFormat.format("select count(*) as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE, mainCondition);
        }else{
            query = MessageFormat.format("select count(*) as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE, getVisitFilterCondition(ssName,month,year,mainCondition));

        }*/

        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1}", IndicatorRepository.INDICATOR_TABLE,mainCondition);
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT count(*) as count,strftime(''%s'', strftime(''%s'', date), ''unixepoch'') as longtime from t1 {1} {2}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getBetweenCondition(fromMonth,toMonth,"longtime"));
            }else{
                query = MessageFormat.format("with t1 as (SELECT year||''-''||printf(''%02d'',month)||''-''||printf(''%02d'',day) as date,ss_name,indicator_name,indicator_value from {0})SELECT count(*) as count,strftime(''%s'', strftime(''%s'', date), ''unixepoch'') as longtime from t1 {1} {2} {3}", IndicatorRepository.INDICATOR_TABLE,mainCondition,getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,"longtime"));

            }
        }
        Log.v("WORK_SUMMERY","visit_type:"+query);

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
