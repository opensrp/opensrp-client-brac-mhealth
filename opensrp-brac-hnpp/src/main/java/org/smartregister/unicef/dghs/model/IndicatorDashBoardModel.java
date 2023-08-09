package org.smartregister.unicef.dghs.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.contract.DashBoardContract;
import org.smartregister.unicef.dghs.repository.IndicatorRepository;
import org.smartregister.unicef.dghs.utils.DashBoardData;
import org.smartregister.unicef.dghs.utils.HnppConstants;

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
        return getVisitTypeCount(context.getString(R.string.verified_with_biomatric),"is_verified","true",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIdentifiedBySimprints(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.searched_with_bio),"is_identified","true",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFamilyMethodKnown(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.family_planning_user),"familyplanning_method_known","yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNoFamilyMethodUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.out_of_plan),"familyplanning_method_known","no",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFillUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pill_user),"familyplanning_method","contraceptive_pill",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFillFromSS(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pill_cm_through_nurse),"distribution_location","brac_health_worker",ssName,fromMonth,toMonth);
    }
    public DashBoardData getFillFromOther(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pill_from_other_source),"distribution_other","other",ssName,fromMonth,toMonth);
    }
    public DashBoardData getCondomUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.cm_user),"familyplanning_method","condom",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIudUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.iud_user),"familyplanning_method","iud",ssName,fromMonth,toMonth);
    }
    public DashBoardData getInjectionUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.injection_user),"familyplanning_method","injection",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNorplantUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.implant_user),"familyplanning_method","norplant",ssName,fromMonth,toMonth);
    }
    public DashBoardData getVasectomyUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.vasectamy_user),"familyplanning_method","vasectomy",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTubeUser(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.tubactami_user),"familyplanning_method","ligation",ssName,fromMonth,toMonth);
    }
    //ss info dashboard
    public DashBoardData getHHVisitCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum(context.getString(R.string.hh_visit),"no_of_hh_visit",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPregnancyIdentity(String ssName, String fromMonth, String toMonth){
        return getAncRegistrationCount(context.getString(R.string.pregnancy_identification),ssName,fromMonth,toMonth);
    }
    public DashBoardData getAfter1hrBirthAdviceCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum(context.getString(R.string.advice_after_1_hour_of_birth),"one_hour_after_birth",ssName,fromMonth,toMonth);
    }
    public DashBoardData getServiceTakenMemberCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum(context.getString(R.string.num_of_service_receiver),"m",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIncomeFromMedicineCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum(context.getString(R.string.income_from_medicine),"income_from_medicine",ssName,fromMonth,toMonth);
    }
    public DashBoardData getGlassSellCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum(context.getString(R.string.income_from_glass),"g",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPresentEpiCount(String ssName, String fromMonth, String toMonth){
        return getVisitTypeSum(context.getString(R.string.present_in_epi_center),"no_of_epi_present",ssName,fromMonth,toMonth);
    }
    

    //work activity dashboard
    public DashBoardData getAnotherSource(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.anc_other),HnppConstants.INDICATOR.ANC_OTHER_SOURCE,"true",ssName,fromMonth,toMonth);
    }
    public DashBoardData get4PlusAnc(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.four_anc),"no_anc_at_pregnant","4",ssName,fromMonth,toMonth);
    }
    public DashBoardData getCigerDelivery(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pregnancy_outcome_cigar),"delivery_method_c_section","c_section",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNormalDelivery(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pregnancy_outcome_normal),"delivery_method_general","normal",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTTAncWomen(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.tt_vacc_mother),HnppConstants.INDICATOR.ANC_TT,"yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTTPNCWomen(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.tt_vacc_delivered_mother),HnppConstants.INDICATOR.OUTCOME_TT,"yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPncService48Hrs(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pnc_withing_48),"is_delay","false",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPnc1to2(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pnc_1_2),"number_of_pnc_1_2","2",ssName,fromMonth,toMonth);
    }
    public DashBoardData getPnc3to4(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pnc_3_3),"number_of_pnc_3","3",ssName,fromMonth,toMonth);
    }
    public DashBoardData getReferrelByPregnency(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.pregnancy_problem_reffer),"cause_of_referral_woman","pregnancy_problems",ssName,fromMonth,toMonth);
    }
    public DashBoardData getVaccineChild(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.child_vacc),"Vaccination",ssName,fromMonth,toMonth);
    }
    public DashBoardData getVitaminChild(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.child_with_vitamin),"Recurring Service",ssName,fromMonth,toMonth);
    }
    public DashBoardData getBcgChild(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.child_with_bcg),"bcg","",ssName,fromMonth,toMonth);
    }
    public DashBoardData getBrestFeedingByBirth(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.breast_feeding_child_after_1_houe_of_birth),"breastfeeding_time","1",ssName,fromMonth,toMonth);
    }
    public DashBoardData getOnlyBrestFeeding(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.breast_feeding_till_6_month),HnppConstants.INDICATOR.FEEDING_UPTO_6_MONTH,"true",ssName,fromMonth,toMonth);
    }
    public DashBoardData getChildSevenMonth(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.extra_food_child_7_months),"solid_food_month","7",ssName,fromMonth,toMonth);
    }
    public DashBoardData getTotalDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.member_dead_reg),"remove_reason","died",ssName,fromMonth,toMonth);
    }
    public DashBoardData getDeathBirth(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.newborn_dead),"preg_outcome","born_alive_died",ssName,fromMonth,toMonth);
    }
    public DashBoardData getMotherDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.mother_dead),"cause_of_death","c",ssName,fromMonth,toMonth);
    }
    public DashBoardData getChildDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.child_dead),"cause_of_death_child","c",ssName,fromMonth,toMonth);
    }
    public DashBoardData getOtherDeath(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.other_dead),"cause_of_death_other","c",ssName,fromMonth,toMonth);
    }
    public DashBoardData getEstimatedCoronaPatient(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.estimated_corona_petient),"is_affected_member","yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getCoronaPatient(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.corona_positive_petient),"corona_test_result","positive",ssName,fromMonth,toMonth);
    }
    public DashBoardData getIsolationPatient(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(context.getString(R.string.isolation_patient),"isolation","Yes",ssName,fromMonth,toMonth);
    }
    public DashBoardData getNoOfMemberVisited(String ssName, long fromMonth, long toMonth){
        String fromMonthStr= HnppConstants.getDateFormateFromLong(fromMonth);
        String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
        return getVisitTypeSum(context.getString(R.string.people_visit),"member_count",ssName,fromMonthStr,toMonthStr);
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
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
            build.append(MessageFormat.format(" where {0}.{1} is not null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.BLOCK_NAME,"'"+ssName+"'"));
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
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
            build.append(MessageFormat.format(" where {0}.{1} ={4} and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.BLOCK_NAME,"'"+ssName+"'","'1'"));
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
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
    cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
        ssCondition = " and "+HnppConstants.KEY.BLOCK_NAME +" = '"+ssName+"'";
        return ssCondition;
    }


    public String getVisitFilterCondition(String ssName, String month, String year, String mainCondition){
        StringBuilder build = new StringBuilder();

        build.append(mainCondition);
        if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.BLOCK_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.MONTH ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.YEAR  ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.MONTH,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", IndicatorRepository.YEAR ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.BLOCK_NAME,"'"+ssName+"'"));

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
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.BLOCK_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+month+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.BLOCK_NAME,"'"+ssName+"'"));

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