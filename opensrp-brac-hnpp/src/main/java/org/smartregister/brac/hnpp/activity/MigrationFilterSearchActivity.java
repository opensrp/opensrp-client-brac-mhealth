package org.smartregister.brac.hnpp.activity;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.presenter.MigrationPresenter;
import org.smartregister.brac.hnpp.utils.BaseLocation;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class MigrationFilterSearchActivity extends SecuredActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener,MigrationContract.View{
    protected Spinner migration_district_spinner;
    protected Spinner migration_upazila_spinner;
    protected Spinner migration_pourosova_spinner;
    protected Spinner migration_union_spinner;
    protected Spinner migration_village_spinner;
    protected Spinner migration_gender_spinner;
    protected EditText age_migration;

    private MigrationPresenter presenter;
    private String gender, age;

    private ArrayAdapter<BaseLocation> districtSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> upazilaSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> pouroshovaSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> unionSpinnerArrayAdapter;
    private ArrayAdapter<BaseLocation> villageSpinnerArrayAdapter;

    @Override
    protected void onCreation() {
        setContentView(R.layout.migration_search_view);
        migration_district_spinner = findViewById(R.id.migration_filter_district);
        migration_upazila_spinner = findViewById(R.id.migration_filter_thana);
        migration_pourosova_spinner = findViewById(R.id.migration_filter_pourosova);
        migration_union_spinner = findViewById(R.id.migration_filter_union);
        migration_village_spinner = findViewById(R.id.migration_filter_village);
        migration_gender_spinner = findViewById(R.id.migration_filter_gender);
        age_migration = findViewById(R.id.migration_age_ET);

        presenter = new MigrationPresenter(this);
        presenter.fetchDistrict();
        gender = migration_gender_spinner.getSelectedItem().toString();
        age = age_migration.getText().toString();

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


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.migration_filter_search_btn:
                gender = HnppConstants.genderMapping.get(gender);
                BaseLocation villageLocation = (BaseLocation) migration_village_spinner.getSelectedItem();
                if(villageLocation!=null && villageLocation.id!=0){
                    MigrationSearchDetailsActivity.startMigrationSearchActivity(this,villageLocation.id+"",gender,age);
                }


                break;
        }
    }

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