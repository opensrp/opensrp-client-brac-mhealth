package org.smartregister.unicef.mis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.rule.HomeAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.dao.AbstractDao;
import org.smartregister.unicef.mis.model.ForumDetails;
import org.smartregister.unicef.mis.model.TikaInfoModel;
import org.smartregister.unicef.mis.model.VaacineInfo;
import org.smartregister.unicef.mis.model.VisitInfo;
import org.smartregister.unicef.mis.repository.StockRepository;

import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class HnppDBUtils {
    private static final int STOCK_END_THRESHOLD = 2;
    public static ChildVisit getChildVisitStatus(Context context, String yearOfBirth, long lastVisitDate, long visitNotDate, long dateCreated) {
        HomeAlertRule homeAlertRule = new HomeAlertRule(context, yearOfBirth, lastVisitDate, visitNotDate, dateCreated);
        HnppApplication.getInstance().getRulesEngineHelper().getButtonAlertStatus(homeAlertRule, CoreConstants.RULE_FILE.HOME_VISIT);
        return getChildVisitStatus(homeAlertRule, lastVisitDate);
    }
    public static ChildVisit getChildVisitStatus(HomeAlertRule homeAlertRule, long lastVisitDate) {
        ChildVisit childVisit = new ChildVisit();
        childVisit.setVisitStatus(homeAlertRule.buttonStatus);
        childVisit.setNoOfMonthDue(homeAlertRule.noOfMonthDue);
        childVisit.setLastVisitDays(homeAlertRule.noOfDayDue);
        childVisit.setLastVisitMonthName(homeAlertRule.visitMonthName);
        childVisit.setLastVisitTime(lastVisitDate);
        return childVisit;
    }
    public static List<Map<String, String>> readData(String query, String[] selectionArgs) {
        List<Map<String, String>> list = new ArrayList<>();
        Cursor cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, selectionArgs);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Map<String, String> res = new HashMap<>();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                res.put(cursor.getColumnName(i), getCursorValue(cursor, i));
            }
            list.add(res);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }
    @Nullable
    protected static String getCursorValue(Cursor c, int column_index) {
        return c.getType(column_index) == Cursor.FIELD_TYPE_NULL ? null : c.getString(column_index);
    }

    @Nullable
    protected static String getCursorValue(Cursor c, String column_name) {
        return c.getType(c.getColumnIndex(column_name)) == Cursor.FIELD_TYPE_NULL ? null : c.getString(c.getColumnIndex(column_name));
    }

    @Nullable
    protected static Long getCursorLongValue(Cursor c, String column_name) {
        return c.getType(c.getColumnIndex(column_name)) == Cursor.FIELD_TYPE_NULL ? null : c.getLong(c.getColumnIndex(column_name));
    }
    public static String[] getWeightFromBaseEntityId(String baseEntityId){
        String query = "select weight,weight_date from ec_family_member where base_entity_id = '"+baseEntityId+"'";
        Cursor cursor = null;
        String[] weight = new String[2];

        weight[0] = "0";
        weight[1] = "0";

        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                weight[0] = cursor.getString(0);
                if(weight[0] == null){
                    weight[0] = "0";
                }
                weight[1] = cursor.getLong(1)+"";
            }

            return weight;
        } catch (Exception e) {
            Timber.e(e);

        }
        finally {
            if(cursor !=null) cursor.close();
        }
        return weight;
    }
    public static void updateMemberWeight(String base_entity_id, String weight, long weightDate){
        Log.v("WEIGHT_UPDATE","updateMemberWeight>>>weight:"+weight+":weightDate:"+weightDate);
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_family_member set weight = '"+weight+"' , weight_date = '"+weightDate+"' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static String getFirstName(String familyBaseEntityId){
        String query = "select first_name from ec_family  where base_entity_id = '"+familyBaseEntityId+"'";
        Cursor cursor = null;
        String birthWeight="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                birthWeight = cursor.getString(0);
            }

        } catch (Exception e) {
            Timber.e(e);

        }
        finally {
            if(cursor !=null)cursor.close();
        }
        return birthWeight;
    }
    public static ArrayList<VaacineInfo> getVaccineInfo(String baseEntityId){
        String query = "select name,date,is_invalid from vaccines where base_entity_id ='"+baseEntityId+"' ";
        Cursor cursor = null;
        ArrayList<VaacineInfo> vaacineInfos = new ArrayList<>();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    VaacineInfo vaacineInfo = new VaacineInfo();
                    String name = cursor.getString(0);
                    long date = cursor.getLong(1);
                    int invalid = cursor.getInt(2);
                    Date regdate = new Date(date);
                    String vaccineDate = AbstractDao.getDobDateFormat().format(regdate);
                    vaacineInfo.vaccineName = name;
                    vaacineInfo.vaccineDate = vaccineDate;
                    vaacineInfo.invalid = invalid;
                    vaacineInfos.add(vaacineInfo);
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){

        }
        finally {
            if(cursor!=null) cursor.close();
        }
        return vaacineInfos;
    }
    public static TikaInfoModel getTikaDetailsForGuestProfike(String baseEntityId){
        String query = "Select * FROM ec_guest_member WHERE  date_removed is null and base_entity_id ='"+baseEntityId+"'";

        Cursor cursor = null;
        TikaInfoModel infoModel = new TikaInfoModel();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                infoModel.baseEntityId = (cursor.getString(cursor.getColumnIndex("base_entity_id")));
                infoModel.name = (cursor.getString(cursor.getColumnIndex("first_name")))+" "+(cursor.getString(cursor.getColumnIndex("last_name")));
                infoModel.motherName = (cursor.getString(cursor.getColumnIndex("mother_name_english")));
                infoModel.fatherName = (cursor.getString(cursor.getColumnIndex("father_name_english")));
                String dob = (cursor.getString(cursor.getColumnIndex("dob")));
                //2023-03-11T06:00:00.000+06:00
                infoModel.dob = dob.substring(0,dob.indexOf("T"));
                infoModel.birthYear = dob.substring(0,4);
                infoModel.birthMonth = dob.substring(5,7);
                infoModel.birthDay = dob.substring(8,10);
                infoModel.brid = (cursor.getString(cursor.getColumnIndex("birth_id")));
                infoModel.registrationNo = (cursor.getString(cursor.getColumnIndex("shr_id")));
                if(TextUtils.isEmpty(infoModel.registrationNo)){
                    infoModel.registrationNo = (cursor.getString(cursor.getColumnIndex("unique_id")));
                }
                long lastInteractedDate = cursor.getLong(cursor.getColumnIndex("last_interacted_with"));
                Date regdate = new Date(lastInteractedDate);
                String registrationDate = AbstractDao.getDobDateFormat().format(regdate);
                infoModel.registrationDate = registrationDate;
                infoModel.division = (cursor.getString(cursor.getColumnIndex("division_per")));
                infoModel.district = (cursor.getString(cursor.getColumnIndex("district_per")));
                infoModel.divisionId = (cursor.getString(cursor.getColumnIndex("division_id")));
                infoModel.districtId = (cursor.getString(cursor.getColumnIndex("district_id")));
                infoModel.upazilla = (cursor.getString(cursor.getColumnIndex("upazila_per")));
                infoModel.genderEnglish = (cursor.getString(cursor.getColumnIndex("gender")));
                infoModel.gender = HnppConstants.getGender(infoModel.genderEnglish);

            }

        } catch (Exception e) {
            Timber.e(e);
        }
        finally {
            if(cursor !=null) cursor.close();
        }
        return infoModel;

    }
