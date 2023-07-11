package org.smartregister.unicef.dghs.activity;

import static org.smartregister.chw.core.repository.ContactInfoRepository.BASE_ENTITY_ID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.contract.MigrationContract;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.unicef.dghs.presenter.GlobalSearchPresenter;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.GlobalSearchContentData;
import org.smartregister.view.activity.SecuredActivity;

import java.util.Calendar;

public class GlobalSearchActivity extends SecuredActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener,MigrationContract.View{
    private static final String MIGRATION_TYPE = "migration_type";

    protected Spinner migration_district_spinner;
    protected Spinner migration_upazila_spinner;
    protected Spinner migration_division_spinner;
    protected Spinner migration_id_spinner;
    protected Spinner migration_gender_spinner;
    protected EditText phoneNoEditText,idNumberEditText,dobEditText;
    protected EditText shrIdEditText;
    protected TextView migration_filter_title;
    protected int day, month, year;
    Calendar calendar;
    private GlobalSearchPresenter presenter;
    private String gender,phoneNo,idNumber, migrationType,familyBaseEntityId;

    private ArrayAdapter<GlobalLocationModel> districtSpinnerArrayAdapter;
    private ArrayAdapter<GlobalLocationModel> upazilaSpinnerArrayAdapter;
    private ArrayAdapter<GlobalLocationModel> divisionSpinnerArrayAdapter;

    public static void startMigrationFilterActivity(Activity activity, String type){
        Intent intent = new Intent(activity, GlobalSearchActivity.class);
        intent.putExtra(MIGRATION_TYPE,type);
        activity.startActivity(intent);
    }
    public static void startMigrationFilterActivity(Activity activity, String type, String baseentityId){
        Intent intent = new Intent(activity, GlobalSearchActivity.class);
        intent.putExtra(MIGRATION_TYPE,type);
        intent.putExtra(BASE_ENTITY_ID,baseentityId);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.migration_search_view);
        migration_district_spinner = findViewById(R.id.migration_filter_district);
        migration_upazila_spinner = findViewById(R.id.migration_filter_thana);
        migration_division_spinner = findViewById(R.id.migration_filter_division);
        migration_id_spinner = findViewById(R.id.migration_filter_id);
        migration_gender_spinner = findViewById(R.id.migration_filter_gender);
        phoneNoEditText = findViewById(R.id.phone_no);
        idNumberEditText = findViewById(R.id.id_card_number);
        dobEditText = findViewById(R.id.dobEditText);
        shrIdEditText  = findViewById(R.id.shr_number);
        migration_filter_title = findViewById(R.id.titleFilter);
        migrationType = getIntent().getStringExtra(MIGRATION_TYPE);
        familyBaseEntityId = getIntent().getStringExtra(BASE_ENTITY_ID);
        presenter = new GlobalSearchPresenter(this);
        presenter.fetchDivision();

