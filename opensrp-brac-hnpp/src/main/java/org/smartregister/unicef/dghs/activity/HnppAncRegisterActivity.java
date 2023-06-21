package org.smartregister.unicef.dghs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.activity.BaseAncRegisterActivity;
import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.fragment.HnppAncRegisterFragment;
import org.smartregister.unicef.dghs.listener.HnppFamilyBottomNavListener;
import org.smartregister.unicef.dghs.location.HALocation;
import org.smartregister.unicef.dghs.location.HALocationHelper;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.unicef.dghs.repository.HnppVisitLogRepository;
import org.smartregister.unicef.dghs.sync.FormParser;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.interactor.BaseAncRegisterInteractor;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static org.smartregister.unicef.dghs.utils.HnppJsonFormUtils.BLOCK_ID;


public class HnppAncRegisterActivity extends BaseAncRegisterActivity {

    private HnppVisitLogRepository visitLogRepository;
    private static String motherName;
    private static String baseEntityId;
    private static double latitude;
    private static double longitude;

    protected static String phone_number;
    protected static String form_name;
    protected static String unique_id;
    protected static String familyBaseEntityId;
    protected static String familyName;

    public static void startHnppAncRegisterActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name, String moName, double lat, double longi) {
        Intent intent = new Intent(activity, org.smartregister.unicef.dghs.activity.HnppAncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        motherName = moName;
        latitude = lat;
        longitude = longi;
        baseEntityId = memberBaseEntityID;
        unique_id = uniqueId;
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    public String getRegistrationForm() {
        return form_name;
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter()
                    .setSelectedView(CoreConstants.DrawerMenu.ANC);
        }
    }
    public static String getFormTable() {
        if (form_name != null && form_name.equals(HnppConstants.JSON_FORMS.ANC_FORM)) {
            return CoreConstants.TABLE_NAME.ANC_MEMBER;
        }
        return CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME;
    }
    @Override
    public void onBackPressed() {
        Fragment fragment = findFragmentByPosition(currentPage);
        if (fragment instanceof BaseRegisterFragment) {
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients);
            BaseRegisterFragment registerFragment = (BaseRegisterFragment) fragment;
            if (registerFragment.onBackPressed()) {
                return;
            }
        }

        backToHomeScreen();
        setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients);
    }

    @Override
    protected void initializePresenter() {
        presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new BaseAncRegisterInteractor());
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {

        try {
            visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
            HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,familyBaseEntityId);
            if(form_name.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC_FORM) || form_name.equalsIgnoreCase(HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME) ){
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
            }
            Form form = new Form();
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            form.setWizard(false);
            Intent intent = new Intent(this, HnppAncJsonFormActivity.class);

            JSONObject stepOne = jsonForm.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            updateFormField(jsonArray, DBConstants.KEY.UNIQUE_ID, unique_id);
            updateFormField(jsonArray, DBConstants.KEY.TEMP_UNIQUE_ID, unique_id);
            updateFormField(jsonArray, "temp_name", "Baby of "+motherName+"");
            updateMinDate(jsonArray);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAM_NAME, familyName);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER, phone_number);
            updateFormField(jsonArray, org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, familyBaseEntityId);

            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
