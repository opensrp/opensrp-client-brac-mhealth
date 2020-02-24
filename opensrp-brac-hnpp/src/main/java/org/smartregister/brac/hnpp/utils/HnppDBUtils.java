package org.smartregister.brac.hnpp.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.AbstractDao;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class HnppDBUtils extends CoreChildUtils {

    public static String getBirthWeight(String baseEntityId){
        String query = "select birth_weight from ec_child where base_entity_id = '"+baseEntityId+"'";
        Cursor cursor = null;
        String birthWeight="";
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                birthWeight = cursor.getString(0);
                cursor.close();
            }

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
                "ec_child.physically_challenged," +
                "ec_child.breast_feeded" +
                " from ec_child where ec_child.mother_entity_id = '"+baseEntityId+"' AND ec_child.entry_point = 'PNC'";
        Cursor cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
            return "হ্যাঁ";
        }
        if(value.equalsIgnoreCase("No")){
            return "না";
        }
        if(value.equalsIgnoreCase("M")){
            return "পুরুষ";
        }
        if(value.equalsIgnoreCase("F")){
            return "নারী";
        }
        if(value.equalsIgnoreCase("O")){
            return "তৃতীয় লিঙ্গ";
        }
        return value;
    }
    public static ArrayList<ProfileDueInfo> getDueListByFamilyId(String familyId){
        ArrayList<ProfileDueInfo> profileDueInfoArrayList = new ArrayList<>();
        String query = "select base_entity_id,gender,marital_status,first_name,dob from ec_family_member where relational_id = '"+familyId+"' and date_removed is null";
        Cursor cursor = null;
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
                            //if(FormApplicability.isDueAnyForm(profileDueInfo.getBaseEntityId(),eventType) && !TextUtils.isEmpty(eventType)){
                                if(eventType.equalsIgnoreCase("পূর্বের গর্ভের ইতিহাস")){
                                    profileDueInfo.setEventType("গর্ভবতী পরিচর্যা - ১ম ত্রিমাসিক");
                                }else{
                                    profileDueInfo.setEventType(HnppConstants.visitEventTypeMapping.get(eventType));
                                }
                                profileDueInfoArrayList.add(profileDueInfo);
                            //}


                        }
