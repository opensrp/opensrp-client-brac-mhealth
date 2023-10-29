package org.smartregister.unicef.mis.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.GuestMemberContract;
import org.smartregister.unicef.mis.location.HALocationHelper;
import org.smartregister.unicef.mis.utils.GuestMemberData;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.smartregister.unicef.mis.utils.HnppJsonFormUtils.dobEstimatedUpdateFromAge;
import static org.smartregister.unicef.mis.utils.HnppJsonFormUtils.processAttributesWithChoiceIDsForSave;
import static org.smartregister.unicef.mis.utils.HnppJsonFormUtils.updateFormSubmissionID;

public class GuestMemberModel extends JsonFormUtils implements GuestMemberContract.Model {

    private Context context;
    private ArrayList<GuestMemberData> guestMemberDataArrayList;
    private ArrayList<GuestMemberData> searchedGuestMemberDataArrayList;
    private boolean isFromSearch;
    public GuestMemberModel(Context context){
        this.context = context;
        this.guestMemberDataArrayList = new ArrayList<>();
        this.searchedGuestMemberDataArrayList = new ArrayList<>();
    }


    public void filterData(String query){
        isFromSearch = false;
        searchedGuestMemberDataArrayList.clear();
        if(TextUtils.isEmpty(query)) return;
        isFromSearch = true;
        for(GuestMemberData guestMemberData: guestMemberDataArrayList){
            if(!TextUtils.isEmpty(query) ){
                String name = guestMemberData.getName().toLowerCase();
                String phoneNo = guestMemberData.getPhoneNo().toLowerCase();
                if((name.contains(query.toLowerCase()) || phoneNo.contains(query.toLowerCase()))){
                    searchedGuestMemberDataArrayList.add(guestMemberData);
                }
            }
            else if(!TextUtils.isEmpty(query)){
               String name = guestMemberData.getName().toLowerCase();
                String phoneNo = guestMemberData.getPhoneNo().toLowerCase();
                Log.v("SEARCH_GUEST","name:"+name+":query:"+query);
                if(name.contains(query.toLowerCase()) || phoneNo.contains(query.toLowerCase())){
                    searchedGuestMemberDataArrayList.add(guestMemberData);
                }
            }

        }

    }

    @Override
    public ArrayList<GuestMemberData> getData() {
        return isFromSearch?searchedGuestMemberDataArrayList:guestMemberDataArrayList;
    }

