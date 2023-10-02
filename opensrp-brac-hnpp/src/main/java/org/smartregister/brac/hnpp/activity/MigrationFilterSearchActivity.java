package org.smartregister.brac.hnpp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.presenter.MigrationFilterSearchPresenter;
import org.smartregister.brac.hnpp.utils.BaseLocation;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.view.activity.SecuredActivity;

public class MigrationFilterSearchActivity extends SecuredActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener,MigrationContract.View{
    private static final String MIGRATION_TYPE = "migration_type";
    private static final String REQUEST_CODE = "request_code";
    private static final String EXTRA_FAMILY_ENTITY_ID = "family_id";
    private static final String EXTRA_HOUSE_HOLD_ID = "household_id";
    protected Spinner migration_district_spinner;
    protected Spinner migration_upazila_spinner;
    protected Spinner migration_pourosova_spinner;
    protected Spinner migration_union_spinner;
    protected Spinner migration_village_spinner;
    protected Spinner migration_gender_spinner;
    protected EditText age_migration,startAgeEditText;
    protected TextView migration_filter_title;

    private MigrationFilterSearchPresenter presenter;
    private String gender, startAge,age,migrationType;
    private int requestCode;

    private ArrayAdapter<BaseLocation> districtSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> upazilaSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> pouroshovaSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> unionSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> villageSpinnerArrayAdapter;
    private String familyBaseEntityId;
    private String houseHoldId;
    public static void startMigrationFilterActivity(Activity activity, String type){
        Intent intent = new Intent(activity,MigrationFilterSearchActivity.class);
        intent.putExtra(MIGRATION_TYPE,type);
        activity.startActivity(intent);
    }
    public static void startMigrationFilterActivity(Activity activity, String type, int requestCode, String familyBaseEntityId, String houseHoldId){
        Intent intent = new Intent(activity,MigrationFilterSearchActivity.class);
        intent.putExtra(MIGRATION_TYPE,type);
        intent.putExtra(REQUEST_CODE,requestCode);
        intent.putExtra(EXTRA_FAMILY_ENTITY_ID,familyBaseEntityId);
        intent.putExtra(EXTRA_HOUSE_HOLD_ID,houseHoldId);
        activity.startActivityForResult(intent,requestCode);
    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.migration_search_view);
        migration_district_spinner = findViewById(R.id.migration_filter_district);
        migration_upazila_spinner = findViewById(R.id.migration_filter_thana);
        migration_pourosova_spinner = findViewById(R.id.migration_filter_pourosova);
        migration_union_spinner = findViewById(R.id.migration_filter_union);
        migration_village_spinner = findViewById(R.id.migration_filter_village);
        migration_gender_spinner = findViewById(R.id.migration_filter_gender);
        startAgeEditText = findViewById(R.id.start_age_ET);
        age_migration = findViewById(R.id.migration_age_ET);
        migration_filter_title = findViewById(R.id.titleFilter);
        migrationType = getIntent().getStringExtra(MIGRATION_TYPE);
        requestCode = getIntent().getIntExtra(REQUEST_CODE,0);
        familyBaseEntityId = getIntent().getStringExtra(EXTRA_FAMILY_ENTITY_ID);
        houseHoldId = getIntent().getStringExtra(EXTRA_HOUSE_HOLD_ID);
        if(migrationType!=null && migrationType.equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())){
            migration_filter_title.setText("খানার এবং তার পূর্ববর্তী ঠিকানা সম্পর্কে নিম্নোক্ত তথ্যগুলো দিনঃ");
            findViewById(R.id.tv_age).setVisibility(View.GONE);
            findViewById(R.id.age_panel).setVisibility(View.GONE);
            findViewById(R.id.tv_gender).setVisibility(View.GONE);
            migration_gender_spinner.setVisibility(View.GONE);
        }

        presenter = new MigrationFilterSearchPresenter(this);
        presenter.fetchDistrict();

        migration_district_spinner.setOnItemSelectedListener(this);
        migration_upazila_spinner.setOnItemSelectedListener(this);
        migration_pourosova_spinner.setOnItemSelectedListener(this);
        migration_union_spinner.setOnItemSelectedListener(this);
        migration_village_spinner.setOnItemSelectedListener(this);

        findViewById(R.id.migration_filter_search_btn).setOnClickListener(this);
    }

    @Override
    protected void onResumption() {

    }
    @Override
    public void updateDistrictSpinner(){
        districtSpinnerArrayAdapter = new ArrayAdapter<BaseLocation>(this, android.R.layout.simple_spinner_item, presenter.getDistrictList());
        districtSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_district_spinner.setAdapter(districtSpinnerArrayAdapter);

    }
    @Override
    public void updateUpazilaSpinner(){
        upazilaSpinnerArrayAdapter = new ArrayAdapter<BaseLocation>(this, android.R.layout.simple_spinner_item, presenter.getUpazilaList());
        upazilaSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_upazila_spinner.setAdapter(upazilaSpinnerArrayAdapter);

    }
    @Override
    public void updatePouroshovaSpinner(){
        pouroshovaSpinnerArrayAdapter = new ArrayAdapter<BaseLocation>(this, android.R.layout.simple_spinner_item, presenter.getPouroshovaList());
        pouroshovaSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_pourosova_spinner.setAdapter(pouroshovaSpinnerArrayAdapter);
    }
    @Override
    public void updateUnionSpinner(){
        unionSpinnerArrayAdapter = new ArrayAdapter<BaseLocation>(this, android.R.layout.simple_spinner_item, presenter.getUnionList());
        unionSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_union_spinner.setAdapter(unionSpinnerArrayAdapter);
    }
    @Override
    public void updateVillageSpinner(){
        villageSpinnerArrayAdapter = new ArrayAdapter<BaseLocation>(this, android.R.layout.simple_spinner_item, presenter.getVillageList());
        villageSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_village_spinner.setAdapter(villageSpinnerArrayAdapter);
    }

    @Override
    public MigrationContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.migration_filter_search_btn:
                gender = migration_gender_spinner.getSelectedItem().toString();
                age = age_migration.getText().toString();
                startAge = startAgeEditText.getText().toString();
                gender = HnppConstants.genderMapping.get(gender);
                if(TextUtils.isEmpty(startAge)){
                    startAge ="0";
                }
                if(TextUtils.isEmpty(age)){
                    age = "0";
                }
                if(Integer.parseInt(startAge)>Integer.parseInt(age)){
                    startAgeEditText.setError("শেষ বয়সসীমা থেকে ছোট হতে হবে ");
                    return;
                }
                BaseLocation villageLocation = (BaseLocation) migration_village_spinner.getSelectedItem();
                BaseLocation district = (BaseLocation) migration_district_spinner.getSelectedItem();
                MigrationSearchContentData searchContentData = new MigrationSearchContentData();
                searchContentData.setAge(age);
                searchContentData.setStartAge(startAge);
                searchContentData.setDivisionId(district.parentId+"");
                searchContentData.setDistrictId(district.id+"");
                searchContentData.setVillageId(villageLocation.id+"");
                searchContentData.setMigrationType(migrationType);
                searchContentData.setGender(gender);
                if(searchContentData.getDistrictId()!=null && searchContentData.getDivisionId()!=null){
                    if(requestCode!=0){
                        MigrationSearchDetailsActivity.startMigrationSearchActivity(this,searchContentData,requestCode,familyBaseEntityId,houseHoldId);
                    }else{
                        MigrationSearchDetailsActivity.startMigrationSearchActivity(this,searchContentData);
                    }
                }else{
                    Toast.makeText(this,"Id not found",Toast.LENGTH_SHORT).show();
                }



                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == this.requestCode  && resultCode == RESULT_OK){
            setResult(RESULT_OK,data);
            finish();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if(position == -1) return;
        switch (adapterView.getId()){
            case R.id.migration_filter_district:
                BaseLocation district = (BaseLocation) migration_district_spinner.getItemAtPosition(position);
                if(district!=null){
                    presenter.fetchUpazila(district.id+"");
                }
                break;
            case R.id.migration_filter_thana:
                BaseLocation upozila = (BaseLocation) migration_upazila_spinner.getItemAtPosition(position);
                if(upozila!=null){
                    presenter.fetchPouroshova(upozila.id+"");
                }
                break;
            case R.id.migration_filter_pourosova:
                BaseLocation pourosova = (BaseLocation) migration_pourosova_spinner.getItemAtPosition(position);
                if(pourosova!=null){
                    presenter.fetchUnion(pourosova.id+"");
                }
                break;
            case R.id.migration_filter_union:
                BaseLocation union = (BaseLocation) migration_union_spinner.getItemAtPosition(position);
                if(union!=null){
                    presenter.fetchVillage(union.id+"");
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}