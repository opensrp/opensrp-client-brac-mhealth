package org.smartregister.unicef.mis.interactor;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.Photo;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.dao.AlertDao;
import org.smartregister.unicef.mis.enums.ImmunizationState;
import org.smartregister.unicef.mis.utils.ChwServiceSchedule;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildService;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.VaccineScheduleUtil;
import org.smartregister.util.ImageUtils;
import org.smartregister.view.LocationPickerView;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HnppChildProfileInteractor implements CoreChildProfileContract.Interactor {
    public static final String TAG = HnppChildProfileInteractor.class.getName();
    protected AppExecutors appExecutors;
    protected CommonPersonObjectClient pClient;
    private Map<String, Date> vaccineList = new LinkedHashMap<>();
    private String childBaseEntityId;

    public HnppChildProfileInteractor() {
        this(new AppExecutors());
    }

    @VisibleForTesting
    HnppChildProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }
    public Map<String, Date> getVaccineList() {
        return vaccineList;
    }

    public void setVaccineList(Map<String, Date> vaccineList) {
        this.vaccineList = vaccineList;
    }
    @Override
    public void updateChildCommonPerson(String baseEntityId) {

        String query = HnppDBUtils.mainSelect(CoreConstants.TABLE_NAME.CHILD, CoreConstants.TABLE_NAME.FAMILY, CoreConstants.TABLE_NAME.FAMILY_MEMBER, baseEntityId);

        Cursor cursor = null;
        try {
            cursor = getCommonRepository(CoreConstants.TABLE_NAME.CHILD).rawCustomQueryForAdapter(query);
            if (cursor != null && cursor.moveToFirst()) {
                CommonPersonObject personObject = getCommonRepository(CoreConstants.TABLE_NAME.CHILD).readAllcommonforCursorAdapter(cursor);
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
    }
    /**
     * Refreshes family view based on the child id
     *
     * @param baseEntityId
     * @param isForEdit
     * @param callback
     */
    @Override
    public void refreshProfileView(final String baseEntityId, final boolean isForEdit, final CoreChildProfileContract.InteractorCallBack callback) {
        Runnable runnable = () -> {
            String query = HnppDBUtils.mainSelect(CoreConstants.TABLE_NAME.CHILD, CoreConstants.TABLE_NAME.FAMILY, CoreConstants.TABLE_NAME.FAMILY_MEMBER, baseEntityId);

            Cursor cursor = null;
            try {
                cursor = getCommonRepository(CoreConstants.TABLE_NAME.CHILD).rawCustomQueryForAdapter(query);
                if (cursor != null && cursor.moveToFirst()) {
                    CommonPersonObject personObject = getCommonRepository(CoreConstants.TABLE_NAME.CHILD).readAllcommonforCursorAdapter(cursor);
                    pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                            personObject.getDetails(), "");
                    pClient.setColumnmaps(personObject.getColumnmaps());
                    final String familyId = Utils.getValue(pClient.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
                    CommonPersonObjectClient client = null;
                    if(!TextUtils.isEmpty(familyId)){
                        final CommonPersonObject familyPersonObject = getCommonRepository(Utils.metadata().familyRegister.tableName).findByBaseEntityId(familyId);
                        if(familyPersonObject !=null){
                            client = new CommonPersonObjectClient(familyPersonObject.getCaseId(), familyPersonObject.getDetails(), "");
                            client.setColumnmaps(familyPersonObject.getColumnmaps());
                        }

                    }
                    String primaryCaregiverID = null,familyHeadID = null,familyFirstName = null,familyLastName = null;
                    if(client!=null){
                        primaryCaregiverID = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.PRIMARY_CAREGIVER, false);
                        familyHeadID = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.FAMILY_HEAD, false);
                        familyFirstName = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.FIRST_NAME, false);
                        familyLastName = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.LAST_NAME, false);

                    }


                    String finalFamilyHeadID = familyHeadID;
                    String finalPrimaryCaregiverID = primaryCaregiverID;
                    String finalFamilyFirstName = familyFirstName;
                    String finalFamilyLastName = familyLastName;
                    appExecutors.mainThread().execute(() -> {

                        callback.setFamilyHeadID(finalFamilyHeadID);
                        callback.setFamilyID(familyId);
                        callback.setPrimaryCareGiverID(finalPrimaryCaregiverID);
                        callback.setFamilyName(finalFamilyFirstName +" "+ finalFamilyLastName);

                        if (isForEdit) {
                            callback.startFormForEdit("", pClient);
                        } else {
                            callback.refreshProfileTopSection(pClient);
                        }
                    });
                }
            } catch (Exception ex) {
                Timber.e(ex, "CoreChildProfileInteractor --> refreshProfileView");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }


        };

        appExecutors.diskIO().execute(runnable);
    }
    @Override
    public void refreshUpcomingServiceAndFamilyDue(Context context, String familyId, String baseEntityId, final CoreChildProfileContract.InteractorCallBack callback) {
        if (getpClient() == null) {
            return;
        }
        //updateUpcomingServices(callback,context);

    }
    private void updateUpcomingServices(final CoreChildProfileContract.InteractorCallBack callback, Context context) {
        updateUpcomingServices(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CoreChildService>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //// TODO: 15/08/19
                    }

                    @Override
                    public void onNext(CoreChildService childService) {
                        callback.updateChildService(childService);
                        callback.hideProgressBar();

                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.hideProgressBar();

                    }

                    @Override
                    public void onComplete() {
                        //// TODO: 15/08/19
                    }
                });
    }
    public Observable<CoreChildService> updateUpcomingServices(Context context) {
        return Observable.create(e -> {
            // load all the services pending
            String dobString = org.smartregister.util.Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.DOB, false);
            DateTime dob = new DateTime(Utils.dobStringToDate(dobString));

            VaccineScheduleUtil.updateOfflineAlerts(childBaseEntityId, dob, CoreConstants.SERVICE_GROUPS.CHILD);
            ChwServiceSchedule.updateOfflineAlerts(childBaseEntityId, dob, CoreConstants.SERVICE_GROUPS.CHILD);

            List<Vaccine> vaccines = HnppApplication.getInstance().vaccineRepository().findByEntityId(childBaseEntityId); // add vaccines given to the user
            Map<String, Date> receivedVaccines = VaccinatorUtils.receivedVaccines(vaccines);
            setVaccineList(receivedVaccines);
            List<Alert> alertList = AlertDao.getActiveAlerts(childBaseEntityId);
            Alert alert = (alertList.size() > 0) ? alertList.get(0) : null;

            if (alert != null) {
                CoreChildService childService = new CoreChildService();
                childService.setServiceName(alert.scheduleName());
                childService.setServiceDate(alert.startDate());
                childService.setServiceStatus(getImmunizationStateFromAlert(alert.status()).name());
                e.onNext(childService);
            } else {
                e.onNext(null);
            }
        });
    }

    private ImmunizationState getImmunizationStateFromAlert(AlertStatus alertStatus) {
        switch (alertStatus) {
            case normal:
                return ImmunizationState.DUE;
            case urgent:
                return ImmunizationState.OVERDUE;
            case upcoming:
                return ImmunizationState.UPCOMING;
            case expired:
                return ImmunizationState.EXPIRED;
            default:
                return ImmunizationState.NO_ALERT;
        }
    }

    public CommonPersonObjectClient getpClient() {
        return pClient;
    }

    public void setpClient(CommonPersonObjectClient pClient) {
        this.pClient = pClient;
    }
    public CommonRepository getCommonRepository(String tableName) {
        return Utils.context().commonrepository(tableName);
    }
    @Override
    public void updateVisitNotDone(long value, CoreChildProfileContract.InteractorCallBack callback) {
        //// TODO: 02/08/19
    }

    @Override
    public void refreshChildVisitBar(Context context, String baseEntityId, CoreChildProfileContract.InteractorCallBack callback) {
        //// TODO: 02/08/19
    }
    @Override
    public void getClientTasks(String planId, String baseEntityId, CoreChildProfileContract.InteractorCallBack callback) {
       // Set<Task> taskList = HnppApplication.getInstance().getTaskRepository().getTasksByEntityAndStatus(planId, baseEntityId, Task.TaskStatus.READY);
       // callback.setClientTasks(taskList);
    }
    @Override
    public void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode, final CoreChildProfileContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            saveRegistration(pair, jsonString, isEditMode);
            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved(isEditMode));
        };

        appExecutors.diskIO().execute(runnable);
    }

    public void saveRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode) {

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

            if (!isEditMode && baseClient != null) {
                String opensrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                //mark OPENSRP ID as used
                getUniqueIdRepository().close(opensrpId);
            }

            if (baseClient != null || baseEvent != null) {
                String imageLocation = JsonFormUtils.getFieldValue(jsonString, org.smartregister.family.util.Constants.KEY.PHOTO);
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

    public UniqueIdRepository getUniqueIdRepository() {
        return FamilyLibrary.getInstance().getUniqueIdRepository();
    }

    public AllSharedPreferences getAllSharedPreferences() {
        return Utils.context().allSharedPreferences();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }
    @Override
    public JSONObject getAutoPopulatedJsonEditFormString(String formName, String title, Context context, CommonPersonObjectClient client) {
        try {
            JSONObject form = HnppJsonFormUtils.getJsonObject(formName);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            if (form != null) {
                form.put(JsonFormUtils.ENTITY_ID, client.getCaseId());
                form.put(JsonFormUtils.ENCOUNTER_TYPE, CoreConstants.EventType.UPDATE_CHILD_REGISTRATION);

                JSONObject metadata = form.getJSONObject(JsonFormUtils.METADATA);
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                metadata.put(JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

                form.put(JsonFormUtils.CURRENT_OPENSRP_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));

                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);

                if (StringUtils.isNotBlank(title)) {
                    stepOne.put(HnppJsonFormUtils.TITLE, title);
                }
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processPopulatableFields(client, jsonObject, jsonArray);

                }

                return form;
            }
        } catch (Exception e) {
            Timber.e(e, "CoreChildProfileInteractor --> getAutoPopulatedJsonEditFormString");
        }

        return null;
    }

    @Override
    public void processBackGroundEvent(CoreChildProfileContract.InteractorCallBack callback) {
        //todo
    }

    @Override
    public void createSickChildEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception {
      //  CoreReferralUtils.createReferralEvent(allSharedPreferences, jsonString, CoreConstants.TABLE_NAME.CHILD_REFERRAL, getChildBaseEntityId());
    }

    @Override
    public String getChildBaseEntityId() {
        return childBaseEntityId;
    }

    @Override
    public void setChildBaseEntityId(String childBaseEntityId) {
        this.childBaseEntityId = childBaseEntityId;
    }
    public void processPopulatableFields(CommonPersonObjectClient client, JSONObject jsonObject, JSONArray jsonArray) throws JSONException {

        switch (jsonObject.getString(JsonFormUtils.KEY).toLowerCase()) {
            case Constants.JSON_FORM_KEY.DOB_UNKNOWN:
                jsonObject.put(JsonFormUtils.READ_ONLY, false);
                JSONObject optionsObject = jsonObject.getJSONArray(org.smartregister.family.util.Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
                optionsObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN, false));
                break;
            case "age": {
                getAge(client, jsonObject);
            }
            break;
            case DBConstants.KEY.DOB:
                String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                getDob(jsonObject, dobString);
                break;
            case org.smartregister.family.util.Constants.KEY.PHOTO:
                getPhoto(client, jsonObject);
                break;
            case DBConstants.KEY.UNIQUE_ID:
                String uniqueId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
                jsonObject.put(JsonFormUtils.VALUE, uniqueId.replace("-", ""));
                break;
            case CoreConstants.JsonAssets.INSURANCE_PROVIDER:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.INSURANCE_PROVIDER, false));
                break;
            case CoreConstants.JsonAssets.INSURANCE_PROVIDER_NUMBER:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.INSURANCE_PROVIDER_NUMBER, false));
                break;
            case CoreConstants.JsonAssets.INSURANCE_PROVIDER_OTHER:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.INSURANCE_PROVIDER_OTHER, false));
                break;
            case CoreConstants.JsonAssets.DISABILITIES:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.CHILD_PHYSICAL_CHANGE, false));
                break;
            case CoreConstants.JsonAssets.DISABILITY_TYPE:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.TYPE_OF_DISABILITY, false));
                break;
            case CoreConstants.JsonAssets.BIRTH_CERT_AVAILABLE:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.BIRTH_CERT, false));
                break;
            case CoreConstants.JsonAssets.BIRTH_REGIST_NUMBER:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.BIRTH_CERT_NUMBER, false));
                break;
            case CoreConstants.JsonAssets.RHC_CARD:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.RHC_CARD, false));
                break;
            case CoreConstants.JsonAssets.NUTRITION_STATUS:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.NUTRITION_STATUS, false));
                break;
            case DBConstants.KEY.GPS:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.GPS, false));
                break;
            default:
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), jsonObject.getString(JsonFormUtils.KEY), false));
                break;

        }
    }
    private void getAge(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
        dobString = org.smartregister.family.util.Utils.getDuration(dobString);
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";
        jsonObject.put(JsonFormUtils.VALUE, Integer.valueOf(dobString));
    }

    private void getDob(JSONObject jsonObject, String dobString) throws JSONException {
        if (StringUtils.isNotBlank(dobString)) {
            Date dob = Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(JsonFormUtils.VALUE, JsonFormUtils.dd_MM_yyyy.format(dob));
            }
        }
    }
    private void getPhoto(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        Photo photo = ImageUtils.profilePhotoByClientID(client.getCaseId(), Utils.getProfileImageResourceIDentifier());
        if (StringUtils.isNotBlank(photo.getFilePath())) {
            jsonObject.put(JsonFormUtils.VALUE, photo.getFilePath());
        }
    }
    @Override
    public void onDestroy(boolean isChangingConfiguration) {        //todo
    }
}