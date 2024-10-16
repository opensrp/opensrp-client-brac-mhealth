package org.smartregister.unicef.mis.activity;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.unicef.mis.utils.SessionPlanData;
import org.smartregister.unicef.mis.widget.CustomCalendarView;
import org.smartregister.view.activity.SecuredActivity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddSessionActivity extends SecuredActivity implements AdapterView.OnItemSelectedListener,View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String PUT_EXTRA_MICRO_PLAN = "micro_plan_extra";
    MicroPlanEpiData microPlanEpiData;

    public static void startAddSessionActivity(Activity activity, MicroPlanEpiData microPlanEpiData){
        Intent intent = new Intent(activity, AddSessionActivity.class);
        intent.putExtra(PUT_EXTRA_MICRO_PLAN,microPlanEpiData);
        activity.startActivity(intent);
    }
    EditText januaryCountTxt,februaryCountTxt,marchCountTxt,aprilCountTxt,mayCountTxt,juneCountTxt,julyCountTxt,augustCountTxt;
    EditText septemberTxt,octoberTxt,novemberTxt,decemberTxt,yearlyCountTxt;
    Spinner additionalMonth1Txt,additionalMonth2Txt,additionalMonth3Txt,additionalMonth4Txt;
    EditText additionalMonth1ValueTxt,additionalMonth2ValueTxt,additionalMonth3ValueTxt,additionalMonth4ValueTxt;
    TextView yearText;
    CheckBox saturdayChk,sundayChk,mondayChk,tuesdayChk,wednesdayChk,thursdayChk,otherChk;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreation() {
        setContentView(R.layout.add_session_info);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.additional_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.previous_btn).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_jan).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_feb).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_mar).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_april).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_may).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_jun).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_july).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_aug).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_sep).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_oct).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_nov).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_dec).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_add_1).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_add_2).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_add_3).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn_add_4).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);
        saturdayChk = findViewById(R.id.saturday);
        sundayChk = findViewById(R.id.sunday);
        mondayChk   = findViewById(R.id.monday);
        tuesdayChk = findViewById(R.id.tuesday);
        wednesdayChk = findViewById(R.id.wednesday);
        thursdayChk  = findViewById(R.id.thursday);
        otherChk = findViewById(R.id.other);
        saturdayChk.setOnCheckedChangeListener(this);
        sundayChk.setOnCheckedChangeListener(this);
        mondayChk.setOnCheckedChangeListener(this);
        tuesdayChk.setOnCheckedChangeListener(this);
        wednesdayChk.setOnCheckedChangeListener(this);
        thursdayChk.setOnCheckedChangeListener(this);
        otherChk.setOnCheckedChangeListener(this);
        yearText = findViewById(R.id.year_text);

        initUi();
        microPlanEpiData = (MicroPlanEpiData) getIntent().getSerializableExtra(PUT_EXTRA_MICRO_PLAN);
        if(microPlanEpiData!=null){
            if(microPlanEpiData.sessionPlanData!=null){
                populatedUi();
            }
            yearText.setText(microPlanEpiData.year+"");
        }
        if(yearText.getText().toString().isEmpty()) yearText.setText("2024");
        if(HnppConstants.isUrbanUser()){
            //[27-09-2024 as per requirment from shahed vai urban user also show calender view rather than day view
            findViewById(R.id.day_view).setVisibility(View.GONE);
            findViewById(R.id.calendar_view).setVisibility(View.VISIBLE);
            //findViewById(R.id.additional_btn).setVisibility(View.GONE);
        }else{
            findViewById(R.id.calendar_view).setVisibility(View.VISIBLE);
            findViewById(R.id.day_view).setVisibility(View.GONE);
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

        yearText.setText(microPlanEpiData.sessionPlanData.year+"");
        yearlyCountTxt.setText(microPlanEpiData.sessionPlanData.yearlyCount+"");
        if(microPlanEpiData.sessionPlanData.saturday) saturdayChk.setChecked(true);
        if(microPlanEpiData.sessionPlanData.sunday) sundayChk.setChecked(true);
        if(microPlanEpiData.sessionPlanData.monday) mondayChk.setChecked(true);
        if(microPlanEpiData.sessionPlanData.tuesday) tuesdayChk.setChecked(true);
        if(microPlanEpiData.sessionPlanData.wednesday) wednesdayChk.setChecked(true);
        if(microPlanEpiData.sessionPlanData.thursday) thursdayChk.setChecked(true);
        if(microPlanEpiData.sessionPlanData.other) otherChk.setChecked(true);
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
        yearlyCountTxt = findViewById(R.id.yearly_count_text);
        additionalMonth1Txt.setOnItemSelectedListener(this);
        additionalMonth2Txt.setOnItemSelectedListener(this);
        additionalMonth3Txt.setOnItemSelectedListener(this);
        additionalMonth4Txt.setOnItemSelectedListener(this);

    }
    int additionalMonth1Index,additionalMonth2Index,additionalMonth3Index,additionalMonth4Index;
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        Log.v("ADDITIONAL_DATE","position:"+position);
        if(position<=0) return;
        switch (adapterView.getId()){
            case R.id.month_1_spinner:
                additionalMonth1Index = position;
                break;
            case R.id.month_2_spinner:
                additionalMonth2Index = position;
                break;
            case R.id.month_3_spinner:
                additionalMonth3Index = position;
                break;
            case R.id.month_4_spinner:
                additionalMonth4Index = position;
                break;
        }

    }
    private boolean validateDate(int month){
        switch (month){
            case 0:
                return checkValidation(-1,parseIntegerValue(januaryCountTxt.getText().toString()),parseIntegerValue(februaryCountTxt.getText().toString()),-1,0,1);
              //return validationCheck(januaryCountTxt.getText().toString(),februaryCountTxt.getText().toString(),0,1,value);
            case 1:
                return checkValidation(parseIntegerValue(januaryCountTxt.getText().toString()),parseIntegerValue(februaryCountTxt.getText().toString()),parseIntegerValue(marchCountTxt.getText().toString()),0,1,2);

            //return validationCheck(februaryCountTxt.getText().toString(),marchCountTxt.getText().toString(),1,2,value);
            case 2:
                return checkValidation(parseIntegerValue(februaryCountTxt.getText().toString()),parseIntegerValue(marchCountTxt.getText().toString()),parseIntegerValue(aprilCountTxt.getText().toString()),1,2,3);

            //return validationCheck(marchCountTxt.getText().toString(),aprilCountTxt.getText().toString(),2,3,value);
            case 3:
                return checkValidation(parseIntegerValue(marchCountTxt.getText().toString()),parseIntegerValue(aprilCountTxt.getText().toString()),parseIntegerValue(mayCountTxt.getText().toString()),2,3,4);

            //return validationCheck(aprilCountTxt.getText().toString(),mayCountTxt.getText().toString(),3,4,value);
            case 4:
                return checkValidation(parseIntegerValue(aprilCountTxt.getText().toString()),parseIntegerValue(mayCountTxt.getText().toString()),parseIntegerValue(juneCountTxt.getText().toString()),3,4,5);

//            return validationCheck(mayCountTxt.getText().toString(),juneCountTxt.getText().toString(),4,5,value);
            case 5:
                return checkValidation(parseIntegerValue(mayCountTxt.getText().toString()),parseIntegerValue(juneCountTxt.getText().toString()),parseIntegerValue(julyCountTxt.getText().toString()),4,5,6);

//            return validationCheck(juneCountTxt.getText().toString(),julyCountTxt.getText().toString(),5,6,value);
            case 6:
                return checkValidation(parseIntegerValue(juneCountTxt.getText().toString()),parseIntegerValue(julyCountTxt.getText().toString()),parseIntegerValue(augustCountTxt.getText().toString()),5,6,7);

            // return validationCheck(julyCountTxt.getText().toString(),augustCountTxt.getText().toString(),6,7,value);
            case 7:
                return checkValidation(parseIntegerValue(julyCountTxt.getText().toString()),parseIntegerValue(augustCountTxt.getText().toString()),parseIntegerValue(septemberTxt.getText().toString()),6,7,8);

//            return validationCheck(augustCountTxt.getText().toString(),septemberTxt.getText().toString(),7,8,value);
            case 8:
                return checkValidation(parseIntegerValue(augustCountTxt.getText().toString()),parseIntegerValue(septemberTxt.getText().toString()),parseIntegerValue(octoberTxt.getText().toString()),7,8,9);

//            return validationCheck(septemberTxt.getText().toString(),octoberTxt.getText().toString(),8,9,value);
            case 9:
                return checkValidation(parseIntegerValue(septemberTxt.getText().toString()),parseIntegerValue(octoberTxt.getText().toString()),parseIntegerValue(novemberTxt.getText().toString()),8,9,10);

//            return validationCheck(octoberTxt.getText().toString(),novemberTxt.getText().toString(),9,10,value);
            case 10:
                return checkValidation(parseIntegerValue(octoberTxt.getText().toString()),parseIntegerValue(novemberTxt.getText().toString()),parseIntegerValue(decemberTxt.getText().toString()),9,10,11);

//            return validationCheck(novemberTxt.getText().toString(),decemberTxt.getText().toString(),11,12,value);
            case 11:
                return checkValidation(parseIntegerValue(novemberTxt.getText().toString()),parseIntegerValue(decemberTxt.getText().toString()),-1,10,11,-1);

            //return checkValidation(parseIntegerValue(novemberTxt.getText().toString()),parseIntegerValue(decemberTxt.getText().toString()),-1,10,11,-1);

              default:
                return true;
        }
    }
    private boolean additionalValidationCheck(int month, String value){
        switch (month){
            case 0:
            return validationCheck(januaryCountTxt.getText().toString(),februaryCountTxt.getText().toString(),0,1,value);
            case 1:
            return validationCheck(februaryCountTxt.getText().toString(),marchCountTxt.getText().toString(),1,2,value);
            case 2:
            return validationCheck(marchCountTxt.getText().toString(),aprilCountTxt.getText().toString(),2,3,value);
            case 3:
            return validationCheck(aprilCountTxt.getText().toString(),mayCountTxt.getText().toString(),3,4,value);
            case 4:
            return validationCheck(mayCountTxt.getText().toString(),juneCountTxt.getText().toString(),4,5,value);
            case 5:
            return validationCheck(juneCountTxt.getText().toString(),julyCountTxt.getText().toString(),5,6,value);
            case 6:
            return validationCheck(julyCountTxt.getText().toString(),augustCountTxt.getText().toString(),6,7,value);
            case 7:
            return validationCheck(augustCountTxt.getText().toString(),septemberTxt.getText().toString(),7,8,value);
            case 8:
            return validationCheck(septemberTxt.getText().toString(),octoberTxt.getText().toString(),8,9,value);
            case 9:
            return validationCheck(octoberTxt.getText().toString(),novemberTxt.getText().toString(),9,10,value);
            case 10:
            return validationCheck(novemberTxt.getText().toString(),decemberTxt.getText().toString(),11,12,value);
            case 11:
            return validationCheck(decemberTxt.getText().toString(),"",12,-1,value);

            default:
                return true;
        }
    }
    private boolean checkValidation(int previousMonthSelectedDate, int currentMonthSelectedDate, int nextMonthSelectedDate, int previousMonth, int currentMonth, int nextMonth){
        if(currentMonthSelectedDate == -1) return true;
        String currentDateStr = formatedDateStr(currentMonthSelectedDate,currentMonth);

        if(previousMonth == -1){
            if(nextMonthSelectedDate == -1) return true;
            //it's january so calculation on within january and february
            String nextDateStr = formatedDateStr(nextMonthSelectedDate,nextMonth);
            return isValid(currentDateStr,nextDateStr);
        }else if(nextMonth == -1){
            if(previousMonthSelectedDate == -1) return true;
            //it's december so calculation within november and december
            String previousDateStr = formatedDateStr(previousMonthSelectedDate,previousMonth);
            return isValid(currentDateStr,previousDateStr);
        }else{
            //first check with previous month
            boolean previousIsValid;
            if(previousMonthSelectedDate == -1){
                previousIsValid = true;
            }else{
                String previousDateStr = formatedDateStr(previousMonthSelectedDate,previousMonth);
                previousIsValid = isValid(currentDateStr,previousDateStr);
            }
            if(previousIsValid){
                if(nextMonthSelectedDate == -1) return true;
                String nextDateStr = formatedDateStr(nextMonthSelectedDate,nextMonth);
                return isValid(currentDateStr,nextDateStr);
            }
            return false;
        }

    }
    private boolean validationCheck(String date1Value, String date2Value, int date1Month, int date2Month,String pressV){
        int previousMonth = parseIntegerValue(date1Value);
        if(previousMonth == -1) return true;
        if(TextUtils.isEmpty(date2Value)) return true;
        if(previousMonth>5) return false;
        int pressValue = parseIntegerValue(pressV);
        if(pressValue == -1) return true;
        String dateStr1 = formatedDateStr(pressValue,date1Month);
        String dateStr2 = formatedDateStr(previousMonth,date1Month);
        boolean isValid = isValid(dateStr1,dateStr2);
        if(!isValid) return false;
        int nextMonth = parseIntegerValue(date2Value);
        if(nextMonth == -1)return true;
        dateStr2 = formatedDateStr(nextMonth,date2Month);
        isValid = isValid(dateStr1,dateStr2);
        if(!isValid) return false;
        return true;
    }
    /*
    datestr1 = inputedDate, datestr2 = comparedDateValue
     */
    private boolean isValid(String dateStr1, String dateStr2){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
        org.joda.time.LocalDate date1 = formatter.parseDateTime(dateStr1).toLocalDate();
        org.joda.time.LocalDate date2 = formatter.parseDateTime(dateStr2).toLocalDate();
        int dayDiff = Days.daysBetween(date2, date1).getDays();
        Log.v("DATE_DIFF","dateStr1:"+dateStr1+":dateStr2:"+dateStr2+":dayDiff:"+dayDiff);
        return dayDiff>=28 || dayDiff<0;
    }
    private int parseIntegerValue(String value){
        try{
            return Integer.parseInt(value);
        }catch (NumberFormatException ne){
            return -1;
        }
    }
    private String formatedDateStr(int dd, int month){
        month = month+1;
        String formatePressDate = dd<=9?"0"+dd:dd+"";
        String formatePressMonth = month<=9?"0"+month:month+"";
        return formatePressDate+"-"+formatePressMonth+"-"+yearText.getText().toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        switch (view.getId()){
            case R.id.other:
                saturdayChk.setChecked(false);
                sundayChk.setChecked(false);
                mondayChk.setChecked(false);
                tuesdayChk.setChecked(false);
                wednesdayChk.setChecked(false);
                thursdayChk.setChecked(false);
                otherChk.setChecked(isChecked);
                findViewById(R.id.calendar_view).setVisibility(View.VISIBLE);
                findViewById(R.id.day_view).setVisibility(View.GONE);
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.showCalenderBtn_jan:
                showMultiDatePicker(0,januaryCountTxt);
                break;
            case R.id.showCalenderBtn_feb:
                showMultiDatePicker(1,februaryCountTxt);
                break;
            case R.id.showCalenderBtn_mar:
                showMultiDatePicker(2,marchCountTxt);
                break;
            case R.id.showCalenderBtn_april:
                showMultiDatePicker(3,aprilCountTxt);
                break;
            case R.id.showCalenderBtn_may:
                showMultiDatePicker(4,mayCountTxt);
                break;
            case R.id.showCalenderBtn_jun:
                showMultiDatePicker(5,juneCountTxt);
                break;
            case R.id.showCalenderBtn_july:
                showMultiDatePicker(6,julyCountTxt);
                break;
            case R.id.showCalenderBtn_aug:
                showMultiDatePicker(7,augustCountTxt);
                break;
            case R.id.showCalenderBtn_sep:
                showMultiDatePicker(8,septemberTxt);
                break;
            case R.id.showCalenderBtn_oct:
                showMultiDatePicker(9,octoberTxt);
                break;
            case R.id.showCalenderBtn_nov:
                showMultiDatePicker(10,novemberTxt);
                break;
            case R.id.showCalenderBtn_dec:
                showMultiDatePicker(11,decemberTxt);
                break;
            case R.id.showCalenderBtn_add_1:
                showMultiDatePicker(additionalMonth1Index,additionalMonth1ValueTxt,true);
                break;
            case R.id.showCalenderBtn_add_2:
                showMultiDatePicker(additionalMonth2Index,additionalMonth2ValueTxt,true);
                break;
            case R.id.showCalenderBtn_add_3:
                showMultiDatePicker(additionalMonth3Index,additionalMonth3ValueTxt,true);
                break;
            case R.id.showCalenderBtn_add_4:
                showMultiDatePicker(additionalMonth4Index,additionalMonth4ValueTxt,true);
                break;
            case R.id.backBtn:
            case R.id.previous_btn:
                finish();
                break;
            case R.id.additional_btn:
                findViewById(R.id.additional_month_view).setVisibility(View.VISIBLE);
                break;
            case R.id.next_btn:
                if(sundayChk.isChecked()) generateDateFromDay(1);
                if(mondayChk.isChecked()) generateDateFromDay(2);
                if(tuesdayChk.isChecked()) generateDateFromDay(3);
                if(wednesdayChk.isChecked()) generateDateFromDay(4);
                if(thursdayChk.isChecked()) generateDateFromDay(5);
                if(saturdayChk.isChecked()) generateDateFromDay(7);
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
                sessionPlanData.saturday = saturdayChk.isChecked();
                sessionPlanData.sunday = sundayChk.isChecked();
                sessionPlanData.monday = mondayChk.isChecked();
                sessionPlanData.tuesday = tuesdayChk.isChecked();
                sessionPlanData.wednesday = wednesdayChk.isChecked();
                sessionPlanData.thursday = thursdayChk.isChecked();
                sessionPlanData.other = otherChk.isChecked();
                sessionPlanData.year = getIntValue(yearText.getText()+"");//getIntValue(yearSpinner.getSelectedItem()+"");
                sessionPlanData.yearlyCount = count+"";
                microPlanEpiData.sessionPlanData = sessionPlanData;
                microPlanEpiData.year = Integer.parseInt(sessionPlanData.year);
                AddWorkerActivity.startAddWorkerActivity(AddSessionActivity.this,microPlanEpiData);

                break;
        }



    }
    HashMap<Integer,String> dayHashMap = new HashMap<>();

    private void generateDateFromDay(int dayIndex){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2024);//Integer.parseInt(yearText.getText().toString()));
        calendar.set( Calendar.DAY_OF_MONTH, 1  );
        int monthIndex = 0;
        do{
            calendar.set( Calendar.MONTH, monthIndex  );
            int month = calendar.get( Calendar.MONTH );
            StringBuilder builder = new StringBuilder();
            while( calendar.get( Calendar.MONTH ) == month ) {

                if( calendar.get( Calendar.DAY_OF_WEEK ) == dayIndex ) {
                    if(builder.length()>0){
                        builder.append(",");
                    }
                    builder.append(calendar.get( Calendar.DAY_OF_MONTH ));
                }

                calendar.add( Calendar.DAY_OF_MONTH , 1 );
            }

            String previousData = dayHashMap.get(monthIndex)==null?builder.toString():dayHashMap.get(monthIndex)+","+builder.toString();
            List<String> list = new ArrayList<String>(Arrays.asList(builder.toString().split(",")));
            count = count + list.size();
            Log.v("DAY_TEXT","previousData"+previousData+":count:"+count);
            //Collections.sort(list);
           // previousData = String.join(",", list);
            dayHashMap.put(monthIndex,previousData);
            setOnCalenderView(monthIndex,previousData);
            monthIndex++;
        }while (monthIndex<=11);
       Log.v("DAY_TEXT","hashmap:"+dayHashMap);

    }

    private void setOnCalenderView(int monthIndex, String toString) {
        switch (monthIndex){
            case 0: januaryCountTxt.setText(toString);break;
            case 1: februaryCountTxt.setText(toString);break;
            case 2: marchCountTxt.setText(toString);break;
            case 3: aprilCountTxt.setText(toString);break;
            case 4: mayCountTxt.setText(toString);break;
            case 5: juneCountTxt.setText(toString);break;
            case 6: julyCountTxt.setText(toString);break;
            case 7: augustCountTxt.setText(toString);break;
            case 8: septemberTxt.setText(toString);break;
            case 9: octoberTxt.setText(toString);break;
            case 10: novemberTxt.setText(toString);break;
            case 11: decemberTxt.setText(toString);break;

        }
    }

    ArrayList<Date> selectedDates =  new ArrayList<>();
    int count = 0;
    private void showMultiDatePicker(int month,EditText editText){
        showMultiDatePicker(month,editText,false);
    }
    private void showMultiDatePicker(int month,EditText editText,boolean isComesFromAdditional){
        selectedDates.clear();
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.add_multidate_picker);
        Button doneBtn = dialog.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!HnppConstants.isUrbanUser()){
                    boolean isValid = validateDate(month);
                    if(isComesFromAdditional){
                        isValid = additionalValidationCheck(month,getDateFromAllDates().toString());
                    }
                    Log.v("DATE_DIFF","isValid:"+isValid+":month:"+month);
                    updateSessionCount();
                    if(!isValid){
                        Toast.makeText(AddSessionActivity.this,getString(R.string.difference_date),Toast.LENGTH_LONG).show();
                        return;
                        //editText.setText("");
                    }

                }else{
                    count += selectedDates.size();
                }

                editText.setText(getDateFromAllDates().toString());
                if(!HnppConstants.isUrbanUser()){
                    updateSessionCount();
                }
                updateYearlyCount();


                dialog.dismiss();
            }
        });
        CustomCalendarView calendarView = dialog.findViewById(R.id.calendar_view);

        if(HnppConstants.isUrbanUser()) {
            calendarView.setMultiSelectDayEnabled(true);
        }else {
            calendarView.setMultiSelectDayEnabled(false);
        }

            calendarView.currentMonthIndex = month;
            calendarView.year =Integer.parseInt( getIntValue(yearText.getText().toString()).equals("")?"2024":getIntValue(yearText.getText().toString()));
            calendarView.setOnMultipleDaySelectedListener(new CustomCalendarView.OnMultipleDaySelectedListener() {
                @Override
                public void onMultipleDaySelected(int month, @NonNull List<Date> dates) {
                    Log.v("SELECTED_MONTH","dates:"+dates+":");
                }
            });
            calendarView.setOnDateLongClickListener(new CustomCalendarView.OnDateLongClickListener() {
                @Override
                public void onDateLongClick(@NonNull Date selectedDate) {

                }
            });
            calendarView.setOnDateClickListener(new CustomCalendarView.OnDateClickListener() {
                @Override
                public void onDateClick(@NonNull Date selectedDate) {
                    if(HnppConstants.isUrbanUser()){
                        if(isExists(selectedDate)){
                            calendarView.clearDayViewSelection(selectedDate);
                            selectedDates.remove(selectedDate);
                        }else{
                            calendarView.markDateAsSelected(selectedDate);
                            selectedDates.add(selectedDate);
                        }
                    }else{
                        calendarView.markDateAsSelected(selectedDate);
                        selectedDates.clear();
                        selectedDates.add(selectedDate);
                    }



                }
            });
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Calendar.YEAR,calendarView.year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,1);
            calendarView.update(calendar,month);

        dialog.show();
    }
    private void updateSessionCount(){
        count = 0;
        if(!januaryCountTxt.getText().toString().isEmpty())count++;
        if(!februaryCountTxt.getText().toString().isEmpty())count++;
        if(!marchCountTxt.getText().toString().isEmpty())count++;
        if(!aprilCountTxt.getText().toString().isEmpty())count++;
        if(!mayCountTxt.getText().toString().isEmpty())count++;
        if(!juneCountTxt.getText().toString().isEmpty())count++;
        if(!julyCountTxt.getText().toString().isEmpty())count++;
        if(!augustCountTxt.getText().toString().isEmpty())count++;
        if(!septemberTxt.getText().toString().isEmpty())count++;
        if(!octoberTxt.getText().toString().isEmpty())count++;
        if(!novemberTxt.getText().toString().isEmpty())count++;
        if(!decemberTxt.getText().toString().isEmpty())count++;
        if(!additionalMonth1ValueTxt.getText().toString().isEmpty()){
            count = count+1;
        }
        if(!additionalMonth2ValueTxt.getText().toString().isEmpty())count++;
        if(!additionalMonth3ValueTxt.getText().toString().isEmpty())count++;
        if(!additionalMonth4ValueTxt.getText().toString().isEmpty())count++;
        Log.v("ADDITIONAL_DATE","count>>>"+count);
    }
    @SuppressLint("SetTextI18n")
    private void updateYearlyCount(){
        yearlyCountTxt.setText(count+"");
    }
    private boolean isExists(Date selectedDate){
        for (Date date : selectedDates){
            if(date.equals(selectedDate)){
                return true;
            }
        }
        return false;
    }
    private StringBuilder getDateFromAllDates(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Date date : selectedDates){
            if(stringBuilder.length()>0){
                stringBuilder.append(",");
            }
            org.joda.time.LocalDate localDate = new org.joda.time.LocalDate(date);
            stringBuilder.append(localDate.getDayOfMonth());
        }
        return stringBuilder;

    }


    private String  getIntValue(String value){
        try{
            return value;
            //return Integer.parseInt(value);
        }catch (NumberFormatException e){
            return "";
        }
    }



    @Override
    protected void onResumption() {

    }


}
