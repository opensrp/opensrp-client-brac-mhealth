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
import org.smartregister.unicef.mis.utils.SessionPlanData;
import org.smartregister.view.activity.SecuredActivity;

public class AddSessionActivity extends SecuredActivity implements View.OnClickListener {
    private static final String PUT_EXTRA_MICRO_PLAN = "micro_plan_extra";
    MicroPlanEpiData microPlanEpiData;

    public static void startAddSessionActivity(Activity activity, MicroPlanEpiData microPlanEpiData){
        Intent intent = new Intent(activity, AddSessionActivity.class);
        intent.putExtra(PUT_EXTRA_MICRO_PLAN,microPlanEpiData);
        activity.startActivity(intent);
    }
    EditText januaryCountTxt,februaryCountTxt,marchCountTxt,aprilCountTxt,mayCountTxt,juneCountTxt,julyCountTxt,augustCountTxt;
    EditText septemberTxt,octoberTxt,novemberTxt,decemberTxt;
    Spinner additionalMonth1Txt,additionalMonth2Txt,additionalMonth3Txt,additionalMonth4Txt,yearSpinner;
    EditText additionalMonth1ValueTxt,additionalMonth2ValueTxt,additionalMonth3ValueTxt,additionalMonth4ValueTxt;
    @Override
    protected void onCreation() {
        setContentView(R.layout.add_session_info);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.additional_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.previous_btn).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);
        initUi();
        microPlanEpiData = (MicroPlanEpiData) getIntent().getSerializableExtra(PUT_EXTRA_MICRO_PLAN);
        if(microPlanEpiData!=null){
            if(microPlanEpiData.sessionPlanData!=null){
                populatedUi();
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private void populatedUi(){
        januaryCountTxt.setText(microPlanEpiData.sessionPlanData.januaryDate+"");
        februaryCountTxt.setText(microPlanEpiData.sessionPlanData.februaryDate+"");
        marchCountTxt.setText(microPlanEpiData.sessionPlanData.marchDate+"");
        aprilCountTxt.setText(microPlanEpiData.sessionPlanData.aprilDate+"");
        mayCountTxt.setText(microPlanEpiData.sessionPlanData.mayDate+"");
        juneCountTxt.setText(microPlanEpiData.sessionPlanData.juneDate+"");
        julyCountTxt.setText(microPlanEpiData.sessionPlanData.julyDate+"");
        augustCountTxt.setText(microPlanEpiData.sessionPlanData.augustDate+"");
        septemberTxt.setText(microPlanEpiData.sessionPlanData.septemberDate+"");
        octoberTxt.setText(microPlanEpiData.sessionPlanData.octoberDate+"");
        novemberTxt.setText(microPlanEpiData.sessionPlanData.novemberDate+"");
        decemberTxt.setText(microPlanEpiData.sessionPlanData.decemberDate+"");
        additionalMonth1ValueTxt.setText(microPlanEpiData.sessionPlanData.additionalMonth1Date+"");
        additionalMonth2ValueTxt.setText(microPlanEpiData.sessionPlanData.additionalMonth2Date+"");
        additionalMonth3ValueTxt.setText(microPlanEpiData.sessionPlanData.additionalMonth3Date+"");
        additionalMonth4ValueTxt.setText(microPlanEpiData.sessionPlanData.additionalMonth4Date+"");
        additionalMonth1Txt.setSelection(((ArrayAdapter<String>)additionalMonth1Txt.getAdapter()).getPosition(microPlanEpiData.sessionPlanData.additionalMonth1));
        additionalMonth2Txt.setSelection(((ArrayAdapter<String>)additionalMonth2Txt.getAdapter()).getPosition(microPlanEpiData.sessionPlanData.additionalMonth2));
        additionalMonth3Txt.setSelection(((ArrayAdapter<String>)additionalMonth3Txt.getAdapter()).getPosition(microPlanEpiData.sessionPlanData.additionalMonth3));
        additionalMonth4Txt.setSelection(((ArrayAdapter<String>)additionalMonth4Txt.getAdapter()).getPosition(microPlanEpiData.sessionPlanData.additionalMonth4));
        yearSpinner.setSelection(((ArrayAdapter<String>)yearSpinner.getAdapter()).getPosition(microPlanEpiData.sessionPlanData.year+""));



    }

    private void initUi() {
        januaryCountTxt = findViewById(R.id.january_text);
        februaryCountTxt = findViewById(R.id.fabruary_text);
        marchCountTxt = findViewById(R.id.march_text);
        aprilCountTxt = findViewById(R.id.april_text);
        mayCountTxt = findViewById(R.id.may_text);
        juneCountTxt = findViewById(R.id.june_text);
        julyCountTxt = findViewById(R.id.july_text);
        augustCountTxt = findViewById(R.id.august_text);
        septemberTxt = findViewById(R.id.september_text);
        octoberTxt = findViewById(R.id.october_text);
        novemberTxt = findViewById(R.id.november_text);
        decemberTxt = findViewById(R.id.december_text);
        additionalMonth1Txt = findViewById(R.id.month_1_spinner);
        additionalMonth1ValueTxt = findViewById(R.id.month_1_value);
        additionalMonth2Txt = findViewById(R.id.month_2_spinner);
        additionalMonth2ValueTxt = findViewById(R.id.month_2_value);
        additionalMonth3Txt = findViewById(R.id.month_3_spinner);
        additionalMonth3ValueTxt = findViewById(R.id.month_3_value);
        additionalMonth4Txt = findViewById(R.id.month_4_spinner);
        additionalMonth4ValueTxt = findViewById(R.id.month_4_value);
        yearSpinner = findViewById(R.id.year_spinner);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
            case R.id.previous_btn:
                finish();
                break;
            case R.id.additional_btn:
                findViewById(R.id.additional_month_view).setVisibility(View.VISIBLE);
                break;
            case R.id.next_btn:
                SessionPlanData sessionPlanData = new SessionPlanData();
                sessionPlanData.januaryDate = getIntValue(januaryCountTxt.getText()+"");
                sessionPlanData.februaryDate = getIntValue(februaryCountTxt.getText()+"");
                sessionPlanData.marchDate = getIntValue(marchCountTxt.getText()+"");
                sessionPlanData.aprilDate = getIntValue(aprilCountTxt.getText()+"");
                sessionPlanData.mayDate = getIntValue(mayCountTxt.getText()+"");
                sessionPlanData.juneDate = getIntValue(juneCountTxt.getText()+"");
                sessionPlanData.julyDate = getIntValue(julyCountTxt.getText()+"");
                sessionPlanData.augustDate = getIntValue(augustCountTxt.getText()+"");
                sessionPlanData.septemberDate = getIntValue(septemberTxt.getText()+"");
                sessionPlanData.octoberDate = getIntValue(octoberTxt.getText()+"");
                sessionPlanData.novemberDate = getIntValue(novemberTxt.getText()+"");
                sessionPlanData.decemberDate = getIntValue(decemberTxt.getText()+"");
                sessionPlanData.additionalMonth1 = additionalMonth1Txt.getSelectedItem()+"";
                sessionPlanData.additionalMonth1Date =  getIntValue(additionalMonth1ValueTxt.getText()+"");
                sessionPlanData.additionalMonth2 = additionalMonth2Txt.getSelectedItem()+"";
                sessionPlanData.additionalMonth2Date =  getIntValue(additionalMonth2ValueTxt.getText()+"");
                sessionPlanData.additionalMonth3 = additionalMonth3Txt.getSelectedItem()+"";
                sessionPlanData.additionalMonth3Date =getIntValue(additionalMonth3ValueTxt.getText()+"");
                sessionPlanData.additionalMonth4 = additionalMonth4Txt.getSelectedItem()+"";
                sessionPlanData.additionalMonth4Date =  getIntValue(additionalMonth4ValueTxt.getText()+"");
                sessionPlanData.year = getIntValue(yearSpinner.getSelectedItem()+"");
                microPlanEpiData.sessionPlanData = sessionPlanData;
                microPlanEpiData.year = sessionPlanData.year;
                AddWorkerActivity.startAddWorkerActivity(AddSessionActivity.this,microPlanEpiData);
                
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



    @Override
    protected void onResumption() {

    }
}
