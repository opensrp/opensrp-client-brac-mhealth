package org.smartregister.chw.core.interactor;

import android.support.annotation.VisibleForTesting;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.CoreChildRegisterContract;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.UniqueId;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.Date;

import timber.log.Timber;

/**
 * Created by keyman 12/11/2018.
 */
public class CoreChildRegisterInteractor implements CoreChildRegisterContract.Interactor {

    public static final String TAG = CoreChildRegisterInteractor.class.getName();
    protected AppExecutors appExecutors;


    public CoreChildRegisterInteractor() {
        this(new AppExecutors());
    }

    @VisibleForTesting
    CoreChildRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        //TODO set presenter or model to null
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final CoreChildRegisterContract.InteractorCallBack callBack, final String familyId) {

        Runnable runnable = () -> {
            UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
            final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
            appExecutors.mainThread().execute(() -> {
                if (StringUtils.isBlank(entityId)) {
                    callBack.onNoUniqueId();
                } else {
                    callBack.onUniqueIdFetched(triple, entityId, familyId);
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode, final CoreChildRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            saveRegistration(pair, jsonString, isEditMode);
            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved(isEditMode));
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void removeChildFromRegister(final String closeFormJsonString, final String providerId) {
        Runnable runnable = () -> {
            //TODO add functionality to remove family from register
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void saveRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode) {

        try {
            Thread.sleep(2000);
            Client baseClient = pair.first;
            Event baseEvent = pair.second;

            if (baseClient != null) {
                JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
                if (isEditMode) {
                    JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
                } else {
                    getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                }
            }

            if (baseEvent != null) {
                JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
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

                    //mark OPENSRP ID as used
                    getUniqueIdRepository().close(opensrpId);
                }
            }

            if (baseClient != null || baseEvent != null) {
                String imageLocation = JsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO);
                JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public ECSyncHelper getSyncHelper() {
        return FamilyLibrary.getInstance().getEcSyncHelper();
    }

    public AllSharedPreferences getAllSharedPreferences() {
        return Utils.context().allSharedPreferences();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }

    public UniqueIdRepository getUniqueIdRepository() {
        return FamilyLibrary.getInstance().getUniqueIdRepository();
    }

    public enum type {SAVED, UPDATED}
}
