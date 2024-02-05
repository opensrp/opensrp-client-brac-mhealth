package org.smartregister.unicef.mis.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.SearchMigrationAdapter;
import org.smartregister.unicef.mis.contract.MigrationContract;
import org.smartregister.unicef.mis.contract.SearchDetailsContract;
import org.smartregister.unicef.mis.holder.SearchMigrationViewHolder;
import org.smartregister.unicef.mis.interactor.MigrationInteractor;
import org.smartregister.unicef.mis.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.mis.model.GlobalSearchResult;
import org.smartregister.unicef.mis.presenter.SearchDetailsPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.GlobalSearchContentData;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.HouseHoldInfo;
import org.smartregister.view.activity.SecuredActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GlobalSearchDetailsActivity extends SecuredActivity implements View.OnClickListener, SearchDetailsContract.View {
    public static final String EXTRA_SEARCH_CONTENT = "extra_search_content";

    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private EditText editTextSearch;
    ImageView crossBtn;
    private SearchDetailsPresenter presenter;
    private SearchMigrationAdapter adapter;

    private GlobalSearchContentData globalSearchContentData;

    public static void startMigrationSearchActivity(Activity activity, GlobalSearchContentData globalSearchContentData){
        Intent intent = new Intent(activity, GlobalSearchDetailsActivity.class);
        intent.putExtra(EXTRA_SEARCH_CONTENT, globalSearchContentData);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_migration_search_details);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        titleTextView = findViewById(R.id.textview_detail_two);
        editTextSearch = findViewById(R.id.search_edit_text);
        crossBtn = findViewById(R.id.cross_btn);
        crossBtn.setOnClickListener(this);
        findViewById(R.id.sort_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);

        globalSearchContentData =(GlobalSearchContentData) getIntent().getSerializableExtra(EXTRA_SEARCH_CONTENT);

        presenter = new SearchDetailsPresenter(this);
        if(globalSearchContentData !=null){
            if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.Member.name())){
                titleTextView.setText(getString(R.string.member_migration));
            }else {
                titleTextView.setText(getString(R.string.member_import));
            }
            presenter.fetchData(globalSearchContentData);
        }
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!TextUtils.isEmpty(s.toString())){
                    crossBtn.setVisibility(View.VISIBLE);
                }else{
                    crossBtn.setVisibility(View.GONE);
                }
                presenter.search(s.toString());


            }
        });

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.cross_btn:
                editTextSearch.setText("");
                break;
        }
    }
    @Override
    protected void onResumption() {

    }

    @Override
    public SearchDetailsContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void updateAdapter() {
        adapter = new SearchMigrationAdapter(this, new SearchMigrationAdapter.OnClickAdapter() {
            @Override
            public void onItemClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Client content) {
                showDetailsDialog(content);
            }

            @Override
            public void onClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Client content) {
//                PopupMenu popup = new PopupMenu(getContext(), viewHolder.imageViewMenu);
//                popup.inflate(R.menu.popup_menu);
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @SuppressLint("NonConstantResourceId")
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.migration_menu:
//                                globalSearchContentData.setBaseEntityId(content.baseEntityId);
//                                openFamilyListActivity();
//                                return true;
//                            case R.id.migration_details:
//                                content.cityVillage = content.addresses.get(0).getCityVillage();
//
//                                showDetailsDialog(content);
//                                return true;
//                            default:
//                                return false;
//                        }
//                    }
//                });
//                //displaying the popup
//                popup.show();
            }

        });
        adapter.setData(presenter.getMemberList());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }
    @SuppressLint("SimpleDateFormat")
    private void saveClientAndEvent(Client baseClient){
        AppExecutors appExecutors = new AppExecutors();
        Runnable runnable = () -> {
            try{
                if(baseClient == null) return;
                if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())) {
                    List<String> ids = new ArrayList<>();
                    ids.add(globalSearchContentData.getFamilyBaseEntityId());
                    ids.add(globalSearchContentData.getFamilyBaseEntityId());
                    baseClient.getRelationships().put("family",ids);
                    String previousProviderId = baseClient.getAttribute("provider_id")+"";
                    Log.v("GLOBAL_SEARCH","previousProviderId>>"+previousProviderId);
                    if(!TextUtils.isEmpty(previousProviderId) && !previousProviderId.equalsIgnoreCase("null")){
                        Map<String,String> identifiers =  baseClient.getIdentifiers();
                        if(identifiers ==null) identifiers = new HashMap<>();
                        identifiers.put("previous_provider", previousProviderId);
                        identifiers.put("is_migrated", "true");
                        baseClient.setIdentifiers(identifiers);
                    }

                }
                JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));

                getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);

                GlobalSearchResult globalSearchResult = presenter.getGlobalSearchResult();
                for (Event baseEvent: globalSearchResult.events) {
                    if(baseEvent.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)
                     || baseEvent.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_UPDATE_REGISTRATION)){
                        for (Obs observation:baseEvent.getObs()) {
                            if(Objects.equals(observation.getFieldCode(), "age_calculated")){
                               try{
                                   float age = Float.parseFloat(observation.getValues().get(0)+"");
                                   if(age>5){
                                       baseEvent.setEventType(HnppConstants.EVENT_TYPE.FAMILY_MEMBER_REGISTRATION);
                                   }else{
                                       baseEvent.setEventType(HnppConstants.EVENT_TYPE.CHILD_REGISTRATION);
                                   }
                                   break;
                               }catch (Exception e){
                                   baseEvent.setEventType(HnppConstants.EVENT_TYPE.FAMILY_MEMBER_REGISTRATION);
                               }

                            }else{
                                baseEvent.setEventType(HnppConstants.EVENT_TYPE.FAMILY_MEMBER_REGISTRATION);
                            }
                        }
                    }
                    if(baseEvent.getBaseEntityId().equalsIgnoreCase(baseClient.getBaseEntityId())){
                        JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                        getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
                    }
                }

                long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);
                getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
                getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
                Log.v("USER_IMPORT","globalSearchContentData.getMigrationType():"+globalSearchContentData.getMigrationType());
                if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.Member.name())) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    values.put("is_closed", 1);
                    HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY_MEMBER, values,
                            DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseClient.getBaseEntityId()});

                }
                if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())) {
                    //event table need to sync,valid
                    ContentValues valuesEvent = new ContentValues();
                    valuesEvent.put("syncStatus", "Synced");
                    valuesEvent.put("validationStatus", "Valid");
                    HnppApplication.getInstance().getRepository().getWritableDatabase().update("event", valuesEvent,
                            DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseClient.getBaseEntityId()});
                    //client table need to sync,valid
                    ContentValues valuesClient = new ContentValues();
                    valuesClient.put("syncStatus", "Synced");
                    valuesClient.put("validationStatus", "Valid");
                    HnppApplication.getInstance().getRepository().getWritableDatabase().update("client", valuesClient,
                            DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseClient.getBaseEntityId()});

                }



            } catch (Exception e) {
                e.printStackTrace();
            }
            appExecutors.mainThread().execute(() -> {
                if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.Member.name()))
                {
                    GlobalSearchMemberProfileActivity.startGlobalMemberProfileActivity(GlobalSearchDetailsActivity.this, baseClient);
                }
                else{
                    openFamilyDueTab(globalSearchContentData.getFamilyBaseEntityId(),globalSearchContentData.getBaseEntityId());
                    finish();
                }
            });
        };
        appExecutors.diskIO().execute(runnable);
