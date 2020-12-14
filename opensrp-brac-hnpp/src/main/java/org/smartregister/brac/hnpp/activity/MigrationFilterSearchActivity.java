package org.smartregister.brac.hnpp.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.contract.NotificationContract;
import org.smartregister.brac.hnpp.presenter.MigrationPresenter;
import org.smartregister.brac.hnpp.utils.District;
import org.smartregister.brac.hnpp.utils.Pouroshova;
import org.smartregister.brac.hnpp.utils.Union;
import org.smartregister.brac.hnpp.utils.Upazila;
import org.smartregister.brac.hnpp.utils.Village;
import org.smartregister.service.HTTPAgent;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class MigrationFilterSearchActivity extends SecuredActivity implements View.OnClickListener, AdapterView.OnItemClickListener,MigrationContract.View{
    protected Spinner migration_district_spinner;
    protected Spinner migration_upazila_spinner;
    protected Spinner migration_pourosova_spinner;
    protected Spinner migration_union_spinner;
    protected Spinner migration_village_spinner;
    protected Spinner migration_gender_spinner;
    protected EditText age_migration;

    private MigrationPresenter presenter;
    private String district, upazila, pouroshova, union, village, gender, age;

    private ArrayAdapter<District> districtSpinnerArrayAdapter;
    private ArrayAdapter<Upazila> upazilaSpinnerArrayAdapter;
    private ArrayAdapter<Pouroshova> pouroshovaSpinnerArrayAdapter;
    private ArrayAdapter<Union> unionSpinnerArrayAdapter;
    private ArrayAdapter<Village> villageSpinnerArrayAdapter;

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
        presenter.fetchUpazila();
        presenter.fetchPouroshova();
        presenter.fetchUnion();
        presenter.fetchVillage();
        generateSpinner();
        gender = migration_gender_spinner.getSelectedItem().toString();
        age = age_migration.getText().toString();

        findViewById(R.id.migration_filter_search_btn).setOnClickListener(this);
    }

    @Override
    protected void onResumption() {

    }
    private void generateSpinner(){
        districtSpinnerArrayAdapter = new ArrayAdapter<District>(this, android.R.layout.simple_spinner_item, presenter.getDistrictList());
        districtSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_district_spinner.setAdapter(districtSpinnerArrayAdapter);

        upazilaSpinnerArrayAdapter = new ArrayAdapter<Upazila>(this, android.R.layout.simple_spinner_item, presenter.getUpazilaList());
        upazilaSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_upazila_spinner.setAdapter(upazilaSpinnerArrayAdapter);

        pouroshovaSpinnerArrayAdapter = new ArrayAdapter<Pouroshova>(this, android.R.layout.simple_spinner_item, presenter.getPouroshovaList());
        pouroshovaSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_pourosova_spinner.setAdapter(pouroshovaSpinnerArrayAdapter);

        unionSpinnerArrayAdapter = new ArrayAdapter<Union>(this, android.R.layout.simple_spinner_item, presenter.getUnionList());
        unionSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_union_spinner.setAdapter(unionSpinnerArrayAdapter);

        villageSpinnerArrayAdapter = new ArrayAdapter<Village>(this, android.R.layout.simple_spinner_item, presenter.getVillageList());
        villageSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_village_spinner.setAdapter(villageSpinnerArrayAdapter);

        migration_district_spinner.setOnItemClickListener(this);
        migration_upazila_spinner.setOnItemClickListener(this);
        migration_pourosova_spinner.setOnItemClickListener(this);
        migration_union_spinner.setOnItemClickListener(this);
        migration_village_spinner.setOnItemClickListener(this);


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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (view.getId()){
            case R.id.migration_filter_district:
                district = parent.getItemAtPosition(position).toString();
                break;
            case R.id.migration_filter_thana:
                upazila = parent.getItemAtPosition(position).toString();
                break;
            case R.id.migration_filter_pourosova:
                pouroshova = parent.getItemAtPosition(position).toString();
                break;
            case R.id.migration_filter_union:
                union = parent.getItemAtPosition(position).toString();
                break;
            case R.id.migration_filter_village:
                village = parent.getItemAtPosition(position).toString();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.migration_filter_search_btn:

                break;
        }
    }
}