//    public static TikaInfoModel getTikaDetails(String baseEntityId){
//        String query = "select ec_family_member.base_entity_id,ec_family_member.first_name,ec_family_member.last_name, ec_family_member.mother_name_english,ec_family_member.father_name_english, "+
//                " ec_family_member.dob,ec_family_member.gender, ec_family_member.birth_id,ec_family_member.shr_id,ec_family_member.unique_id,ec_family_member.last_interacted_with,ec_family_member.camp_type,"+
//                " ec_family.block_name,ec_family.village,ec_family.holding_no,ec_family.ward_name,ec_family.union_zone from ec_family_member "+
//                " inner join ec_family on ec_family.id  = ec_family_member.relational_id " +
//                " where ec_family_member.base_entity_id ='"+baseEntityId+"' ";
//        Cursor cursor = null;
//        TikaInfoModel infoModel = null;
//        try {
//            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
//            if(cursor !=null && cursor.getCount() >0){
//                cursor.moveToFirst();
//                infoModel = new TikaInfoModel();
//                infoModel.baseEntityId = (cursor.getString(cursor.getColumnIndex("base_entity_id")));
//                infoModel.name = (cursor.getString(cursor.getColumnIndex("first_name")))+" "+(cursor.getString(cursor.getColumnIndex("last_name")));
//                infoModel.motherName = (cursor.getString(cursor.getColumnIndex("mother_name_english")));
//                infoModel.fatherName = (cursor.getString(cursor.getColumnIndex("father_name_english")));
//                String dob = (cursor.getString(cursor.getColumnIndex("dob")));
//                //2023-03-11T06:00:00.000+06:00
//                infoModel.dob = dob.substring(0,dob.indexOf("T"));
//                infoModel.birthYear = dob.substring(0,4);
//                infoModel.birthMonth = dob.substring(5,7);
//                infoModel.birthDay = dob.substring(8,10);
//                infoModel.brid = (cursor.getString(cursor.getColumnIndex("birth_id")));
//                infoModel.registrationNo = (cursor.getString(cursor.getColumnIndex("shr_id")));
//                if(TextUtils.isEmpty(infoModel.registrationNo)){
//                    infoModel.registrationNo = (cursor.getString(cursor.getColumnIndex("unique_id")));
//                }
//                long lastInteractedDate = cursor.getLong(cursor.getColumnIndex("last_interacted_with"));
//                Date regdate = new Date(lastInteractedDate);
//                String registrationDate = AbstractDao.getDobDateFormat().format(regdate);
//                infoModel.registrationDate = registrationDate;
//                infoModel.centerName = (cursor.getString(cursor.getColumnIndex("camp_type")));
//                infoModel.subBlock = (cursor.getString(cursor.getColumnIndex("block_name")));
//                infoModel.village = (cursor.getString(cursor.getColumnIndex("village")));
//                infoModel.houseHoldNo = (cursor.getString(cursor.getColumnIndex("holding_no")));
//                infoModel.wardNo = (cursor.getString(cursor.getColumnIndex("ward_name")));
//                infoModel.union = (cursor.getString(cursor.getColumnIndex("union_zone")));
//                infoModel.genderEnglish = (cursor.getString(cursor.getColumnIndex("gender")));
//                infoModel.gender = HnppConstants.getGender(infoModel.genderEnglish);
//
//
//            }
//
//        } catch (Exception e) {
//            Timber.e(e);
//        }
//        finally {
//            if(cursor !=null) cursor.close();
//        }
//        return infoModel;
//
//    }
    public static TikaInfoModel getTikaDetailsFromClient(String baseEntityId){
        String query = "select json from client where baseEntityId ='"+baseEntityId+"' ";
        Cursor cursor = null;
        TikaInfoModel infoModel = null;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                String clientJson = cursor.getString(cursor.getColumnIndex("json"));
                Client domainClient = JsonFormUtils.gson.fromJson(clientJson, Client.class);


                infoModel = new TikaInfoModel();
                infoModel.baseEntityId = domainClient.getBaseEntityId();
                infoModel.name = domainClient.getFirstName()+" "+domainClient.getLastName();
                infoModel.motherName = domainClient.getAttribute("mother_name_english")==null?"":domainClient.getAttribute("mother_name_english")+"";
                infoModel.fatherName = domainClient.getAttribute("father_name_english")==null?"":domainClient.getAttribute("father_name_english")+"";
                String dob = DateUtil.fromDate(domainClient.getBirthdate());
                //2023-03-11T06:00:00.000+06:00
                infoModel.dob = dob.substring(0,dob.indexOf("T"));
                infoModel.birthYear = dob.substring(0,4);
                infoModel.birthMonth = dob.substring(5,7);
                infoModel.birthDay = dob.substring(8,10);
                infoModel.brid = domainClient.getAttribute("birthRegistrationID")==null?"":domainClient.getAttribute("birthRegistrationID")+"";
                infoModel.registrationNo = domainClient.getIdentifier("shr_id")==null?"":domainClient.getIdentifier("shr_id")+"";
                if(TextUtils.isEmpty(infoModel.registrationNo)){
                    infoModel.registrationNo = domainClient.getIdentifier("opensrp_id")==null?"":domainClient.getIdentifier("opensrp_id")+"";
                }
                String registrationDate = AbstractDao.getDobDateFormat().format(domainClient.getDateCreated());
                infoModel.registrationDate = registrationDate;
                infoModel.subBlock = domainClient.getAttribute("block_name")==null?"":domainClient.getAttribute("block_name")+"";
                infoModel.village = domainClient.getAddresses()!=null?domainClient.getAddresses().get(0).getCityVillage():""+"";
                infoModel.wardNo = domainClient.getAddresses()!=null?domainClient.getAddresses().get(0).getAddressField("address3"):""+"";
                infoModel.union = domainClient.getAddresses()!=null?domainClient.getAddresses().get(0).getAddressField("address1"):""+"";
                infoModel.genderEnglish = domainClient.getGender();
                infoModel.gender = HnppConstants.getGender(infoModel.genderEnglish);
                infoModel.division = domainClient.getAddresses()!=null?domainClient.getAddresses().get(0).getStateProvince():""+"";
                infoModel.district = domainClient.getAddresses()!=null?domainClient.getAddresses().get(0).getCountyDistrict():""+"";
                infoModel.upazilla = domainClient.getAddresses()!=null?domainClient.getAddresses().get(0).getAddressField("address2"):""+"";

                infoModel.divisionId = domainClient.getAttribute("division_id")==null?"0":domainClient.getAttribute("division_id")+"";
                infoModel.districtId = domainClient.getAttribute("district_id") == null?"0":domainClient.getAttribute("district_id")+"";

            }

        } catch (Exception e) {
            Timber.e(e);
        }
        finally {
            if(cursor !=null) cursor.close();
        }
        return infoModel;

    }

    public static HashMap<String, String> getDetails(String baseEntityId, String tableName) {
        HashMap<String, String> map = new HashMap<>();
        String query = "select * from "+tableName+" where base_entity_id='" + baseEntityId + "'";

        Cursor cursor =  HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        try {

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int columncount = cursor.getColumnCount();
                for(int i=0;i<columncount;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                cursor.moveToNext();
            }

        } catch (Exception e) {

        } finally {
            if(cursor!=null)cursor.close();
        }

        return map;
    }

    public static VisitInfo getVisitInfo(String eventType, String baseEntityId){
        String query = "select count(*) as count, max(visit_date) as v_date from ec_visit_log where base_entity_id ='"+baseEntityId+"' and visit_type ='"+eventType+"'";
        Cursor cursor = null;
        VisitInfo visitInfo = new VisitInfo();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                visitInfo.setVisitCount(cursor.getInt(0));
                visitInfo.setVisitDate(cursor.getInt(1));

            }

        } catch (Exception e) {
            Timber.e(e);
        }
        finally {
            if(cursor !=null) cursor.close();
        }
        return visitInfo;

    }

    public static void updateMigratedOrRejectedHH(String base_entity_id){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_family set "+DBConstants.KEY.DATE_REMOVED+" = '1' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static void updateMigratedOrRejectedMember(String base_entity_id){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_family_member set "+DBConstants.KEY.DATE_REMOVED+" = '1' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static boolean isAvailableStock(String stockName){
        String query = "select  product_name, (sum(coalesce(stock_quantity,0)) - sum(coalesce(achievemnt_count,0))) as balance from stock_table where  product_name='"+stockName+"' group by product_name having balance>0";
        Cursor cursor = null;
        boolean isAvailable = false;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    isAvailable = true;
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){

        }
        finally {
            if(cursor!=null) cursor.close();
        }
        return isAvailable;

    }


    public static StringBuilder getStockEnd(){
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH)+1;
        int year = calendar.get(Calendar.YEAR);

        String query = "select  product_name, (sum(coalesce(stock_quantity,0)) - sum(coalesce(achievemnt_count,0))) as balance from stock_table where "+ StockRepository.STOCK_TIMESTAMP+" < "+HnppConstants.getLongDateFormatForToMonth(year+"",month+"")+" group by product_name having (sum(stock_quantity) - sum(achievemnt_count))<"+STOCK_END_THRESHOLD;
       Log.v("NOTIFICATION_JOB","getStockEnd:"+query);
        Cursor cursor = null;
        StringBuilder nameCount = new StringBuilder();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    try{
                        nameCount.append(HnppApplication.appContext.getString(R.string.stock_name)+HnppConstants.getWorkSummeryTypeMapping().get(cursor.getString(0))+"\n");
                    }catch (Exception e){
                        nameCount.append(HnppApplication.appContext.getString(R.string.stock_name)+cursor.getString(0)+"\n");

                    }
                    nameCount.append(HnppApplication.appContext.getString(R.string.end_balance_2)+cursor.getString(1)+"\n");
                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return nameCount;
    }
    public static StringBuilder getEddThisMonth(){
        String query = "select ec_family_member.first_name, edd,STRFTIME('%Y', datetime('now')) as nowYear,STRFTIME('%m', datetime('now')) as nowMonth,substr(edd, 7, 4) as year,substr(edd, 4, 2) as month from ec_anc_register " +
                "inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id " +
                "where year=nowYear and month = nowMonth order by ec_anc_register.edd ASC";
        Log.v("NOTIFICATION_JOB","getEddThisMonth:"+query);
        Cursor cursor = null;
        StringBuilder nameCount = new StringBuilder();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    nameCount.append(HnppApplication.appContext.getString(R.string.name_only)).append(cursor.getString(0)).append("\n");
                    nameCount.append(HnppApplication.appContext.getString(R.string.edd_only)).append(cursor.getString(1)).append("\n");
                    nameCount.append("------------------------------------"+"\n");
                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return nameCount;
    }
    public static void updateBloodGroup(String base_entity_id, String value){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_family_member set blood_group = '"+value+"' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static void updateNextAncVisitDate(String base_entity_id, String value){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_anc_register set next_visit_date = '"+value+"' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            Log.v("FILTER_VISIT","updateNextAncVisitDate:"+sql);
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static boolean isAdolescent(String baseEntityId){
        String query = "select base_entity_id from ec_family_member where base_entity_id ='"+baseEntityId+"' and gender ='F' and (( julianday('now') - julianday(dob))/365) >=10 and (( julianday('now') - julianday(dob))/365) <=19";
        Cursor cursor = null;
        boolean isAdo = false;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                String d  = cursor.getString(0);
                if(!TextUtils.isEmpty(d)){
                    isAdo = true;
                }

            }

        } catch (Exception e) {
            Timber.e(e);
        }
        finally {
            if(cursor !=null) cursor.close();
        }
        return isAdo;

    }


    @SuppressLint("Range")
    public static GuestMemberData getGuestMemberById(String baseEntityId){
        String query = "Select * FROM ec_guest_member WHERE  date_removed is null and base_entity_id ='"+baseEntityId+"'";
        GuestMemberData guestMemberData = null;
        Cursor cursor = null;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                guestMemberData = new GuestMemberData();
                cursor.moveToFirst();
                guestMemberData.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                guestMemberData.setDivision(cursor.getString(cursor.getColumnIndex("division_per")));
                guestMemberData.setDistrict(cursor.getString(cursor.getColumnIndex("district_per")));
                guestMemberData.setUpozila(cursor.getString(cursor.getColumnIndex("upazila_per")));

                guestMemberData.setMemberId(cursor.getString(cursor.getColumnIndex("unique_id")));
                String firstName = cursor.getString(cursor.getColumnIndex("first_name"));
                String lastName = cursor.getString(cursor.getColumnIndex("last_name"));
                guestMemberData.setName(firstName+" "+lastName);
                guestMemberData.setDob(cursor.getString(cursor.getColumnIndex("dob")));
                guestMemberData.setGender(cursor.getString(cursor.getColumnIndex("gender")));
                guestMemberData.setPhoneNo(cursor.getString(cursor.getColumnIndex("phone_number")));
                cursor.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return guestMemberData;

    }
    public static String getChildFollowUpFormName(String baseEntityId){
        String query = "select ((( julianday('now') - julianday(dob))/365) *12) as age from ec_family_member where base_entity_id ='"+baseEntityId+"'";
        Cursor cursor = null;
        int month = 0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                month = cursor.getInt(0);
            }

        } catch (Exception e) {
            Timber.e(e);
        }
        finally {
            if(cursor!=null)cursor.close();
        }
        if(month>= 18 && month <= 36) return HnppConstants.EVENT_TYPE.CHILD_VISIT_18_36;
        if(month>= 6 && month < 24) return HnppConstants.EVENT_TYPE.CHILD_VISIT_7_24;
        if(month>= 0 && month < 6) return HnppConstants.EVENT_TYPE.CHILD_VISIT_0_6;
        return "";

    }
    public static String getBlockNameFromFamilyTable(String familyBaseEntityId){
        String query = "select block_name from ec_family  where base_entity_id = '"+familyBaseEntityId+"'";
        Cursor cursor = null;
        String birthWeight="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                birthWeight = cursor.getString(0);
            }

        } catch (Exception e) {
            Timber.e(e);

        }
        finally {
            if(cursor !=null)cursor.close();
        }
        return birthWeight;
    }
    public static String getBlockName(String baseEntityId){
        String query = "select ec_family.block_name from ec_family inner join ec_family_member on ec_family.base_entity_id = ec_family_member.relational_id where ec_family_member.base_entity_id = '"+baseEntityId+"'";
        Cursor cursor = null;
        String blockName="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                blockName = cursor.getString(0);
            }

        } catch (Exception e) {
            Timber.e(e);

        }
        finally {
            if(cursor !=null)cursor.close();
        }

        return blockName;
    }
    public static BaseLocation getBlocksHHID(String hhBaseEntityId){
        String query = "select block_name,block_id from ec_family where base_entity_id = '"+hhBaseEntityId+"'";
        Log.v("SAVE_VISIT","getBlocksIdFromMember>>query:"+query);
        Cursor cursor = null;
        BaseLocation blocksLocation = new BaseLocation();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                blocksLocation.name = cursor.getString(0);
                int bid= Double.valueOf(cursor.getString(1)).intValue();
                blocksLocation.id = bid;
            }

        } catch (Exception e) {
            Timber.e(e);

        }
        finally {
            if(cursor !=null)cursor.close();
        }
        return blocksLocation;
    }
    public static String getBlocksIdFromMember(String baseEntityId){
        String query = "select block_id from ec_family_member where base_entity_id = '"+baseEntityId+"'";
        Log.v("SAVE_VISIT","getBlocksIdFromMember>>query:"+query);
        Cursor cursor = null;
        String blockId = "";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                blockId = cursor.getString(0);
            }

        } catch (Exception e) {
            Timber.e(e);

        }
        finally {
            if(cursor !=null)cursor.close();
        }
        return blockId;
    }
    public static void updateCoronaFamilyMember(String base_entity_id, String value){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_family_member set is_corona = '"+value+"' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static void updateDeathMember(String base_entity_id){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String dodDate = HnppConstants.DDMMYY.format(System.currentTimeMillis());
            String sql = "update ec_family_member set dod = '"+dodDate+"', is_closed ='1', date_removed ='"+dodDate+"' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static void updateIsRiskFamilyMember(String base_entity_id, String value, String eventType){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_family_member set is_risk = '"+value+"', risk_event_type ='"+eventType+"' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public static void updateIsRiskChild(String base_entity_id, String value){
        try{
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "update ec_child set is_risk = '"+value+"' where " +
                    "base_entity_id = '"+base_entity_id+"' ;";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }

    public static ArrayList<ForumDetails> getPreviousForum(){
        String query = "select * from ec_visit_log where event_type ='"+HnppConstants.EVENT_TYPE.FORUM_CHILD+"' OR event_type = '"+HnppConstants.EVENT_TYPE.FORUM_ADO+"'" +
                " OR event_type ='"+HnppConstants.EVENT_TYPE.FORUM_NCD+"' OR event_type = '"+HnppConstants.EVENT_TYPE.FORUM_WOMEN+"' OR event_type ='"+HnppConstants.EVENT_TYPE.FORUM_ADULT+"' order by visit_date desc";

        List<Map<String, String>> valus = AbstractDao.readData(query, null);
        ArrayList<ForumDetails> visitIds = new ArrayList<>();
        for(Map<String, String> valu : valus){
            ForumDetails forumDetails = JsonFormUtils.gson.fromJson(valu.get("visit_json"),ForumDetails.class);
            if(forumDetails!=null)visitIds.add(forumDetails);

        }
        return visitIds;

    }

    public static CommonPersonObjectClient createFromBaseEntity(String baseEntityId){
        CommonPersonObjectClient pClient = null;
        String query = "Select ec_family_member.id as _id , ec_family_member.first_name , ec_family_member.last_name , ec_family_member.middle_name , ec_family_member.phone_number , ec_family_member.relational_id as relationalid , ec_family_member.entity_type , ec_family.village_town as village_name , ec_family_member.unique_id , ec_family_member.gender , ec_family_member.dob , ec_family.unique_id as house_hold_id , ec_family.first_name as house_hold_name , ec_family.module_id FROM ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE  WHERE  ec_family_member.date_removed is null and ec_family_member.base_entity_id ='"+baseEntityId+"'";
        CommonRepository commonRepository = Utils.context().commonrepository("ec_family_member");
        Cursor cursor = null;
        try {
           cursor = commonRepository.rawCustomQueryForAdapter(query);
            if (cursor != null && cursor.moveToFirst()) {
                CommonPersonObject personObject = commonRepository.readAllcommonforCursorAdapter(cursor);
                pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                        personObject.getDetails(), "");
                pClient.setColumnmaps(personObject.getColumnmaps());
            }
        } catch (Exception ex) {
            Timber.e(ex, "CoreChildProfileInteractor --> updateChildCommonPerson");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pClient;

    }
    public static CommonPersonObjectClient createFromBaseEntityForGuestMember(String baseEntityId){
        CommonPersonObjectClient pClient = null;
        String query = "Select * FROM ec_guest_member WHERE base_entity_id ='"+baseEntityId+"'";
        CommonRepository commonRepository = Utils.context().commonrepository("ec_guest_member");
        Cursor cursor = null;
        try {
            //cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor = commonRepository.rawCustomQueryForAdapter(query);
            if (cursor != null && cursor.moveToFirst()) {
                CommonPersonObject personObject = commonRepository.readAllcommonforCursorAdapter(cursor);
                //personObject.setCaseId(baseEntityId);
                pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                        personObject.getDetails(), "");
                pClient.setColumnmaps(personObject.getColumnmaps());
            }
        } catch (Exception ex) {
            Timber.e(ex, "CoreChildProfileInteractor --> updateChildCommonPerson");
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pClient;

    }
    public static CommonPersonObjectClient getCommonPersonByBaseEntityId(String baseEntityId){
        CommonPersonObjectClient pClient = null;
        String query = "Select * FROM ec_family_member WHERE base_entity_id ='"+baseEntityId+"'";
        CommonRepository commonRepository = Utils.context().commonrepository("ec_family_member");
        Cursor cursor = null;
        try {
            //cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor = commonRepository.rawCustomQueryForAdapter(query);
            if (cursor != null && cursor.moveToFirst()) {
                CommonPersonObject personObject = commonRepository.readAllcommonforCursorAdapter(cursor);
                //personObject.setCaseId(baseEntityId);
                pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                        personObject.getDetails(), "");
                pClient.setColumnmaps(personObject.getColumnmaps());
            }
        } catch (Exception ex) {
            Timber.e(ex, "CoreChildProfileInteractor --> updateChildCommonPerson");
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pClient;

    }
    public static String getIsCorona(String baseEntityId){
        String query = "select ec_family_member.is_corona from ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id where ec_family_member.base_entity_id = '"+baseEntityId+"'" +
                " and (strftime('%d',datetime('now')) - strftime('%d',datetime(last_home_visit/1000,'unixepoch','localtime'))) <= 14";
        Log.v("IS_CORONA","query:"+query);
        Cursor cursor = null;
        String isCorona="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                isCorona = cursor.getString(0);

            }
            if(cursor!=null)cursor.close();

            return isCorona;
        } catch (Exception e) {
            Timber.e(e);
        }
        return isCorona;
    }


    public static String getGuid(String baseEntityId){
        String query = "select gu_id from ec_family_member where base_entity_id = '"+baseEntityId+"'";
        Cursor cursor = null;
        String birthWeight="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                birthWeight = cursor.getString(0);
            }
            if(cursor!=null)cursor.close();
            return birthWeight;
        } catch (Exception e) {
            Timber.e(e);
        }
        return birthWeight;
    }
    public static int getChildRefCount(long fromMonth, long toMonth){
        String query;
        if(fromMonth == -1 && toMonth == -1){
            query= "select count(DISTINCT(baseEntityId)) from event where eventType = 'Referral Clinic' group by baseEntityId";

        }
        else{
            String fromMonthStr = HnppConstants.getDateFormateFromLong(fromMonth);
            String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
            query= "select count(DISTINCT(baseEntityId)) from event where eventType = 'Referral Clinic' and eventDate between '"+fromMonthStr+"' and '"+toMonthStr+"'";

        }
        Log.v("GMP_REPORT","getChildRefCount>>>"+query);
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public static int getChildGmpCounselingCount(long fromMonth, long toMonth ){
        String query;
        if(fromMonth == -1 && toMonth == -1){
            query = "select count(DISTINCT(baseEntityId)) from event where eventType = '"+HnppConstants.EVENT_TYPE.GMP_COUNSELING+"' ";
        }
        else{
            String fromMonthStr = HnppConstants.getDateFormateFromLong(fromMonth);
            String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
            query= "select count(DISTINCT(baseEntityId)) from event where eventType = '"+HnppConstants.EVENT_TYPE.GMP_COUNSELING+"' and eventDate between '"+fromMonthStr+"' and '"+toMonthStr+"'";

        }
        Log.v("GMP_REPORT","getChildGmpCounselingCount>>>"+query);
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public static int getChildRefFollowupCount(long fromMonth, long toMonth){
        String query;
        if(fromMonth == -1 && toMonth == -1){
            query = "select count(DISTINCT(baseEntityId)) from event where eventType = '"+HnppConstants.EVENT_TYPE.GMP_REFERREL_FOLLOWUP+"' ";
        }
        else{
            String fromMonthStr = HnppConstants.getDateFormateFromLong(fromMonth);
            String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
            query= "select count(DISTINCT(baseEntityId)) from event where eventType = '"+HnppConstants.EVENT_TYPE.GMP_REFERREL_FOLLOWUP+"' and eventDate between '"+fromMonthStr+"' and '"+toMonthStr+"'";

        }
        Log.v("GMP_REPORT","getChildRefCount>>>"+query);
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public static int getImmunizationCount(){

        String query = "select count(DISTINCT(baseEntityId)) from event where (eventType = 'Vaccination' OR eventType = 'Recurring Service') ";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public static int getGMPCount(long fromMonth, long toMonth){
        String query;
        if(fromMonth == -1 && toMonth == -1){
            query = "select count(DISTINCT(baseEntityId)) from event where (eventType = 'Height Monitoring' OR eventType = 'Weight Monitoring' OR eventType = 'MUAC Monitoring') ";        }
        else{
            String fromMonthStr = HnppConstants.getDateFormateFromLong(fromMonth);
            String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
            query= "select count(DISTINCT(baseEntityId)) from event where (eventType = 'Height Monitoring' OR eventType = 'Weight Monitoring' OR eventType = 'MUAC Monitoring') and eventDate between '"+fromMonthStr+"' and '"+toMonthStr+"'";

        }
        Log.v("GMP_REPORT","getGMPCount>>>"+query);
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public static int getNotUniqueCount(long fromMonth, long toMonth){
        int totalCount = 0;
        String query= "select count(DISTINCT(base_entity_id)) from weights where date not between '"+fromMonth+"' and '"+toMonth+"'";


        Log.v("GMP_REPORT","getChildGmpCounselingCount>>>"+query);
        Cursor cursor = null;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                totalCount += cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
        } catch (Exception e) {
            Timber.e(e);
        }
        query= "select count(DISTINCT(base_entity_id)) from heights where date not between '"+fromMonth+"' and '"+toMonth+"'";


        Log.v("GMP_REPORT","getChildGmpCounselingCount>>>"+query);
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                totalCount += cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
        } catch (Exception e) {
            Timber.e(e);
        }
        query= "select count(DISTINCT(base_entity_id)) from muac_tbl where date not between '"+fromMonth+"' and '"+toMonth+"'";


        Log.v("GMP_REPORT","getChildGmpCounselingCount>>>"+query);
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                totalCount += cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
        } catch (Exception e) {
            Timber.e(e);
        }
        return totalCount;
    }
    public static int getGMPCount(long fromMonth, long toMonth,String tableName){
        String query;
        if(fromMonth == -1 && toMonth == -1){
            query = "select count(DISTINCT(base_entity_id)) from "+tableName+" ";        }
        else{

            query= "select count(DISTINCT(base_entity_id)) from "+tableName+" where date between '"+fromMonth+"' and '"+toMonth+"'";

        }
        Log.v("GMP_REPORT","getChildGmpCounselingCount>>>"+query);
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
//    public static int getGMPCount(long fromMonth, long toMonth, String eventType){
//        String query;
//        if(fromMonth == -1 && toMonth == -1){
//            query = "select count(DISTINCT(baseEntityId)) from event where eventType='"+eventType+"' ";        }
//        else{
//            String fromMonthStr = HnppConstants.getDateFormateFromLong(fromMonth);
//            String toMonthStr = HnppConstants.getDateFormateFromLong(toMonth);
//            query= "select count(DISTINCT(baseEntityId)) from event where eventType='"+eventType+"' and eventDate between '"+fromMonthStr+"' and '"+toMonthStr+"'";
//
//        }
//        Log.v("GMP_REPORT","getChildGmpCounselingCount>>>"+query);
//        Cursor cursor = null;
//        int count=0;
//        try {
//            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
//            if(cursor !=null && cursor.getCount() >0){
//                cursor.moveToFirst();
//                count = cursor.getInt(0);
//            }
//            if(cursor!=null)cursor.close();
//            return count;
//        } catch (Exception e) {
//            Timber.e(e);
//        }
//        return count;
//    }
    public static int getHouseHoldCount(){

        String query = "select count(*) from ec_family";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public static int getOCACount(){

        String query = "select count(*) from ec_guest_member";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public static boolean isExitHouseHoldId(String uniqueId){

        String query = "select count(*) from ec_family where unique_id = '"+uniqueId+"'";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count>0;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count>0;
    }
    public static boolean isRisk(String baseEntityId, String eventType){

        String query = "select count(*) from ec_family_member where base_entity_id = '"+baseEntityId+"' and is_risk ='true' and risk_event_type ='"+ eventType+"'";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count>0;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count>0;
    }
    public static boolean isRiskAll(String baseEntityId){

        String query = "select count(*) from ec_family_member where base_entity_id = '"+baseEntityId+"' and is_risk ='true'";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count>0;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count>0;
    }
    public static boolean childRisk(String baseEntityId){

        String query = "select count(*) from ec_child where base_entity_id = '"+baseEntityId+"' and is_risk ='1'";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count>0;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count>0;
    }
    public static boolean isAncRisk(String baseEntityId){
        //String query = "select count(*) from ec_family_member where base_entity_id = '"+baseEntityId+"' and is_risk ='true' and (risk_event_type ='"+ HnppConstants.EVENT_TYPE.ANC_REGISTRATION +"' OR risk_event_type ='"+ HnppConstants.EventType.ANC_HOME_VISIT +"' OR risk_event_type ='"+ HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY +"' OR risk_event_type ='"+ HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE +"')";
        String query = "select count(*) from ec_family_member where base_entity_id = '"+baseEntityId+"' and is_risk ='true' and (risk_event_type ='"+ HnppConstants.EVENT_TYPE.ANC_REGISTRATION +"' OR risk_event_type ='"+ HnppConstants.EventType.ANC_HOME_VISIT +"' )";

        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count>0;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count>0;
    }
    public static boolean isPncRisk(String baseEntityId){
        String query = "select count(*) from ec_family_member where base_entity_id = '"+baseEntityId+"' and is_risk ='true' and risk_event_type ='"+ HnppConstants.EVENT_TYPE.PNC_REGISTRATION +"'";

        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count>0;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count>0;
    }
    public static int getCoutByFingerPrint(String startTime, String endTime){
        String query = "select count(*) from ec_family_member where gu_id IS NOT NULL and gu_id !='test' and strftime('%Y-%m-%d', datetime((last_interacted_with)/1000,'unixepoch') ) BETWEEN '"+startTime+"' AND '"+endTime+"'";
        Cursor cursor = null;
        int count=0;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }

    public static IdentityModel getBaseEntityByGuId(String guid){
        String query = "select ec_family_member.base_entity_id,ec_family_member.first_name,ec_family_member.unique_id,ec_family.first_name,ec_family_member.dob from ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE  where ec_family_member.gu_id = '"+guid+"'";
        Cursor cursor = null;
        IdentityModel identityModel = new IdentityModel();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                identityModel.setBaseEntityId(cursor.getString(0));
                identityModel.setName(cursor.getString(1));
                identityModel.setId(cursor.getString(2));
                identityModel.setFamilyHead(cursor.getString(3));
                String dobString = Utils.getDuration(cursor.getString(4));
                identityModel.setAge(dobString);
            }

            return identityModel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(cursor!=null) cursor.close();
        }
        return identityModel;
    }

    public static String getBirthWeight(String baseEntityId){
        String query = "select birth_weight from ec_child where base_entity_id = '"+baseEntityId+"'";
        Cursor cursor = null;
        String birthWeight="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                birthWeight = cursor.getString(0);
            }
            if(cursor!=null)cursor.close();
            return birthWeight;
        } catch (Exception e) {
            Timber.e(e);
        }
        return birthWeight;
    }

    public static void populatePNCChildDetails(String baseEntityId, JSONObject jsonForm){
        String query = "select " +
                "ec_child.first_name," +
                "ec_child.dob," +
                "ec_child.gender," +
                "ec_child.birth_weight_taken," +
                "ec_child.birth_weight," +
                "ec_child.chlorohexadin," +
                "ec_child.breastfeeding_time," +
                "ec_child.head_body_covered," +
                "ec_child.breast_feeded," +"ec_child.which_problem" +
                " from ec_child where ec_child.mother_entity_id = '"+baseEntityId+"' AND ec_child.entry_point = 'PNC'";
        Cursor cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        HashMap<String,String> child_details = new HashMap<>();
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            child_details.put("first_name",cursor.getString(0));
            child_details.put("dob",cursor.getString(1));
            child_details.put("gender",translateValues(cursor.getString(2)));
            child_details.put("birth_weight_taken",translateValues(cursor.getString(3)));
            child_details.put("birth_weight",translateValues(cursor.getString(4)));
            child_details.put("chlorohexadin",translateValues(cursor.getString(5)));
            child_details.put("breastfeeding_time",cursor.getString(6));
            child_details.put("head_body_covered",translateValues(cursor.getString(7)));
            child_details.put("physically_challenged",translateValues(cursor.getString(8)));
            child_details.put("breast_feeded",translateValues(cursor.getString(9)));
            child_details.put("which_problem",translateValues(cursor.getString(10)));
            try {
                JSONObject stepOne = jsonForm.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject fieldObject = jsonArray.getJSONObject(i);
                    String key = fieldObject.getString(org.smartregister.util.JsonFormUtils.KEY);
                    if(child_details.containsKey(key)){
                        fieldObject.put(org.smartregister.util.JsonFormUtils.VALUE,child_details.get(key));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

       if(cursor !=null) cursor.close();
    }
    public static String translateValues(String value){
        if(value==null)return "";
        if(value.equalsIgnoreCase("Yes")){
            return HnppApplication.appContext.getString(R.string.yes);
        }
        if(value.equalsIgnoreCase("No")){
            return HnppApplication.appContext.getString(R.string.no);
        }
        if(value.equalsIgnoreCase("M")){
            return HnppApplication.appContext.getString(R.string.man);
        }
        if(value.equalsIgnoreCase("F")){
            return HnppApplication.appContext.getString(R.string.woman2);
        }
        if(value.equalsIgnoreCase("O")){
            return HnppApplication.appContext.getString(R.string.third_gender);
        }
        return value;
    }
    public static ArrayList<ProfileDueInfo> getDueListByFamilyId(String familyId){
        ArrayList<ProfileDueInfo> profileDueInfoArrayList = new ArrayList<>();
        String query = "select base_entity_id,gender,marital_status,first_name,dob from ec_family_member where relational_id = '"+familyId+"' and date_removed is null";
        Cursor cursor = null;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    ProfileDueInfo profileDueInfo = new ProfileDueInfo();
                    profileDueInfo.setBaseEntityId(cursor.getString(0));
                    profileDueInfo.setGender(cursor.getString(1));
                    profileDueInfo.setMaritalStatus(cursor.getString(2));
                    profileDueInfo.setName(cursor.getString(3));
                    profileDueInfo.setDob(cursor.getString(4));
                    String dobString = Utils.getDuration(profileDueInfo.getDob());
                    profileDueInfo.setAge(dobString);
                    try{
                        if(profileDueInfo.getGender().equalsIgnoreCase("F") && profileDueInfo.getMaritalStatus().equalsIgnoreCase("Married")){
                            String eventType = FormApplicability.getDueFormForMarriedWomen(profileDueInfo.getBaseEntityId(),
                                    FormApplicability.getAge(profileDueInfo.getDob()));
                            profileDueInfo.setOriginalEventType(eventType);
                            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ELCO)){
                                eventType = HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
                            }
                            //if(FormApplicability.isDueAnyForm(profileDueInfo.getBaseEntityId(),eventType) && !TextUtils.isEmpty(eventType)){
                                if(eventType.equalsIgnoreCase(HnppApplication.appContext.getString(R.string.pregnancy_history))){
                                    profileDueInfo.setEventType(HnppApplication.appContext.getString(R.string.pregnancy_care_1_st_trimester));
                                }else{
                                    profileDueInfo.setEventType(HnppConstants.getVisitEventTypeMapping().get(eventType));
                                }
                                if(FormApplicability.isDueCoronaForm(profileDueInfo.getBaseEntityId())){
                                    profileDueInfo.setEventType(HnppConstants.getVisitEventTypeMapping().get(HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL));

                                }
                                profileDueInfoArrayList.add(profileDueInfo);
                            //}


                        }else{
                            if(FormApplicability.isDueCoronaForm(profileDueInfo.getBaseEntityId())){
                                profileDueInfo.setEventType(HnppConstants.getVisitEventTypeMapping().get(HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL));

                            }
                            profileDueInfoArrayList.add(profileDueInfo);
                        }

//                        else {
//                            Date dob = Utils.dobStringToDate(profileDueInfo.getDob());
//                            boolean isEnc = FormApplicability.isEncVisible(dob);
//                            if(isEnc){
//                                profileDueInfo.setEventType(HnppConstants.getVisitEventTypeMapping().get(HnppConstants.EVENT_TYPE.ENC_REGISTRATION));
//                                profileDueInfoArrayList.add(profileDueInfo);
//                            }
//                        }
                    }catch (Exception e){

                    }


                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
            Iterator<ProfileDueInfo> profileDueInfoIterator = profileDueInfoArrayList.iterator();
            while (profileDueInfoIterator.hasNext()){
                ProfileDueInfo p = profileDueInfoIterator.next();
                if(!FormApplicability.isDueAnyForm(p.getBaseEntityId(),p.getOriginalEventType()) && !TextUtils.isEmpty(p.getOriginalEventType())){
                    profileDueInfoIterator.remove();
                }
            }
        }
        return profileDueInfoArrayList;
    }



    public static List<Map<String,String>>  getGenderMaritalStatus(String baseEntityId){
            String lmp = "SELECT gender,marital_status FROM ec_family_member where base_entity_id = ? ";
            List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});
            return valus;
    }
    public static String  getFamilyMobileNo(String baseEntityId){
        String lmp = "SELECT phone_number FROM ec_family where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});
        if(valus.size()>0){
            return valus.get(0).get("phone_number");
        }
        return "";
    }
    public static String getVisitIdByFormSubmissionId(String formSubmissionId){
        String query = " select visit_id from visits where form_submission_id='"+formSubmissionId+"'";
        Cursor cursor = null;
        String visitId="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    visitId = cursor.getString(0);
                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return visitId;
    }


    public static String getMotherBaseEntityId(String familyId, String motherName){
        String query = "select base_entity_id from ec_family_member where  first_name = '"+motherName+"' and relational_id = '"+familyId+"'";
        Cursor cursor = null;
        String entityId="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    entityId = cursor.getString(0);
                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return entityId;
    }
    public static String[] getNameMobileFromFamily(String familyID){
        return getNameMobile(familyID,"ec_family");
    }
    public static String[] getNameMobile(String familyID,String tableName){
        String query = "select first_name,last_name,phone_number from "+tableName+" where base_entity_id = '"+familyID+"'";
        Cursor cursor = null;
        String[] nameNumber = new String[3];
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            nameNumber[0] = cursor.getString(0);
            nameNumber[1] = cursor.getString(1);
            nameNumber[2] = cursor.getString(2);
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return nameNumber;
    }
    public static String getChampType(String familyID){
        String query = "select camp_type from ec_family where base_entity_id = '"+familyID+"'";
        Cursor cursor = null;
        String champType ="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            champType = cursor.getString(0);
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return champType;
    }
    public static ArrayList<String> getAllWomenInHouseHold(String familyID){
        String query = "select first_name from ec_family_member where (gender = 'নারী' OR gender = 'F') and ((marital_status != 'অবিবাহিত' AND marital_status != 'Unmarried') and marital_status IS NOT NULL) and relational_id = '"+familyID+"'";
        Cursor cursor = null;
        ArrayList<String> womenList = new ArrayList<>();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(0);
                womenList.add(name);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return womenList;
    }
    public static ArrayList<String[]> getAllMembersInHouseHold(String familyID){
        String query = "select first_name,base_entity_id from ec_family_member where relational_id = '"+familyID+"' and date_removed is null";
        Cursor cursor = null;
        ArrayList<String[]> memberList = new ArrayList<>();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String[] strs = new String[2];
                strs[0] = cursor.getString(0);
                strs[1] = cursor.getString(1);
                memberList.add(strs);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return memberList;
    }
    public static ArrayList<String> getAllWomenInHouseHold(String entityId, String familyID){
        String query = "select first_name from ec_family_member where (gender = 'নারী' OR gender = 'F') and ((marital_status != 'অবিবাহিত' AND marital_status != 'Unmarried') and marital_status IS NOT NULL) and relational_id = '"+familyID+"' and base_entity_id != '"+entityId+"'";
        Cursor cursor = null;
        ArrayList<String> womenList = new ArrayList<>();
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(0);
                womenList.add(name);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return womenList;
    }
    public static String getNameBaseEntityId(String baseEntityId){
        String query = "select first_name from ec_family_member where base_entity_id = '"+baseEntityId+"'";
        Cursor cursor = null;
        String motherName="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                motherName = cursor.getString(0);
            }
            if(cursor!=null)cursor.close();
            return motherName;
        } catch (Exception e) {
            Timber.e(e);
        }
        return motherName;
    }
    public static String getFamilyIdFromBaseEntityId(String baseEntityId){
        String query = "select relational_id from ec_family_member where base_entity_id = '"+baseEntityId+"'";
        Cursor cursor = null;
        String motherName="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                motherName = cursor.getString(0);
            }

            return motherName;
        } catch (Exception e) {
            Timber.e(e);

        }
        finally {
            if(cursor !=null) cursor.close();
        }
        return motherName;
    }
    public static String getMotherNameFromMemberTable(String motherEntityId){
        String query = "select first_name from ec_family_member where base_entity_id = '"+motherEntityId+"'";
        Cursor cursor = null;
        String motherName="";
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                motherName = cursor.getString(0);
            }
            if(cursor!=null)cursor.close();
            return motherName;
        } catch (Exception e) {
            Timber.e(e);
        }
        return motherName;
    }
    public static HouseHoldInfo getHouseHoldInfo(String familyBaseEntityId){
        String query = "select first_name,unique_id,module_id,family_head,primary_caregiver,block_name from ec_family where base_entity_id = '"+familyBaseEntityId+"'";
        Cursor cursor = null;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HouseHoldInfo houseHoldInfo = new HouseHoldInfo();
                houseHoldInfo.setHouseHoldName( cursor.getString(0));
                houseHoldInfo.setHouseHoldBaseEntityId(familyBaseEntityId);
                houseHoldInfo.setHouseHoldUniqueId(cursor.getString(1));
                houseHoldInfo.setModuleId(cursor.getString(2));
                houseHoldInfo.setHouseHoldHeadId(cursor.getString(3));
                houseHoldInfo.setPrimaryCaregiverId(cursor.getString(4));
                houseHoldInfo.setBlockName(cursor.getString(5));
                return houseHoldInfo;
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    public static String getModuleId(String familyId){
        if(!HnppConstants.isReleaseBuild()) return HnppConstants.MODULE_ID_TRAINING;
        String query = "select module_id from ec_family where base_entity_id = '"+familyId+"'";
        Cursor cursor = null;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(0);
                return name;
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }


    public static String matchPhrase(String phrase) {
        String stringPhrase = phrase;
        if (StringUtils.isEmpty(stringPhrase)) {
            stringPhrase = "";
        }

        // Underscore does not work well in fts search
//        if (stringPhrase.contains("_")) {
//            stringPhrase = stringPhrase.replace("_", "");
//        }
        return " MATCH '" + stringPhrase + "*' ";

    }
    public static String mainSelect(String tableName, String familyTableName, String familyMemberTableName, String mainCondition) {
        return mainSelectRegisterWithoutGroupby(tableName, familyTableName, familyMemberTableName, tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = '" + mainCondition + "'");
    }

    public static String mainSelectRegisterWithoutGroupby(String tableName, String familyTableName, String familyMemberTableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, mainColumns(tableName, familyTableName, familyMemberTableName));
        queryBUilder.customJoin("LEFT JOIN " + familyTableName + " ON  " + tableName + "." + DBConstants.KEY.RELATIONAL_ID + " = " + familyTableName + ".id COLLATE NOCASE ");
        queryBUilder.customJoin("LEFT JOIN " + familyMemberTableName + " ON  " + familyMemberTableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + familyTableName + ".primary_caregiver COLLATE NOCASE ");

        return queryBUilder.mainCondition(mainCondition);
    }
    //TODO need to change manually
    public static String[] mainColumns(String tableName, String familyTable, String familyMemberTable) {
        ArrayList<String> columnList = new ArrayList<>();
        columnList.add(tableName + "." + DBConstants.KEY.RELATIONAL_ID + " as " + ChildDBConstants.KEY.RELATIONAL_ID);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH);
        columnList.add(tableName + "." + DBConstants.KEY.BASE_ENTITY_ID);
        columnList.add(tableName + "." + DBConstants.KEY.FIRST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.MIDDLE_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.FIRST_NAME + " as " + ChildDBConstants.KEY.FAMILY_FIRST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.LAST_NAME + " as " + ChildDBConstants.KEY.FAMILY_LAST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.MIDDLE_NAME + " as " + ChildDBConstants.KEY.FAMILY_MIDDLE_NAME);
        columnList.add(familyMemberTable + "." + ChildDBConstants.PHONE_NUMBER + " as " + ChildDBConstants.KEY.FAMILY_MEMBER_PHONENUMBER);
        columnList.add(familyMemberTable + "." + ChildDBConstants.OTHER_PHONE_NUMBER + " as " + ChildDBConstants.KEY.FAMILY_MEMBER_PHONENUMBER_OTHER);
//        columnList.add(familyTable + "." + HnppConstants.KEY.VILLAGE_NAME + " as " + ChildDBConstants.KEY.FAMILY_HOME_ADDRESS);
        columnList.add(familyTable + "." + ChildDBConstants.PHONE_NUMBER);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.UNIQUE_ID);
        columnList.add(tableName + "." + DBConstants.KEY.GENDER);
        columnList.add(tableName + "." + DBConstants.KEY.DOB);
        columnList.add(tableName + "." + org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN);
        columnList.add(tableName + "." + ChildDBConstants.KEY.LAST_HOME_VISIT);
        columnList.add(tableName + "." + ChildDBConstants.KEY.DATE_CREATED);
        columnList.add(tableName + "." + HnppConstants.KEY.RELATION_WITH_HOUSEHOLD);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_MOTHER_NAME);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED);
        columnList.add(tableName + "." + HnppConstants.KEY.BLOOD_GROUP);
        columnList.add(tableName + "." + HnppConstants.KEY.SHR_ID);
        columnList.add(tableName + "." + HnppConstants.KEY.HAS_AEFI);
        columnList.add(tableName + "." + HnppConstants.KEY.NEW_BORN_INFO);
        columnList.add(tableName + "." + HnppConstants.KEY.AEFI_VACCINE);
        columnList.add(tableName + "." + HnppConstants.KEY.DUE_VACCINE_NAME);
        columnList.add(tableName + "." + HnppConstants.KEY.DUE_VACCINE_DATE);
        columnList.add(tableName + "." + HnppConstants.KEY.IS_RISK);
        columnList.add(tableName + "." + HnppConstants.KEY.DUE_VACCINE_WEEK);
        columnList.add(tableName + "." + ChildDBConstants.PHONE_NUMBER);
        columnList.add(tableName + "." + HnppConstants.KEY.FATHER_NAME_ENGLISH);
        columnList.add(tableName + "." + HnppConstants.KEY.FATHER_NAME_BANGLA);
        columnList.add(tableName + ".birth_id");
        columnList.add(tableName + ".disability");
        columnList.add(tableName + ".disability_type");
        columnList.add(tableName + ".camp_type");

        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_WEIGHT_TAKEN);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_WEIGHT);
        columnList.add(tableName + "." + ChildDBConstants.KEY.CHLOROHEXADIN);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BREASTFEEDING_TIME);
        columnList.add(tableName + "." + ChildDBConstants.KEY.HEAD_BODY_COVERED);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BREAST_FEEDED);
        columnList.add(tableName + "." + ChildDBConstants.KEY.SKIN);
        columnList.add(tableName + "." + ChildDBConstants.KEY.COUNSELLING);
        return columnList.toArray(new String[columnList.size()]);
    }
    public static String getMotherName(String motherEntityId, String relationId, String motherName){
        if(motherEntityId.isEmpty()) return motherName;
        if(motherEntityId.equalsIgnoreCase(relationId)) return motherName;
        String mName = getMotherNameFromMemberTable(motherEntityId);
        return TextUtils.isEmpty(mName)?motherName:mName;
    }
    public static String getMotherId(String baseEntityId) {
        String motherQuery = "SELECT mother_id FROM ec_child where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(motherQuery, new String[]{baseEntityId});

        return valus.get(0).get("mother_id");
    }
    public static String getLmpDate(String baseEntityId) {
        String lmp = "SELECT last_menstrual_period FROM ec_anc_register where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});

        return valus.get(0).get("last_menstrual_period");
    }
    public static String getWeight(String baseEntityId) {
        String lmp = "SELECT birth_weight FROM ec_child where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});

        return valus.get(0).get("birth_weight");
    }
    public static String getBloodGroup(String baseEntityId) {
        String lmp = "SELECT blood_group FROM ec_family_member where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});

        return valus.get(0).get("blood_group");
    }
    public static String getDueVaccineDate(String baseEntityId) {
        String lmp = "SELECT due_vaccine_date FROM ec_child where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});

        return valus.get(0).get("due_vaccine_date");
    }
    public static Map<String, String> getMotherName(String baseEntityId) {
        String mem = "SELECT first_name,last_name,member_name_bengla,phone_number FROM ec_family_member where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(mem, new String[]{baseEntityId});

        return valus.get(0);
    }

    public static String getaefiVaccines(String baseEntityId) {
        String lmp = "SELECT aefi_vaccines FROM ec_child where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});

        return valus.get(0).get("aefi_vaccines");
    }
    public static String getSessionInfo(String baseEntityId) {
        String lmp = "SELECT session_info_received FROM ec_child where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});
        return valus.get(0).get("session_info_received");
    }

    public static OtherVaccineContentData getMemberInfo(String baseEntityId) {
            String query = "select first_name,last_name,dob,birth_id,phone_number,mother_name_english,father_name_english,gender,unique_id,member_name_bengla,mother_name_bangla,father_name_bangla from ec_family_member where base_entity_id = '"+baseEntityId+"'";
            Cursor cursor = null;
            try {
                cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    OtherVaccineContentData memberInfo = new OtherVaccineContentData();
                    memberInfo.firstName = cursor.getString(0);
                    memberInfo.lastName = cursor.getString(1);
                    memberInfo.dob = cursor.getString(2);
                    memberInfo.brn = cursor.getString(3);
                    memberInfo.dob = memberInfo.dob.substring(0,memberInfo.dob.indexOf("T"));
                    memberInfo.mobile = cursor.getString(4);
                    memberInfo.mothernameEn = cursor.getString(5);
                    memberInfo.fatherNameEn = cursor.getString(6);
                    memberInfo.gender = cursor.getString(7);
                    memberInfo.vaccine_name = "HPV";
                    if(TextUtils.isEmpty(memberInfo.brn)){
                        memberInfo.brn = cursor.getString(8);
                    }
                    memberInfo.firstNameBN = cursor.getString(9);
                    memberInfo.motherName = cursor.getString(10);
                    memberInfo.fatherName = cursor.getString(11);
                    return memberInfo;
                }
            } catch (Exception e) {
                Timber.e(e);
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;

    }
}
