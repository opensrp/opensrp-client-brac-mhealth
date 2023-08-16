package org.smartregister.unicef.dghs.interactor;

import android.database.Cursor;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;


import org.json.JSONObject;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.contract.FamilyRegisterInteractorCallBack;

import java.util.Collections;
import java.util.Date;

import timber.log.Timber;

public class HnppFamilyProfileInteractor extends org.smartregister.family.interactor.FamilyProfileInteractor {
    private String phoneNumber;
    protected AppExecutors appExecutors;

    public HnppFamilyProfileInteractor() {
        this(new AppExecutors());
    }
    @VisibleForTesting
    private HnppFamilyProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }
    public void saveRegistration(FamilyEventClient familyEventClient, String jsonString, boolean isEditMode, FamilyRegisterInteractorCallBack callBack) {
        Runnable runnable = new Runnable() {
            public void run() {
                String baseEntityId = saveRegistration(familyEventClient, jsonString, isEditMode);
                appExecutors.mainThread().execute(new Runnable() {
                    public void run() {
                        callBack.onRegistrationSaved(isEditMode,baseEntityId);
                    }
                });
            }
        };
        this.appExecutors.diskIO().execute(runnable);
    }
    private String saveRegistration(FamilyEventClient familyEventClient, String jsonString, boolean isEditMode) {
        try {
            Client baseClient = familyEventClient.getClient();
            Event baseEvent = familyEventClient.getEvent();
            JSONObject eventJson = null;
            JSONObject clientJson = null;
            if (baseClient != null) {
                clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
                if (isEditMode) {
                    JsonFormUtils.mergeAndSaveClient(this.getSyncHelper(), baseClient);
                } else {
                    this.getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                }
            }

            if (baseEvent != null) {
                eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                this.getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
            }

            String newOpenSRPId;
            if (isEditMode) {
                if (baseClient != null) {
                    newOpenSRPId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                    if (newOpenSRPId != null) {
                        newOpenSRPId.replace("-", "");
                        String currentOpenSRPId = JsonFormUtils.getString(jsonString, "current_opensrp_id").replace("-", "");
                        if (!newOpenSRPId.equals(currentOpenSRPId)) {
                            this.getUniqueIdRepository().open(currentOpenSRPId);
                        }
                    }
                }
            } else if (baseClient != null) {
                newOpenSRPId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                this.getUniqueIdRepository().close(newOpenSRPId);
            }

            if (baseClient != null || baseEvent != null) {
                newOpenSRPId = JsonFormUtils.getFieldValue(jsonString, "photo");
                JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), newOpenSRPId);
            }

            long lastSyncTimeStamp = this.getAllSharedPreferences().fetchLastUpdatedAtDate(0L);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            org.smartregister.domain.db.Event domainEvent = (org.smartregister.domain.db.Event)JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.db.Event.class);
            org.smartregister.domain.db.Client domainClient = (org.smartregister.domain.db.Client)JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.db.Client.class);
            this.processClient(Collections.singletonList(new EventClient(domainEvent, domainClient)));
            this.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
            return baseEvent.getBaseEntityId();
        } catch (Exception var13) {
            Timber.e(var13);
        }
        return "";

    }

    public void verifyHasPhone(String familyID, FamilyProfileExtendedContract.PresenterCallBack profilePresenter) {
        Runnable runnable = () -> {

            phoneNumber = getPhoneNumber(familyID);

            final boolean hasPhone = !TextUtils.isEmpty(phoneNumber) && !phoneNumber.equalsIgnoreCase("0");

            appExecutors.mainThread().execute(() -> profilePresenter.notifyHasPhone(hasPhone));
        };

        appExecutors.diskIO().execute(runnable);
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    private String getPhoneNumber(String familyId){
            String query = "select phone_number from ec_family where base_entity_id = '"+familyId+"'";
            Cursor cursor = null;
            String phone="";
            try {
                cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
                if(cursor !=null && cursor.getCount() >0){
                    cursor.moveToFirst();
                    phone = cursor.getString(0);
                    cursor.close();
                }

                return phone;
            } catch (Exception e) {
                Timber.e(e);
            }
            return phone;

    };
}
