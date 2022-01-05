package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.listener.HnppBottomNavigationListener;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.HnppFamilyRegisterModel;
import org.smartregister.brac.hnpp.presenter.FamilyRegisterPresenter;
import org.smartregister.brac.hnpp.presenter.HnppNavigationPresenter;
import org.smartregister.brac.hnpp.repository.HnppChwRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HnppFamilyRegisterFragment;
import org.smartregister.brac.hnpp.listener.HnppFamilyBottomNavListener;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.contract.FamilyRegisterContract;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.job.ForceSyncDataServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.simprint.SimPrintsLibrary;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.intent.InValidateIntentService;
import org.smartregister.sync.intent.ValidateIntentService;
import org.smartregister.view.fragment.BaseRegisterFragment;


import java.util.ArrayList;

import timber.log.Timber;

import static org.smartregister.util.JsonFormUtils.FIELDS;

public class FamilyRegisterActivity extends CoreFamilyRegisterActivity{
    private BroadcastReceiver notificationBroadcastReceiver;
    private MigrationSearchContentData migrationSearchContentData;
    @Override
    public void onBackPressed() {
        if(isFinishing()) return;
        new AlertDialog.Builder(this).setMessage(getString(R.string.exit_app_message))
                .setTitle(getString(R.string.exit_app_title)).setCancelable(false)
                .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent a = new Intent(Intent.ACTION_MAIN);
                        a.addCategory(Intent.CATEGORY_HOME);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(a);
                        finish();
                    }
                }).setNegativeButton(R.string.no_button_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    public static void registerBottomNavigation(BottomNavigationHelper bottomNavigationHelper,
                                                BottomNavigationView bottomNavigationView, Activity activity) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_family_menu);
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);
            bottomNavigationView.setOnNavigationItemSelectedListener(new HnppBottomNavigationListener(activity));
        }

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
        if (HnppConstants.isPALogin()) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new HnppFamilyBottomNavListener(this, bottomNavigationView));
    }
    NavigationMenu navigationMenu;

    HnppNavigationPresenter hnppNavigationPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigationMenu = NavigationMenu.getInstance(this, null, null);
        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        hnppNavigationPresenter = new HnppNavigationPresenter(

                HnppApplication.getHNPPInstance(),

                navigationMenu,

                HnppApplication.getHNPPInstance().getHnppNavigationModel());
        HnppApplication.getHNPPInstance().setupNavigation(hnppNavigationPresenter);
        if(getIntent().getBooleanExtra(HnppConstants.KEY_NEED_TO_OPEN,false)){
            navigationMenu.openDrawer();
        }
        if(!HnppConstants.isPALogin()){
            ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
            if(ssLocationForms.size() > 0){
                boolean simPrintsEnable = ssLocationForms.get(0).simprints_enable;
                if(simPrintsEnable){
                    findViewById(R.id.simprints_identity).setVisibility(View.VISIBLE);

                }else{
                    findViewById(R.id.simprints_identity).setVisibility(View.GONE);
                }
                boolean paymentEnable = ssLocationForms.get(0).payment_enable;
                if(paymentEnable){
                    findViewById(R.id.payment_view).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.payment_view).setVisibility(View.GONE);
                }
            }
            findViewById(R.id.simprints_identity).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FamilyRegisterActivity.this, SimprintsIdentityActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(org.smartregister.chw.core.R.anim.slide_in_up, org.smartregister.chw.core.R.anim.slide_out_up);
                }
            });
        }else{
            try{
                findViewById(R.id.simprints_identity).setVisibility(View.GONE);
                findViewById(R.id.ss_info_browse).setVisibility(View.GONE);
                findViewById(R.id.migration_view).setVisibility(View.GONE);
                findViewById(R.id.sk_change).setVisibility(View.VISIBLE);
                findViewById(R.id.sk_change).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FamilyRegisterActivity.this, SkSelectionActivity.class);
                        intent.putExtra(SkSelectionActivity.IS_COMES_FROM_UPDATE,true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(org.smartregister.chw.core.R.anim.slide_in_up, org.smartregister.chw.core.R.anim.slide_out_up);
                    }
                });
            }catch (Exception e){

            }


        }


        //HnppApplication.getInstance().notifyAppContextChange(); // initialize the language (bug in translation)
        action = getIntent().getStringExtra(CoreConstants.ACTIVITY_PAYLOAD.ACTION);
        if (action != null && action.equals(CoreConstants.ACTION.START_REGISTRATION)) {
            startRegistration();
        }
        HnppConstants.isViewRefresh = false;
        SimPrintsLibrary.init(FamilyRegisterActivity.this, HnppConstants.getSimPrintsProjectId(), CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
        migrationSearchContentData = (MigrationSearchContentData) getIntent().getSerializableExtra(MigrationSearchDetailsActivity.EXTRA_SEARCH_CONTENT);
        if(migrationSearchContentData!=null){
            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_text_family),"");
            hnppFamilyRegisterFragment.setMigrationSearchContentData(migrationSearchContentData);

        }

    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu.getInstance(this, null, null).getNavigationAdapter()
                .setSelectedView(CoreConstants.DrawerMenu.ALL_FAMILIES);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HnppConstants.ACTION_STOCK_COME);
        intentFilter.addAction(HnppConstants.ACTION_STOCK_END);
        intentFilter.addAction(HnppConstants.ACTION_EDD);
        intentFilter.addAction(ValidateIntentService.ACTION_VALIDATION);
        intentFilter.addAction(InValidateIntentService.ACTION_INVALIDATION);
        registerReceiver(notificationBroadcastReceiver, intentFilter);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if(SSLocationHelper.getInstance().getSsModels().size() == 0){
            Toast.makeText(this,"ss  লোকেশন পাওয়া যায়নি . পুনরায় লগইন করুন",Toast.LENGTH_LONG).show();
            return;
        }
        HnppConstants.getGPSLocation(FamilyRegisterActivity.this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try{
                    Intent intent = new Intent(FamilyRegisterActivity.this, Utils.metadata().familyFormActivity);
                    HnppJsonFormUtils.updateLatitudeLongitudeFamily(jsonForm,latitude,longitude);
                    intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                    Form form = new Form();
                    form.setName(getString(R.string.add_family));
                    form.setSaveLabel(getString(R.string.save));
                    form.setWizard(false);
                    if(!HnppConstants.isReleaseBuild()){
                        form.setActionBarBackground(R.color.test_app_color);

                    }else{
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }
                    form.setNavigationBackground(org.smartregister.family.R.color.family_navigation);
                    form.setHomeAsUpIndicator(org.smartregister.family.R.mipmap.ic_cross_white);
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    if(HnppConstants.isPALogin()){
                        form.setHideSaveLabel(true);
                        form.setSaveLabel("");
                    }

                    startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);

                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d(jsonString);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.registerEventType)) {
                    presenter().saveForm(jsonString, false);
                }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals("COVID19")) {
                    saveRegistration(jsonString);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
            HnppConstants.isViewRefresh = true;

        }

    }
    private static JSONArray processAttributesWithChoiceIDsForSave(JSONArray fields) {
        for (int i = 0; i < fields.length(); i++) {
            try {
                JSONObject fieldObject = fields.getJSONObject(i);
                if (fieldObject.has("openmrs_choice_ids")&&fieldObject.getJSONObject("openmrs_choice_ids").length()>0) {
                    if (fieldObject.has("value")) {
                        String valueEntered = fieldObject.getString("value");
                        fieldObject.put("value", fieldObject.getJSONObject("openmrs_choice_ids").get(valueEntered));
                    }
                }

            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
        return fields;
    }
    public static final String ENCOUNTER_TYPE = "encounter_type";

    public void saveRegistration(String jsonString){
        try{
            JSONObject jsonForm = new JSONObject(jsonString);
            JSONObject step1 = jsonForm.getJSONObject("step1");
            JSONArray field = step1.getJSONArray(FIELDS);
            processAttributesWithChoiceIDsForSave(field);
            String entityId =JsonFormUtils.generateRandomUUIDString();
            FormTag formTag = new FormTag();
            try{
                formTag.providerId = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
            }catch (Exception e){
                e.printStackTrace();
            }
            formTag.appVersionName = BuildConfig.VERSION_NAME;
            formTag.appVersion = BuildConfig.VERSION_CODE;
            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(
                    field, JsonFormUtils.getJSONObject(jsonForm, JsonFormUtils.METADATA),
                    formTag, entityId, JsonFormUtils.getString(jsonForm, ENCOUNTER_TYPE), CoreConstants.TABLE_NAME.CHILD);
            baseEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            JSONObject clientJson = null;
            JSONObject eventJson = null;
            eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
            Context context = HnppApplication.getInstance().getContext().applicationContext();
            HnppChwRepository pathRepository = new HnppChwRepository(context, HnppApplication.getInstance().getContext());
            EventClientRepository eventClientRepository = new EventClientRepository(pathRepository);
            eventClientRepository.addEvent(entityId,eventJson);
//            List<EventClient>eventClientList = new ArrayList<>();
//            Client baseClient = new Client(entityId);
//            baseClient.setBirthdate(new Date());
//            baseClient.setFirstName("COV");
//            baseClient.setAddresses(new ArrayList<Address>());
//            clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
//            org.smartregister.domain.db.Event domainEvent = JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.db.Event.class);
//            org.smartregister.domain.db.Client domainClient = JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.db.Client.class);
//            eventClientList.add(new EventClient(domainEvent,domainClient));
//            FamilyLibrary.getInstance().getEcSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);

//            try {
//                FamilyLibrary.getInstance().getClientProcessorForJava().processClient(eventClientList);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    FamilyRegisterPresenter presenter;

    @Override
    public FamilyRegisterContract.Presenter presenter() {
        presenter = new FamilyRegisterPresenter(this,new HnppFamilyRegisterModel());
        return presenter;
    }
    HnppFamilyRegisterFragment hnppFamilyRegisterFragment;

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        hnppFamilyRegisterFragment = new HnppFamilyRegisterFragment();
        return hnppFamilyRegisterFragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(notificationBroadcastReceiver!=null)unregisterReceiver(notificationBroadcastReceiver);
    }

    private class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(FamilyRegisterActivity.this.isFinishing()) return;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(intent != null && intent.getAction().equalsIgnoreCase(HnppConstants.ACTION_STOCK_COME)){
                            String value = intent.getStringExtra(HnppConstants.EXTRA_STOCK_COME);
                            HnppConstants.showDialog(FamilyRegisterActivity.this,getString(R.string.menu_new_stock),value);

                        }
                        if(intent != null && intent.getAction().equalsIgnoreCase(HnppConstants.ACTION_STOCK_END)){
                            String value = intent.getStringExtra(HnppConstants.EXTRA_STOCK_END);
                            HnppConstants.showDialog(FamilyRegisterActivity.this,getString(R.string.menu_end_stock),value);
                        }
                        if(intent != null && intent.getAction().equalsIgnoreCase(HnppConstants.ACTION_EDD)){
                            String value = intent.getStringExtra(HnppConstants.EXTRA_EDD);
                            HnppConstants.showDialog(FamilyRegisterActivity.this,getString(R.string.menu_edd_this_month),value);
                        }
                        if(intent != null && intent.getAction().equalsIgnoreCase(ValidateIntentService.ACTION_VALIDATION)){
                            String value = intent.getStringExtra(ValidateIntentService.EXTRA_VALIDATION);
                            if(!TextUtils.isEmpty(value) && !value.equalsIgnoreCase(ValidateIntentService.STATUS_FAILED)){
                                InValidateSyncDataServiceJob.scheduleJobImmediately(InValidateSyncDataServiceJob.TAG);
                            }
                        }
                        if(intent != null && intent.getAction().equalsIgnoreCase(InValidateIntentService.ACTION_INVALIDATION)){
                            String value = intent.getStringExtra(InValidateIntentService.EXTRA_INVALIDATION);
                            if(!TextUtils.isEmpty(value) && !value.equalsIgnoreCase(InValidateIntentService.STATUS_NOTHING)){
                                hnppNavigationPresenter.updateUnSyncCount();
                            }

                        }
                    }catch (Exception e){

                    }
                }
            });



        }
    }


}
