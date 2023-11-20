package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.DistributionData;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.view.activity.SecuredActivity;

public class AddDistributionActivity extends SecuredActivity implements View.OnClickListener {
    private static final String PUT_EXTRA_MICRO_PLAN = "micro_plan_extra";
    MicroPlanEpiData microPlanEpiData;

    public static void startAddDistributionActivity(Activity activity, MicroPlanEpiData microPlanEpiData){
        Intent intent = new Intent(activity, AddDistributionActivity.class);
        intent.putExtra(PUT_EXTRA_MICRO_PLAN,microPlanEpiData);
        activity.startActivity(intent);
    }
    EditText distributionNameTxt,distanceupozilaTxt,distanceMinuteTxt,distanceHourTxt,portarNameTxt,mobileTxt,distanceEpiTxt;
    Spinner transpotUpozilaSpinner,transportEpiSpinner;
    @Override
    protected void onCreation() {
        setContentView(R.layout.add_distribution_info);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.previous_btn).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);
        initUi();
        microPlanEpiData = (MicroPlanEpiData) getIntent().getSerializableExtra(PUT_EXTRA_MICRO_PLAN);
        if(microPlanEpiData!=null){
            if(microPlanEpiData.distributionData!=null){
                populatedUI();
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private void populatedUI(){
        distributionNameTxt.setText(microPlanEpiData.distributionData.distributionPointName);
        distanceupozilaTxt.setText(microPlanEpiData.distributionData.distanceFromUpozilla);
        distanceMinuteTxt.setText(microPlanEpiData.distributionData.minuteArriveToDistribution+"");
        distanceHourTxt.setText(microPlanEpiData.distributionData.hourArriveToDistribution+"");
        portarNameTxt.setText(microPlanEpiData.distributionData.portarName);
        mobileTxt.setText(microPlanEpiData.distributionData.portarMobile);
        distanceEpiTxt.setText(microPlanEpiData.distributionData.distanceFromEpi);
        transpotUpozilaSpinner.setSelection(((ArrayAdapter<String>)transpotUpozilaSpinner.getAdapter()).getPosition(microPlanEpiData.distributionData.transportationFromUpozilla));
        transportEpiSpinner.setSelection(((ArrayAdapter<String>)transportEpiSpinner.getAdapter()).getPosition(microPlanEpiData.distributionData.transportationFromEpi));

    }

    private void initUi() {
        distributionNameTxt = findViewById(R.id.distribution_name);
        distanceupozilaTxt = findViewById(R.id.distance_upozilla);
        distanceMinuteTxt = findViewById(R.id.distance_minute);
        distanceHourTxt = findViewById(R.id.distance_hour);
        portarNameTxt = findViewById(R.id.portar_name);
        mobileTxt = findViewById(R.id.mobile_no);
        distanceEpiTxt = findViewById(R.id.distance_epi_txt);
        transpotUpozilaSpinner = findViewById(R.id.transportation_upozilla_spinner);
        transportEpiSpinner = findViewById(R.id.transportation_epi_spinner);
        distanceMinuteTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length()>0){
                    int minute = Integer.parseInt(editable.toString());
                    float hour = (float)minute / 60;
                    distanceHourTxt.setText(hour+"");
                }

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
            case R.id.previous_btn:
                finish();
                break;
            case R.id.next_btn:
                DistributionData distributionData = new DistributionData();
                distributionData.distributionPointName = distributionNameTxt.getText().toString();
                distributionData.distanceFromUpozilla = distanceupozilaTxt.getText().toString();
                distributionData.minuteArriveToDistribution =getIntValue(distanceMinuteTxt.getText().toString());
                distributionData.hourArriveToDistribution =getFloatValue(distanceHourTxt.getText().toString());
                distributionData.portarName = portarNameTxt.getText().toString();
                distributionData.portarMobile = mobileTxt.getText().toString();
                distributionData.distanceFromEpi = distanceEpiTxt.getText().toString();
                distributionData.transportationFromUpozilla = transpotUpozilaSpinner.getSelectedItem().toString();
                distributionData.transportationFromEpi = transportEpiSpinner.getSelectedItem().toString();
                microPlanEpiData.distributionData = distributionData;
                AddSessionActivity.startAddSessionActivity(AddDistributionActivity.this,microPlanEpiData);

                break;
        }


    }
    private int getIntValue(String value){
        try {
            return Integer.parseInt(value);

        }catch (NumberFormatException e){
            return 0;
        }
    }
    private float getFloatValue(String value){
        try{
            return Float.parseFloat(value);
        }catch (NumberFormatException ne){
            return 0;
        }
    }



    @Override
    protected void onResumption() {

    }
}
