package org.smartregister.brac.hnpp.model;

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
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.GuestMemberContract;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.repository.GuestMemberIdRepository;
import org.smartregister.brac.hnpp.repository.HnppChwRepository;
import org.smartregister.brac.hnpp.repository.HouseholdIdRepository;
import org.smartregister.brac.hnpp.utils.GuestMemberData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.dobEstimatedUpdateFromAge;
import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.processAttributesWithChoiceIDsForSave;
import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.updateFormSubmissionID;

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


    public void filterData(String query, String ssName){
        isFromSearch = false;
        searchedGuestMemberDataArrayList.clear();
        if(TextUtils.isEmpty(query) && TextUtils.isEmpty(ssName)) return;
        isFromSearch = true;
        for(GuestMemberData guestMemberData: guestMemberDataArrayList){
            if(!TextUtils.isEmpty(query) && !TextUtils.isEmpty(ssName)){
                String name = guestMemberData.getName().toLowerCase();
                String phoneNo = guestMemberData.getPhoneNo().toLowerCase();
                if((name.contains(query.toLowerCase()) || phoneNo.contains(query.toLowerCase()))&& guestMemberData.getSsName().equalsIgnoreCase(ssName)){
                    searchedGuestMemberDataArrayList.add(guestMemberData);
                }
            }
            else if(!TextUtils.isEmpty(ssName)){
                if(guestMemberData.getSsName().equalsIgnoreCase(ssName)){
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
            baseClient.setLastName(GUEST_MEMBER_REGISTRATION);
            baseClient.addAttribute("provider_id", CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, METADATA), formTag, entityId, getString(jsonForm, ENCOUNTER_TYPE), CoreConstants.TABLE_NAME.CHILD);
            tagSyncMetadata(getAllSharedPreferences(), baseEvent);
            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            String entity_id = baseClient.getBaseEntityId();
            updateFormSubmissionID(encounterType,entity_id,baseEvent);

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject jobkect = jsonObject.getJSONObject("step1");
            String villageIndex = jobkect.getString("village_index");
            String ssIndex = jobkect.getString("ss_index");
            SSLocations ss = SSLocationHelper.getInstance().getSsModels().get(Integer.parseInt(ssIndex)).locations.get(Integer.parseInt(villageIndex));
            JSONArray field = jobkect.getJSONArray(FIELDS);
            JSONObject villageIdObj = getFieldJSONObject(field, "village_id");
            String villageId = villageIdObj.getString(VALUE);
            try{
                String hhid = jobkect.getString( "hhid");
                GuestMemberIdRepository householdIdRepo = HnppApplication.getHNPPInstance().getGuestMemberIdRepository();
                householdIdRepo.close(villageId,hhid);
            }catch (Exception e){
                e.printStackTrace();

            }
            List<Address> listAddress = new ArrayList<>();
            listAddress.add(SSLocationHelper.getInstance().getSSAddress(ss));
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
        String query =  "select ec_guest_member.base_entity_id,ec_guest_member.unique_id,ec_guest_member.first_name,ec_guest_member.ss_name,ec_guest_member.dob,ec_guest_member.gender,ec_guest_member.phone_number from ec_guest_member " +
         " where ec_guest_member.date_removed is null group by ec_guest_member.unique_id order by ec_guest_member.last_interacted_with desc ";
        Cursor cursor = null;
        try{
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    GuestMemberData guestMemberData = new GuestMemberData();
                    guestMemberData.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                    guestMemberData.setMemberId(cursor.getString(cursor.getColumnIndex("unique_id")));
                    guestMemberData.setName(cursor.getString(cursor.getColumnIndex("first_name")));
                    guestMemberData.setSsName(cursor.getString(cursor.getColumnIndex("ss_name")));
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
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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

    @Override
    public Context getContext() {
        return context;
    }
}
