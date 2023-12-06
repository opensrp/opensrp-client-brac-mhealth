package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.listener.OnPostDataWithGps;
import org.smartregister.unicef.mis.location.BlockLocation;
import org.smartregister.unicef.mis.location.HALocationHelper;
import org.smartregister.unicef.mis.location.WardLocation;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.unicef.mis.utils.MicroPlanTypeData;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class AddCenterDetailsActivity extends SecuredActivity implements View.OnClickListener {
    private static final String PUT_EXTRA_MICRO_PLAN = "micro_plan_extra";

    protected TextView centerTypeTV;
    protected TextView totalPopulationTxt,zeroElevenTotal,twelveTwentyTotalTxt,fifteenTotalTxt,tenTotalTxt;
    protected EditText statusEditText, outreachNameTxt, hhToTxt, hhFromTxt, totalPopulationMaleTxt,totalPopulationFemaleTxt;
    private EditText zeroElevenMaleTxt,zeroElevenFeMaleTxt,twelveTwentyMaleTxt,twelveTwentyFemaleTxt,tenFemaleTxt;
    private EditText fifteenMaleTxt,fifteenFemaleTxt;
    private MicroPlanEpiData microPlanEpiData;

    public static void startAddCenterDetailsActivity(Activity activity, MicroPlanEpiData microPlanEpiData){
        Intent intent = new Intent(activity, AddCenterDetailsActivity.class);
        intent.putExtra(PUT_EXTRA_MICRO_PLAN,microPlanEpiData);
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreation() {
        setContentView(R.layout.add_center_details);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);
        initUi();
        microPlanEpiData =(MicroPlanEpiData) getIntent().getSerializableExtra(PUT_EXTRA_MICRO_PLAN);
        if(microPlanEpiData!=null){
            statusEditText.setText(microPlanEpiData.microPlanStatus);
            if(TextUtils.isEmpty(microPlanEpiData.outreachName)){
                outreachNameTxt.setError(getString(R.string.outreach_error));

            }else{
                outreachNameTxt.setText(microPlanEpiData.outreachName);
            }
            centerTypeTV.setText(microPlanEpiData.centerType);
            populatedUI();
        }
    }
    @SuppressLint("SetTextI18n")
    private void populatedUI(){
        statusEditText.setText(microPlanEpiData.microPlanStatus);
        centerTypeTV.setText(microPlanEpiData.centerType);
        outreachNameTxt.setText(microPlanEpiData.outreachName);
        if(microPlanEpiData.microPlanTypeData==null){
            return;
        }
        hhToTxt.setText(microPlanEpiData.microPlanTypeData.hhNoTo);
        hhFromTxt.setText(microPlanEpiData.microPlanTypeData.hhNoFrom);
        totalPopulationMaleTxt.setText(microPlanEpiData.microPlanTypeData.totalPopulationMale+"");
        totalPopulationFemaleTxt.setText(microPlanEpiData.microPlanTypeData.totalPopulationFemale+"");
        totalPopulationTxt.setText(microPlanEpiData.microPlanTypeData.totalPopulation+"");
        zeroElevenTotal.setText(microPlanEpiData.microPlanTypeData.zeroElevenChildTotal+"");
        tenFemaleTxt.setText(microPlanEpiData.microPlanTypeData.tenFemaleCount+"");
        tenTotalTxt.setText(microPlanEpiData.microPlanTypeData.tenFemaleCount+"");
        twelveTwentyTotalTxt.setText(microPlanEpiData.microPlanTypeData.twelveTwentyThreeChildTotal+"");
        fifteenTotalTxt.setText(microPlanEpiData.microPlanTypeData.fifteenFortyNineWomenTotal+"");
        zeroElevenMaleTxt.setText(microPlanEpiData.microPlanTypeData.zeroElevenChildMale+"");
        zeroElevenFeMaleTxt.setText(microPlanEpiData.microPlanTypeData.zeroElevenChildFeMale+"");
        twelveTwentyMaleTxt.setText(microPlanEpiData.microPlanTypeData.twelveTwentyThreeChildMale+"");
        twelveTwentyFemaleTxt.setText(microPlanEpiData.microPlanTypeData.twelveTwentyThreeChildFeMale+"");
        fifteenMaleTxt.setText(microPlanEpiData.microPlanTypeData.fifteenFortyNineWomenMale+"");
        fifteenFemaleTxt.setText(microPlanEpiData.microPlanTypeData.fifteenFortyNineWomenFeMale+"");
    }

    private void initUi() {
        statusEditText = findViewById(R.id.status_name);
        hhToTxt = findViewById(R.id.hh_to);
        hhFromTxt = findViewById(R.id.hh_from);
        centerTypeTV = findViewById(R.id.center_type);
        outreachNameTxt = findViewById(R.id.outreach_name);
        totalPopulationMaleTxt = findViewById(R.id.total_male_number);
        totalPopulationFemaleTxt = findViewById(R.id.total_female_number);
        totalPopulationTxt = findViewById(R.id.total_population_number);
        zeroElevenTotal = findViewById(R.id.zero_eleven_total_number);
        twelveTwentyTotalTxt = findViewById(R.id.twelve_total_number);
        fifteenTotalTxt = findViewById(R.id.fifteen_total_number);

        zeroElevenMaleTxt =findViewById(R.id.zero_eleven_male_number);
        zeroElevenFeMaleTxt = findViewById(R.id.zero_eleven_female_number);
        tenFemaleTxt = findViewById(R.id.ten_female_number);
        tenTotalTxt = findViewById(R.id.ten_total_number);
        twelveTwentyMaleTxt = findViewById(R.id.twelve_male_number);
        twelveTwentyFemaleTxt = findViewById(R.id.twelve_female_number);

        fifteenMaleTxt = findViewById(R.id.fifteen_male_number);
        fifteenFemaleTxt = findViewById(R.id.fifteen_female_number);
        TextWatcher zeroElevenTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //if(s.length() > 0){
                    updateZeroElevenTotal();
                //}

            }
        };
        TextWatcher twelveTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //if(s.length() > 0){
                    updateTwelveTwentyTotal();
               // }

            }
        };
        TextWatcher fifteenTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //if(s.length() > 0){
                    updateFifteenTotal();
                //}

            }
        };
        TextWatcher totalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
               // if(s.length() > 0){
                    updateTotalPopulation();
               // }

            }
        };
        TextWatcher tenTotalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // if(s.length() > 0){
                updateTenTotal();
                // }

            }
        };
        tenFemaleTxt.addTextChangedListener(tenTotalTextWatcher);
        totalPopulationMaleTxt.addTextChangedListener(totalTextWatcher);
        totalPopulationFemaleTxt.addTextChangedListener(totalTextWatcher);
        fifteenMaleTxt.addTextChangedListener(fifteenTextWatcher);
        fifteenFemaleTxt.addTextChangedListener(fifteenTextWatcher);
        twelveTwentyMaleTxt.addTextChangedListener(twelveTextWatcher);
        twelveTwentyFemaleTxt.addTextChangedListener(twelveTextWatcher);
        zeroElevenMaleTxt.addTextChangedListener(zeroElevenTextWatcher);
        zeroElevenFeMaleTxt.addTextChangedListener(zeroElevenTextWatcher);
    }
        @SuppressLint("SetTextI18n")
        private void updateZeroElevenTotal(){
        int male = getIntValue(zeroElevenMaleTxt.getText().toString());
        int female = getIntValue(zeroElevenFeMaleTxt.getText().toString());
        zeroElevenTotal.setText((male+female)+"");
    }
    @SuppressLint("SetTextI18n")
    private void updateTwelveTwentyTotal(){
        int male = getIntValue(twelveTwentyMaleTxt.getText().toString());
        int female = getIntValue(twelveTwentyFemaleTxt.getText().toString());
        twelveTwentyTotalTxt.setText((male+female)+"");
    }
    @SuppressLint("SetTextI18n")
    private void updateFifteenTotal(){
        int male = getIntValue(fifteenMaleTxt.getText().toString());
        int female = getIntValue(fifteenFemaleTxt.getText().toString());
        fifteenTotalTxt.setText((male+female)+"");
    }
    @SuppressLint("SetTextI18n")
    private void updateTotalPopulation(){
        int male = getIntValue(totalPopulationMaleTxt.getText().toString());
        int female = getIntValue(totalPopulationFemaleTxt.getText().toString());
        totalPopulationTxt.setText((male+female)+"");
    }
    @SuppressLint("SetTextI18n")
    private void updateTenTotal(){
        int female = getIntValue(tenFemaleTxt.getText().toString());
        tenTotalTxt.setText((female)+"");
    }
    @Override
    protected void onResumption() {
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.next_btn:
                if(TextUtils.isEmpty(outreachNameTxt.getText())){
                    outreachNameTxt.setError("Outreach name empty");
                    return;
                }
                MicroPlanTypeData microPlanTypeData = new MicroPlanTypeData();
                microPlanTypeData.hhNoTo = hhToTxt.getText().toString();
                microPlanTypeData.hhNoFrom = hhFromTxt.getText().toString();
                microPlanTypeData.totalPopulationMale =getIntValue(totalPopulationMaleTxt.getText().toString()) ;
                microPlanTypeData.totalPopulationFemale =getIntValue(totalPopulationFemaleTxt.getText().toString()) ;
                microPlanTypeData.totalPopulation =getIntValue(totalPopulationTxt.getText().toString()) ;
                microPlanTypeData.zeroElevenChildMale=getIntValue(zeroElevenMaleTxt.getText().toString()) ;
                microPlanTypeData.zeroElevenChildFeMale=getIntValue(zeroElevenFeMaleTxt.getText().toString()) ;
                microPlanTypeData.zeroElevenChildTotal=getIntValue(zeroElevenTotal.getText().toString()) ;
                microPlanTypeData.twelveTwentyThreeChildMale=getIntValue(twelveTwentyMaleTxt.getText().toString()) ;
                microPlanTypeData.twelveTwentyThreeChildFeMale=getIntValue(twelveTwentyFemaleTxt.getText().toString()) ;
                microPlanTypeData.twelveTwentyThreeChildTotal=getIntValue(twelveTwentyTotalTxt.getText().toString()) ;
                microPlanTypeData.fifteenFortyNineWomenMale=getIntValue(fifteenMaleTxt.getText().toString()) ;
                microPlanTypeData.fifteenFortyNineWomenFeMale=getIntValue(fifteenFemaleTxt.getText().toString()) ;
                microPlanTypeData.fifteenFortyNineWomenTotal=getIntValue(fifteenTotalTxt.getText().toString()) ;
                microPlanTypeData.tenFemaleCount = getIntValue(tenFemaleTxt.getText().toString());
                microPlanEpiData.microPlanTypeData = microPlanTypeData;
                AddDistributionActivity.startAddDistributionActivity(this,microPlanEpiData);
                break;
        }
    }
    private int getIntValue(String value){
        try{
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            return 0;
        }
    }
}