    @Override
    public Pair<Client, Event> processRegistration(String jsonString) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }
            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();
            String entityId = getString(jsonForm, ENTITY_ID);
            if (StringUtils.isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }
            lastInteractedWith(fields);
            dobEstimatedUpdateFromAge(fields);
            processAttributesWithChoiceIDsForSave(fields);
            FormTag formTag = formTag(getAllSharedPreferences());
            formTag.appVersionName = BuildConfig.VERSION_NAME;
            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields,formTag , entityId);
            baseClient.addAttribute("provider_id", CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject jobkect = jsonObject.getJSONObject("step1");
            JSONArray field = jobkect.getJSONArray(FIELDS);
            JSONObject divisionIdObj = getFieldJSONObject(field, "division_id");
            String divId = divisionIdObj.getString("value");
            JSONObject districtIdObj = getFieldJSONObject(field, "district_id");
            String disId = districtIdObj.getString("value");
            JSONObject upozilaIdObj = getFieldJSONObject(field, "upazila_id");
            String upozilaId = upozilaIdObj.optString("value");

            baseClient.addAttribute("division_id", divId+"");
            baseClient.addAttribute("district_id", disId+"");
            baseClient.addAttribute("upazila_id", upozilaId+"");
            baseClient.addAttribute("ooc","yes");
            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, METADATA), formTag, entityId, getString(jsonForm, ENCOUNTER_TYPE), CoreConstants.TABLE_NAME.CHILD);
            tagSyncMetadata(getAllSharedPreferences(), baseEvent);
            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            String entity_id = baseClient.getBaseEntityId();
            updateFormSubmissionID(encounterType,entity_id,baseEvent);
            HALocationHelper.getInstance().addOOCIdentifier(divId,disId,upozilaId,baseEvent);
            JSONObject divisionObj = getFieldJSONObject(field, "division_per");
            String divName = divisionObj.getString("value");
            JSONObject districtObj = getFieldJSONObject(field, "district_per");
            String disName = districtObj.getString("value");
            JSONObject upozilaObj = getFieldJSONObject(field, "upazila_per");
            String upozila = upozilaObj.optString("value");
            List<Address> listAddress = new ArrayList<>();
            Address address = new Address();
            address.setAddressType("usual_residence");
            HashMap<String,String> addressMap = new HashMap<>();
            addressMap.put("address2", upozila);
            addressMap.put("division_id", divId);
            addressMap.put("district_id", disId);
            addressMap.put("upazila_id", upozilaId);
            address.setAddressFields(addressMap);
            address.setStateProvince(divName);
            address.setCountyDistrict(disName);
            listAddress.add(address);
            baseClient.setAddresses(listAddress);

            return Pair.create(baseClient, baseEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void saveRegistration(Pair<Client, Event> pair) {
        try{

                Client baseClient = pair.first;
                Event baseEvent = pair.second;

                if (baseClient != null) {
                    JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));

                        getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);

                }

                if (baseEvent != null) {
                    JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                    getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
                }


                long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);
                getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
                getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }


    }
    public ECSyncHelper getSyncHelper() {
        return FamilyLibrary.getInstance().getEcSyncHelper();
    }


    public AllSharedPreferences getAllSharedPreferences() {
        return org.smartregister.chw.core.utils.Utils.context().allSharedPreferences();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }

    @Override
    public void loadData() {
        guestMemberDataArrayList.clear();
        String query =  "select * from  ec_guest_member order by last_interacted_with desc ";
        Cursor cursor = null;
        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    GuestMemberData guestMemberData = new GuestMemberData();
                    guestMemberData.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                    String shrId = cursor.getString(cursor.getColumnIndex("shr_id"));
                    if(!TextUtils.isEmpty(shrId)){
                        guestMemberData.setMemberId(shrId);
                    }else{
                        guestMemberData.setMemberId(cursor.getString(cursor.getColumnIndex("unique_id")));
                    }
                    String firstName = cursor.getString(cursor.getColumnIndex("first_name"));
                    String lastName = cursor.getString(cursor.getColumnIndex("last_name"));
                    guestMemberData.setName(firstName+" "+lastName);
                    guestMemberData.setDob(cursor.getString(cursor.getColumnIndex("dob")));
                    guestMemberData.setGender(cursor.getString(cursor.getColumnIndex("gender")));
                    guestMemberData.setPhoneNo(cursor.getString(cursor.getColumnIndex("phone_number")));
                    guestMemberData.setLastSubmissionDate(getLatestVisitDate(guestMemberData.getBaseEntityId()));

                    guestMemberDataArrayList.add(guestMemberData);

                    cursor.moveToNext();
                }
            }
        }catch (Exception e){

        }finally {
            if(cursor!=null) cursor.close();
        }


    }
    private long getLatestVisitDate(String baseEntityId){
        String query = "select max(visit_date) as visit_date from ec_visit_log where base_entity_id ='"+baseEntityId+"'";
        Cursor cursor = null;
        long latestVisitDate = 0;
        // try {
        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    latestVisitDate = (cursor.getLong(cursor.getColumnIndex("visit_date")));
                    cursor.moveToNext();
                }

            }
        }catch (Exception e){

        }finally {
            if(cursor!=null) cursor.close();
        }

        return latestVisitDate;
    }
    public ArrayList<String> getIdsWithoutSHRIds(){
        ArrayList<String> baseEntityIds = new ArrayList<>();
        String query = "select base_entity_id from ec_guest_member where shr_id is null";
        Cursor cursor = null;
        // try {
        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String baseEntityId = (cursor.getString(cursor.getColumnIndex("base_entity_id")));
                    baseEntityIds.add(baseEntityId);
                    cursor.moveToNext();
                }

            }
        }catch (Exception e){

        }finally {
            if(cursor!=null) cursor.close();
        }
        return baseEntityIds;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