//                        else {
//                            Date dob = Utils.dobStringToDate(profileDueInfo.getDob());
//                            boolean isEnc = FormApplicability.isEncVisible(dob);
//                            if(isEnc){
//                                profileDueInfo.setEventType(HnppConstants.visitEventTypeMapping.get(HnppConstants.EVENT_TYPE.ENC_REGISTRATION));
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


    public static String getMotherBaseEntityId(String familyId, String motherName){
        String query = "select base_entity_id from ec_family_member where  first_name = '"+motherName+"' and relational_id = '"+familyId+"'";
        Cursor cursor = null;
        String entityId="";
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
        String query = "select first_name,phone_number from "+tableName+" where base_entity_id = '"+familyID+"'";
        Cursor cursor = null;
        String[] nameNumber = new String[2];
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            nameNumber[0] = cursor.getString(0);
            nameNumber[1] = cursor.getString(1);
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return nameNumber;
    }
    public static ArrayList<String> getAllWomenInHouseHold(String familyID){
        String query = "select first_name from ec_family_member where (gender = 'নারী' OR gender = 'F') and ((marital_status != 'অবিবাহিত' AND marital_status != 'Unmarried') and marital_status IS NOT NULL) and relational_id = '"+familyID+"'";
        Cursor cursor = null;
        ArrayList<String> womenList = new ArrayList<>();
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
    public static ArrayList<String> getAllMembersInHouseHold(String familyID){
        String query = "select first_name from ec_family_member where relational_id = '"+familyID+"'";
        Cursor cursor = null;
        ArrayList<String> memberList = new ArrayList<>();
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(0);
                memberList.add(name);
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
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                motherName = cursor.getString(0);
                cursor.close();
            }

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
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                motherName = cursor.getString(0);
                cursor.close();
            }

            return motherName;
        } catch (Exception e) {
            Timber.e(e);
        }
        return motherName;
    }
    public static String getMotherNameFromMemberTable(String motherEntityId){
        String query = "select first_name from ec_family_member where base_entity_id = '"+motherEntityId+"'";
        Cursor cursor = null;
        String motherName="";
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                motherName = cursor.getString(0);
                cursor.close();
            }

            return motherName;
        } catch (Exception e) {
            Timber.e(e);
        }
        return motherName;
    }
    public static HouseHoldInfo getHouseHoldInfo(String familyBaseEntityId){
        String query = "select first_name,unique_id,module_id,family_head,primary_caregiver from ec_family where base_entity_id = '"+familyBaseEntityId+"'";
        Cursor cursor = null;
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HouseHoldInfo houseHoldInfo = new HouseHoldInfo();
                houseHoldInfo.setHouseHoldName( cursor.getString(0));
                houseHoldInfo.setHouseHoldBaseEntityId(familyBaseEntityId);
                houseHoldInfo.setHouseHoldUniqueId(cursor.getString(1));
                houseHoldInfo.setModuleId(cursor.getString(2));
                houseHoldInfo.setHouseHoldHeadId(cursor.getString(3));
                houseHoldInfo.setPrimaryCaregiverId(cursor.getString(4));
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
        String query = "select module_id from ec_family where base_entity_id = '"+familyId+"'";
        Cursor cursor = null;
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
        columnList.add(familyTable + "." + DBConstants.KEY.VILLAGE_TOWN + " as " + ChildDBConstants.KEY.FAMILY_HOME_ADDRESS);
        columnList.add(familyTable + "." + HnppConstants.KEY.CLASTER);
        columnList.add(familyTable + "." + ChildDBConstants.PHONE_NUMBER);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.UNIQUE_ID);
        columnList.add(tableName + "." + DBConstants.KEY.GENDER);
        columnList.add(tableName + "." + DBConstants.KEY.DOB);
        columnList.add(tableName + "." + org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN);
        columnList.add(tableName + "." + ChildDBConstants.KEY.LAST_HOME_VISIT);
        columnList.add(tableName + "." + ChildDBConstants.KEY.VISIT_NOT_DONE);
        columnList.add(tableName + "." + ChildDBConstants.KEY.CHILD_BF_HR);
        columnList.add(tableName + "." + ChildDBConstants.KEY.CHILD_PHYSICAL_CHANGE);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_CERT);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_CERT_ISSUE_DATE);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_CERT_NUMBER);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_CERT_NOTIFIICATION);
        columnList.add(tableName + "." + ChildDBConstants.KEY.ILLNESS_DATE);
        columnList.add(tableName + "." + ChildDBConstants.KEY.ILLNESS_DESCRIPTION);
        columnList.add(tableName + "." + ChildDBConstants.KEY.DATE_CREATED);
        columnList.add(tableName + "." + ChildDBConstants.KEY.ILLNESS_ACTION);
        columnList.add(tableName + "." + ChildDBConstants.KEY.VACCINE_CARD);
        columnList.add(tableName + "." + HnppConstants.KEY.RELATION_WITH_HOUSEHOLD);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_MOTHER_NAME);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED);
        columnList.add(tableName + "." + HnppConstants.KEY.BLOOD_GROUP);
        columnList.add(tableName + "." + ChildDBConstants.KEY.MOTHER_ENTITY_ID);

        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_WEIGHT_TAKEN);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BIRTH_WEIGHT);
        columnList.add(tableName + "." + ChildDBConstants.KEY.CHLOROHEXADIN);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BREASTFEEDING_TIME);
        columnList.add(tableName + "." + ChildDBConstants.KEY.HEAD_BODY_COVERED);
        columnList.add(tableName + "." + ChildDBConstants.KEY.PHYSICALLY_CHALLENGED);
        columnList.add(tableName + "." + ChildDBConstants.KEY.BREAST_FEEDED);



        return columnList.toArray(new String[columnList.size()]);
    }
    public static String getMotherName(String motherEntityId, String relationId, String motherName){
        if(motherEntityId.isEmpty()) return motherName;
        if(motherEntityId.equalsIgnoreCase(relationId)) return motherName;
        String mName = getMotherNameFromMemberTable(motherEntityId);
        return TextUtils.isEmpty(mName)?motherName:mName;
    }

}
