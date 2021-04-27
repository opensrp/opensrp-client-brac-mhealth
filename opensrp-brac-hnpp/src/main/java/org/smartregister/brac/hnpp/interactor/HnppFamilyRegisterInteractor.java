package org.smartregister.brac.hnpp.interactor;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.family.contract.FamilyRegisterContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HnppFamilyRegisterInteractor extends org.smartregister.family.interactor.FamilyRegisterInteractor {
    @Override
    public void getNextUniqueId(Triple<String, String, String> triple, FamilyRegisterContract.InteractorCallBack callBack) {
        //no need to handle this
    }

    @Override
    public void saveRegistration(List<FamilyEventClient> familyEventClientList, String jsonString, boolean isEditMode, FamilyRegisterContract.InteractorCallBack callBack) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

               boolean isSaved = saveRegistration(familyEventClientList, jsonString, isEditMode);
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onRegistrationSaved(isEditMode,isSaved,familyEventClientList);
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }
    private boolean saveRegistration(List<FamilyEventClient> familyEventClientList, String jsonString, boolean isEditMode) {
        boolean isSaved = false;
        try {


            //status change in householdidtable;

//            String address =
            List<EventClient> eventClientList = new ArrayList<>();
            for (int i = 0; i < familyEventClientList.size(); i++) {
                FamilyEventClient familyEventClient = familyEventClientList.get(i);
                Client baseClient = familyEventClient.getClient();
                Event baseEvent = familyEventClient.getEvent();
                JSONObject eventJson = null;
                JSONObject clientJson = null;

                if (baseClient != null) {
                    clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
                    if (isEditMode) {
                        JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
                    } else {
                        getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                    }
                }

                if (baseEvent != null) {
                    eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                    getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
                }

                if (isEditMode) {
                    // Unassign current OPENSRP ID
                    if (baseClient != null) {
                        String newOpenSRPId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey).replace("-", "");
                        String currentOpenSRPId = JsonFormUtils.getString(jsonString, JsonFormUtils.CURRENT_OPENSRP_ID).replace("-", "");
                        if (!newOpenSRPId.equals(currentOpenSRPId)) {
                            //OPENSRP ID was changed
                            getUniqueIdRepository().open(currentOpenSRPId);
                        }
                    }

                } else {
                    if (baseClient != null) {
                        String opensrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                        opensrpId = opensrpId.replace(Constants.IDENTIFIER.FAMILY_SUFFIX,"")
                                .replace(HnppConstants.IDENTIFIER.FAMILY_TEXT,"");
                        Log.v("HHID","after save>>"+opensrpId);
                        //if (StringUtils.isNotBlank(opensrpId) && !opensrpId.contains(Constants.IDENTIFIER.FAMILY_SUFFIX)) {
                            //mark OPENSRP ID as used
                            getUniqueIdRepository().close(opensrpId);
                        //}
                    }
                }

//                if (baseClient != null || baseEvent != null) {
//                    String imageLocation = null;
//                    if (i == 0) {
//                        imageLocation = JsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO);
//                    } else if (i == 1) {
//                        imageLocation = JsonFormUtils.getFieldValue(jsonString, JsonFormUtils.STEP2, Constants.KEY.PHOTO);
//                    }
//
//                    if (StringUtils.isNotBlank(imageLocation)) {
//                        JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
//                    }
//                }
                org.smartregister.domain.Event domainEvent = JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.Event.class);
                org.smartregister.domain.Client domainClient = JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.Client.class);
                eventClientList.add(new EventClient(domainEvent, domainClient));
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);

            processClient(eventClientList);
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
            isSaved = true;
        } catch (Exception e) {
            e.printStackTrace();
            isSaved = false;
        }
        return isSaved;
    }
}