//            updateWithSSLocation();

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void updateMinDate(JSONArray jsonArray) throws JSONException{
       try{
           JSONObject min_date = CoreJsonFormUtils.getFieldJSONObject(jsonArray, "delivery_date");
           String lmp = FormApplicability.getLmp(baseEntityId);
           //int days = CoreJsonFormUtils.getDayFromDate(lmp);
           int days = getDaysFromDate(lmp);
           min_date.put("min_date", "today-" + days + "d");
       }catch (Exception e){

       }

    }

    private int getDaysFromDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Date past,now;
        int days=0;
        try {
            past = format.parse(date);
            now = new Date();
            days = (int) TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }
    private void updateEncounterType(JSONObject jsonForm) {
        try {
            jsonForm.put("encounter_type", HnppConstants.EVENT_TYPE.UPDATE_ANC_REGISTRATION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateFormField(JSONArray formFieldArrays, String formFeildKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFeildKey);
            if (formObject != null) {
                try {
                    formObject.remove(org.smartregister.util.JsonFormUtils.VALUE);
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
        bottomNavigationView.setOnNavigationItemSelectedListener(new HnppFamilyBottomNavListener(this, bottomNavigationView));
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppAncRegisterFragment();
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        this.finish();
    }
    public void backToHomeScreen() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(HnppConstants.KEY_NEED_TO_OPEN,true);
        startActivity(intent);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null){
            finish();
        }
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_GET_JSON) {
            showProgressDialog(R.string.please_wait);
            executeQuery(data)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String eventType) {
                            hideProgressDialog();
                            if (eventType.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME)) {

                                // HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
                                if(!familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                                    HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppAncRegisterActivity.this, baseEntityId);
                                }
                            }else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_REGISTRATION)){
                                // HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
                                HnppConstants.isViewRefresh = true;
                                refreshList(FetchStatus.fetched);

                            }
                            if(familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                                Intent intent = new Intent();
                                intent.putExtra("event_type",HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

    }
    Observable<String> executeQuery( Intent data){
        return  Observable.create(e->{
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);

            String eventType = processVisitFormAndSave(jsonString);

            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            //database.execSQL(sql1);
            String sql = "UPDATE ec_anc_register SET is_closed = 1 WHERE ec_anc_register.base_entity_id IN " +
                    "(select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = 0) ";

            database.execSQL(sql);
            e.onNext(eventType);
        });
    }
    private String processVisitFormAndSave(String jsonString){
        String encounter_type = "";
        long startTime = System.currentTimeMillis();
        try {
            JSONObject form = new JSONObject(jsonString);
            JSONObject step1 = form.getJSONObject(STEP1);
            encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);
            if(encounter_type.equalsIgnoreCase(CoreConstants.EventType.PREGNANCY_OUTCOME)){
                if(!familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                    JSONArray fields = step1.getJSONArray(FIELDS);
                    updateUniqueId(fields);
                }
                try{

                    saveRegistration(form.toString(), HnppConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME);
                }catch (Exception e){
                    e.printStackTrace();

                }



            }
            else if (encounter_type.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_REGISTRATION)) {
                try{
                    saveRegistration(form.toString(), HnppConstants.TABLE_NAME.ANC_MEMBER);
                }catch (Exception e){

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return encounter_type;
    }

    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = org.smartregister.chw.anc.util.JsonFormUtils.getFormAsJson(formName);
        org.smartregister.chw.anc.util.JsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }
    private void saveRegistration(final String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);
        JSONObject form = new JSONObject(jsonString);
        JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
        String blockId = org.smartregister.util.JsonFormUtils.getFieldValue(fields,BLOCK_ID);
        Log.v("saveRegistration","saveRegistration>>>blockId:"+blockId);
        HALocation selectedLocation = HnppApplication.getHALocationRepository().getLocationByBlock(blockId);
        baseEvent.setIdentifiers(HALocationHelper.getInstance().getGeoIdentifier(selectedLocation));
        String visitID ="";
        if(!TextUtils.isEmpty(baseEvent.getEventId())){
            visitID = baseEvent.getEventId();
        }else{
            visitID = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
        }
        Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
        visit.setPreProcessedJson(new Gson().toJson(baseEvent));
        try{
           // visit.setParentVisitID(visitRepository().getParentVisitEventID(visit.getBaseEntityId(), HnppConstants.EVENT_TYPE.SS_INFO, visit.getDate()));
            AncLibrary.getInstance().visitRepository().addVisit(visit);
        }catch (Exception e){
            e.printStackTrace();
        }
        FormParser.processVisitLog(visit);
        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    private void processChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId) {
        try {
            Client pncChild = org.smartregister.util.JsonFormUtils.createBaseClient(fields, org.smartregister.chw.anc.util.JsonFormUtils.formTag(allSharedPreferences), entityId);
            if(familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                pncChild.setLastName(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
            }
            pncChild.addRelationship(Constants.RELATIONSHIP.FAMILY, familyBaseEntityId);
            pncChild.addRelationship(Constants.RELATIONSHIP.MOTHER, motherBaseId);
            JSONObject clientjson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(pncChild));
            EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
            SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
            JSONObject dsasd = eventClientRepository.getClient(db, familyBaseEntityId);
            pncChild.setAddresses(updateWithSSLocation(dsasd));
            clientjson.put("addresses",dsasd.getJSONArray("addresses"));
            AncLibrary.getInstance().getUniqueIdRepository().close(pncChild.getIdentifier(Constants.JSON_FORM_EXTRA.OPENSPR_ID));

            NCUtils.getSyncHelper().addClient(pncChild.getBaseEntityId(), clientjson);

        } catch (Exception e) {
            Timber.e(e);
        }
    }
    private static List<Address> updateWithSSLocation(JSONObject clientjson){
        try{
            String addessJson = clientjson.getString("addresses");
            JSONArray jsonArray = new JSONArray(addessJson);
            List<Address> listAddress = new ArrayList<>();
            for(int i = 0; i <jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Address address = new Gson().fromJson(jsonObject.toString(), Address.class);
                listAddress.add(address);
            }
            return listAddress;
        }catch (Exception e){

        }
        return new ArrayList<>();

    }
    @Override
    public void onRegistrationSaved(boolean isEdit) {
        finish();
        startRegisterActivity(HnppAncRegisterActivity.class);
    }
    private void startRegisterActivity(Class registerClass) {
        Intent intent = new Intent(this, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        this.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        this.finish();
    }
    public static void updateUniqueId(JSONArray fields){
        boolean has_delivery_date = false;
        for(int i=0;i<fields.length();i++){
            try {
                JSONObject object = fields.getJSONObject(i);
                if("delivery_date".equalsIgnoreCase(object.getString("key"))){

                    if(object.has("value")&&!StringUtils.isEmpty(object.getString("value"))){
                        has_delivery_date = true;
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(!has_delivery_date){
            for(int i=0;i<fields.length();i++){
                try {
                    JSONObject object = fields.getJSONObject(i);
                    if("unique_id".equalsIgnoreCase(object.getString("key"))){

                        object.put("value","");
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
   public static String getPhoneNumber() {
        return phone_number;
    }

    public static void setPhoneNumber(String phNo) {
        phone_number = phNo;
    }

    public static String getFormName() {
        return form_name;
    }

    public static void setFormName(String formName) {
        form_name = formName;
    }

    public static String getUniqueId() {
        return unique_id;
    }

    public static void setUniqueId(String uniqueId) {
        unique_id = uniqueId;
    }

    public static String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }

    public static void setFamilyBaseEntityId(String baseEntityId) {
        familyBaseEntityId = baseEntityId;
    }

    public static String getFamilyName() {
        return familyName;
    }

    public static void setFamilyName(String name) {
       familyName = name;
    }
    @Override
    public String getFormRegistrationEvent() {
        return HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
    }
}