        migration_district_spinner.setOnItemSelectedListener(this);
        migration_upazila_spinner.setOnItemSelectedListener(this);
        migration_division_spinner.setOnItemSelectedListener(this);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        findViewById(R.id.migration_filter_search_btn).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.by_shr_search_btn).setOnClickListener(this);
        findViewById(R.id.shr_search_btn).setOnClickListener(this);
        findViewById(R.id.by_address_search_btn).setOnClickListener(this);

    }

    @Override
    protected void onResumption() {

    }
    @Override
    public void updateDistrictSpinner(){
        districtSpinnerArrayAdapter = new ArrayAdapter<GlobalLocationModel>(this, android.R.layout.simple_spinner_item, presenter.getDistrictList());
        districtSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_district_spinner.setAdapter(districtSpinnerArrayAdapter);

    }
    @Override
    public void updateUpazilaSpinner(){
        upazilaSpinnerArrayAdapter = new ArrayAdapter<GlobalLocationModel>(this, android.R.layout.simple_spinner_item, presenter.getUpazilaList());
        upazilaSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_upazila_spinner.setAdapter(upazilaSpinnerArrayAdapter);

    }
    @Override
    public void updateDivisionSpinner(){
        divisionSpinnerArrayAdapter = new ArrayAdapter<GlobalLocationModel>(this, android.R.layout.simple_spinner_item, presenter.getDivisionList());
        divisionSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        migration_division_spinner.setAdapter(divisionSpinnerArrayAdapter);
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
            case R.id.by_shr_search_btn:
                findViewById(R.id.filter_by_shr).setVisibility(View.VISIBLE);
                findViewById(R.id.filter_by_div).setVisibility(View.GONE);

                break;
            case R.id.shr_search_btn: {
                if (TextUtils.isEmpty(shrIdEditText.getText())) {
                    shrIdEditText.setError("Enter SHR Id");
                    return;
                }
                GlobalSearchContentData searchContentData = new GlobalSearchContentData();
                searchContentData.setShrId(shrIdEditText.getText().toString());
                searchContentData.setMigrationType(migrationType);
                searchContentData.setFamilyBaseEntityId(familyBaseEntityId);
                GlobalSearchDetailsActivity.startMigrationSearchActivity(this, searchContentData);
            }
                break;
            case R.id.by_address_search_btn:
                findViewById(R.id.filter_by_shr).setVisibility(View.GONE);
                findViewById(R.id.filter_by_div).setVisibility(View.VISIBLE);
                break;
            case R.id.showCalenderBtn:
                DatePickerDialog fromDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        day = dayOfMonth;
                        month = mnt +1;
                        year = yr;

                        String fromDate = yr + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");
                        dobEditText.setText(fromDate);
                    }
                },year,(month-1),day);
                fromDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                fromDialog.show();
                break;
            case R.id.backBtn:
                finish();
                break;
            case R.id.migration_filter_search_btn:
                if(!HnppConstants.isConnectedToInternet(this)){
                    HnppConstants.checkNetworkConnection(this);
                    return;
                }
                gender = migration_gender_spinner.getSelectedItem().toString();
                phoneNo = phoneNoEditText.getText().toString();
                idNumber = idNumberEditText.getText().toString();
                gender = HnppConstants.genderMapping.get(gender);
                GlobalLocationModel division = (GlobalLocationModel) migration_division_spinner.getSelectedItem();
                GlobalLocationModel district = (GlobalLocationModel) migration_district_spinner.getSelectedItem();
                GlobalLocationModel upozilla = (GlobalLocationModel) migration_upazila_spinner.getSelectedItem();
                if(division==null || district ==null){
                    Toast.makeText(this,"Select division,district",Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(phoneNo) && TextUtils.isEmpty(idNumber)){
                    Toast.makeText(this,"Enter phone number or id",Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(getIdText())&& !TextUtils.isEmpty(idNumber)){
                    Toast.makeText(this,"Enter Id Type",Toast.LENGTH_LONG).show();
                    return;
                }
                GlobalSearchContentData searchContentData = new GlobalSearchContentData();
                searchContentData.setId(getIdText()+"="+idNumber);
                if(!TextUtils.isEmpty(phoneNo))searchContentData.setPhoneNo(phoneNo);
                searchContentData.setDivisionId(division.id+"");
                searchContentData.setDistrictId(district.id+"");
                if(upozilla!=null)searchContentData.setUpozillaId(upozilla.id+"");
                searchContentData.setMigrationType(migrationType);
                searchContentData.setFamilyBaseEntityId(familyBaseEntityId);
                searchContentData.setGender(gender);
                if(!TextUtils.isEmpty(dobEditText.getText().toString()))searchContentData.setDob(dobEditText.getText().toString());
                if(searchContentData.getDistrictId()!=null && searchContentData.getDivisionId()!=null){
                    GlobalSearchDetailsActivity.startMigrationSearchActivity(this,searchContentData);
                }else{
                    Toast.makeText(this,"Id not found",Toast.LENGTH_SHORT).show();
                }



                break;
        }
    }

    private String getIdText() {
        switch (migration_id_spinner.getSelectedItem().toString()){
            case "NID": return "nid";
            case "BRID": return "brid";
            case "SHR ID": return "shr_id";
            case "EPI No": return "epi";
            case "System ID": return "unique_id";
        }
        return "";
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if(position == -1) return;
        switch (adapterView.getId()){
            case R.id.migration_filter_district:
                GlobalLocationModel district = (GlobalLocationModel) migration_district_spinner.getItemAtPosition(position);
                if(district!=null){
                    presenter.fetchUpazila(district.id+"");
                }
                break;
            case R.id.migration_filter_division:
                GlobalLocationModel division = (GlobalLocationModel) migration_division_spinner.getItemAtPosition(position);
                if(division!=null){
                    presenter.fetchDistrict(division.id+"");
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}