//

    }
    public void openFamilyDueTab(String familyId, String memberBaseEntityId) {
        Intent intent = new Intent(this,FamilyProfileActivity.class);
        HouseHoldInfo houseHoldInfo = HnppDBUtils.getHouseHoldInfo(familyId);
        if(houseHoldInfo !=null){
            intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, houseHoldInfo.getHouseHoldHeadId());
            intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, houseHoldInfo.getPrimaryCaregiverId());
            intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, houseHoldInfo.getHouseHoldName());
            intent.putExtra(DBConstants.KEY.UNIQUE_ID, houseHoldInfo.getHouseHoldUniqueId());
            intent.putExtra(HnppConstants.KEY.MODULE_ID, houseHoldInfo.getModuleId());
            intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, houseHoldInfo.getBlockName());
        }
        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyId);
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, memberBaseEntityId);
        intent.putExtra(HnppConstants.KEY.IS_COMES_FROM_MIGRATION, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
    private void openFamilyListActivity(){
        if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())){
            //showSSDialog();
            return;
        }
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(GlobalSearchDetailsActivity.EXTRA_SEARCH_CONTENT, globalSearchContentData);
        startActivity(intent);

    }

    @Override
    public Context getContext() {
        return this;
    }

    private void showDetailsDialog(Client content){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.migration_member_details_dialog);
        TextView textViewName = dialog.findViewById(R.id.name_TV);
        TextView textViewVillage = dialog.findViewById(R.id.village_TV);
        TextView textViewPhoneNo = dialog.findViewById(R.id.phone_no_TV);
        StringBuilder nameBuilder = new StringBuilder();
        if(content.getFirstName()!=null){
            nameBuilder.append(content.getFirstName());
        }
        if(content.getLastName() !=null){
            nameBuilder.append(" ");
            nameBuilder.append(content.getLastName());
        }
            textViewName.setText(this.getString(R.string.name,nameBuilder.toString()));
            textViewName.append("\n");
            textViewName.append(this.getString(R.string.father_name,content.getAttribute("father_name_english")==null?"":content.getAttribute("father_name_english")));
            textViewName.append("\n");
            textViewName.append(this.getString(R.string.mother_name,content.getAttribute("mother_name_english")==null?"":content.getAttribute("mother_name_english")));

            textViewVillage.setText(content.getAddresses().get(0).getCityVillage());
            textViewVillage.append(",");
            textViewVillage.append(content.getAddresses().get(0).getStateProvince());
            textViewVillage.append(",");
            textViewVillage.append(content.getAddresses().get(0).getCountyDistrict());
            StringBuilder builder = new StringBuilder();
            builder.append(this.getString(R.string.dob, HnppConstants.DDMMYY.format(content.getBirthdate()))+"\n");
            builder.append(this.getString(R.string.gender_postfix, HnppConstants.getGender(content.getGender()))+"\n");
            builder.append(this.getString(R.string.phone_no,content.getAttribute("Mobile_Number"))+"\n");
            builder.append(this.getString(R.string.nid,content.getAttribute("nationalId")==null?"":content.getAttribute("nationalId"))+"\n");
            builder.append(this.getString(R.string.bid,content.getAttribute("birthRegistrationID")==null?"":content.getAttribute("birthRegistrationID")));
            textViewPhoneNo.setVisibility(View.VISIBLE);
            textViewPhoneNo.setText(builder.toString());
        if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())) {
            ((TextView)dialog.findViewById(R.id.migration_btn)).setText(getString(R.string.member_import));
        }
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.migration_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                globalSearchContentData.setBaseEntityId(content.getBaseEntityId());
                finish();
                saveClientAndEvent(content);

            }
        });
        dialog.show();

    }
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    String mSelectedVillageName,mSelectedSSName,mSelectedVillageId;
//    private void showSSDialog(){
//        ArrayList<String> ssSpinnerArray = new ArrayList<>();
//
//        ArrayList<String> villageSpinnerArray = new ArrayList<>();
//        ArrayList<String> villageIdArray = new ArrayList<>();
//
//        ArrayList<SSModel> ssLocationForms = GeoLocationHelper.getInstance().getSsModels();
//        for (SSModel ssModel : ssLocationForms) {
//            ssSpinnerArray.add(ssModel.username);
//        }
//
//        ArrayAdapter<String> ssSpinnerArrayAdapter = new ArrayAdapter<String>
//                (this, android.R.layout.simple_spinner_item,
//                        ssSpinnerArray){
//            @Override
//            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                convertView = super.getDropDownView(position, convertView,
//                        parent);
//
//                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
//                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
//                appCompatTextView.setHeight(100);
//
//                return convertView;
//            }
//        };
//
//         villageSpinnerArrayAdapter = new ArrayAdapter<String>
//                (this, android.R.layout.simple_spinner_item,
//                        villageSpinnerArray){
//            @Override
//            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                convertView = super.getDropDownView(position, convertView,
//                        parent);
//                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
//                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
//                appCompatTextView.setHeight(100);
//                return convertView;
//            }
//        };
//
//
//        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
//        dialog.setContentView(R.layout.dialog_ss_selection);
//        Spinner ss_spinner = dialog.findViewById(R.id.ss_filter_spinner);
//        Spinner village_spinner = dialog.findViewById(R.id.village_filter_spinner);
//        village_spinner.setAdapter(villageSpinnerArrayAdapter);
//        ss_spinner.setAdapter(ssSpinnerArrayAdapter);
////        ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
////            @Override
////            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
////                if (position != -1) {
////                    villageSpinnerArray.clear();
////                    villageIdArray.clear();
////                    ArrayList<GeoLocation> ssLocations = GeoLocationHelper.getInstance().getSsModels().get(position).locations;
////                    for (GeoLocation geoLocation1 : ssLocations) {
////                        villageSpinnerArray.add(geoLocation1.village.name.trim());
////                        villageIdArray.add(geoLocation1.village.id+"");
////                    }
////                    villageSpinnerArrayAdapter = new ArrayAdapter<String>
////                            (MigrationSearchDetailsActivity.this, android.R.layout.simple_spinner_item,
////                                    villageSpinnerArray);
////                    village_spinner.setAdapter(villageSpinnerArrayAdapter);
////                    mSelectedSSName = ssSpinnerArray.get(position);
////                }
////            }
////
////            @Override
////            public void onNothingSelected(AdapterView<?> parent) {
////
////            }
////        });
//        village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position != -1) {
//                    mSelectedVillageName = villageSpinnerArray.get(position);
//                    mSelectedVillageId = villageIdArray.get(position);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        Button proceed = dialog.findViewById(R.id.filter_apply_button);
//        proceed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                migrationSearchContentData.setBlockName(mSelectedSSName);
//                migrationSearchContentData.setBlockId(Integer.parseInt(mSelectedVillageId));
//                migrateHH();
//                //dialog.dismiss();
//            }
//        });
//        dialog.show();
//    }

    private void migrateHH() {
        HnppConstants.showDialogWithAction(this, getString(R.string.dialog_title), "", new Runnable() {
            @Override
            public void run() {
                new MigrationInteractor(new AppExecutors()).migrateHH(globalSearchContentData, new MigrationContract.MigrationPostInteractorCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(GlobalSearchDetailsActivity.this, "Successfully migrated,Syncing data", Toast.LENGTH_SHORT).show();
                        HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                        Intent intent = new Intent(GlobalSearchDetailsActivity.this, FamilyRegisterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(GlobalSearchDetailsActivity.this, "Fail to migrate", Toast.LENGTH_SHORT).show();


                    }
                });

            }
        }, new Runnable() {
            @Override
            public void run() {

            }
        });


    }

}