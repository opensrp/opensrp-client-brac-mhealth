package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Spinner;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.utils.FormApplicability;

import java.util.ArrayList;
import java.util.Collection;


public class HnppAncJsonFormFragment extends JsonWizardFormFragment {
    ArrayList<ViewObject> viewList = new ArrayList<>();

    public static HnppAncJsonFormFragment getFormFragment(String stepName) {
        HnppAncJsonFormFragment jsonFormFragment = new HnppAncJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        super.onCheckedChanged(buttonView, isChecked);
        setUHFWCReferCheckStatus();
        setCCFWCReferCheckStatus();
        referUHCCheckStatus(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Collection<View> formDataViews = getJsonApi().getFormDataViews();
        for (View v : formDataViews) {
            if (v instanceof LinearLayout) {
                LinearLayout viewGroup = (LinearLayout) v;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = viewGroup.getChildAt(i);
                    if (childView instanceof LinearLayout) {
                        LinearLayout childGroup = (LinearLayout) childView;
                        int childGroupCount = childGroup.getChildCount();
                        for (int k = 0; k < childGroupCount; k++) {
                            View checkboxView = childGroup.getChildAt(k);
                            if (checkboxView instanceof AppCompatCheckBox) {
                                viewList.add(new ViewObject(((AppCompatCheckBox) checkboxView).getText().toString(), (AppCompatCheckBox) checkboxView));
                            }
                        }

                    }
                }
            }
            if(v instanceof MaterialEditText){
                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("তাপমাত্রা (ফারেনহাইট )")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherTemerature);
                }
                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("ফাস্টিং")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherFasting);
                }
                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("রেন্ডম")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherRandom);
                }
                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("জরায়ুর ঊচচতা")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherUterusLength);
                }
                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("গত ২৪ ঘন্টায় বাচ্চা কতবার নাড়াচাড়া করেছে?")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherchild_movement_in_last_24hr);
                }

                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("সিস্টোলিক")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherblood_pressure_systolic);
                }
                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("ডায়াস্টোলিক")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherblood_pressure_diastolic);
                }
                if (((MaterialEditText)v).getFloatingLabelText()!=null&&(((MaterialEditText)v).getFloatingLabelText().toString()).equals("হিমোগ্লোবিন(gm/dl)")){
                    ((MaterialEditText)v).addTextChangedListener(textWatcherhemoglobin);
                }
                if (((MaterialEditText) v).getFloatingLabelText() != null && (((MaterialEditText) v).getFloatingLabelText().toString()).equals("বি.এম.আই")) {
                    ((MaterialEditText) v).setEnabled(false);
//                    JSONObject formObject = getJsonApi().getmJSONObject();
//                    System.out.print(formObject);
                }
            }

            if(v instanceof MaterialSpinner){
                if (((MaterialSpinner) v).getHint() != null && (((MaterialSpinner) v).getHint().toString()).equals("বিলিরুবিন - প্রস্রাব পরিক্ষা *")) {
                    ((MaterialSpinner) v).setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            // your code here
                            bilirubin = position == 0;
                            referUHCCheckStatus(bilirubin);

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }

                    });

//                    JSONObject formObject = getJsonApi().getmJSONObject();
//                    System.out.print(formObject);
                }
            }
        }

        getEddDate();
//        System.out.println(formDataViews);
    }
    boolean bilirubin = false;
    int uterusLengthCM = 0;
    String edd = null;
    public void getEddDate(){
        try {
            JSONArray fields = getStep("step1").getJSONArray("fields");
            for(int i=0;i<fields.length();i++){
                JSONObject fieldObject = fields.getJSONObject(i);
                String key = fieldObject.getString("key");
                if(key.equalsIgnoreCase("edd")){
                    edd = fieldObject.getString("value");
                    uterusLengthCM = FormApplicability.getUterusLengthInCM(edd);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    boolean hemoglobin = false;
    TextWatcher textWatcherhemoglobin = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    hemoglobin = Double.valueOf(s.toString())<40d;
                    referUHCCheckStatus(hemoglobin);

                }catch (Exception e){

                }
            }
        }
    };
    boolean blood_pressure_systolic = false;
    TextWatcher textWatcherblood_pressure_systolic = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    blood_pressure_systolic = Double.valueOf(s.toString())>140d;
                    referUHCCheckStatus(blood_pressure_systolic);

                }catch (Exception e){

                }
            }
        }
    };
    boolean blood_pressure_diastolic = false;
    TextWatcher textWatcherblood_pressure_diastolic = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    blood_pressure_diastolic = Double.valueOf(s.toString())>90d;
                    referUHCCheckStatus(blood_pressure_diastolic);

                }catch (Exception e){

                }
            }
        }
    };

    boolean child_movement_in_last_24hr = false;
    TextWatcher textWatcherchild_movement_in_last_24hr = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    child_movement_in_last_24hr = Double.valueOf(s.toString())<6d;
                    referUHFWCCheckStatus(child_movement_in_last_24hr);

                }catch (Exception e){

                }
            }
        }
    };
    boolean highTemperature = false;
    TextWatcher textWatcherTemerature = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    highTemperature = Double.valueOf(s.toString())>100d;
                    referUHFWCCheckStatus(highTemperature);

                }catch (Exception e){

                }
            }
        }
    };
    boolean fastingSugar = false;
    TextWatcher textWatcherFasting = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    fastingSugar = Double.valueOf(s.toString())>7d;
                    referUHCCheckStatus(fastingSugar);

                }catch (Exception e){

                }
            }
        }
    };
    boolean randomSugar = false;
    TextWatcher textWatcherRandom = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    randomSugar = Double.valueOf(s.toString())>11.1d;
                    referUHCCheckStatus(randomSugar);

                }catch (Exception e){

                }
            }
        }
    };
    boolean uterus_length = false;
    TextWatcher textWatcherUterusLength = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!StringUtils.isEmpty(s)){
                try{
                    if(edd!=null){

                        uterus_length = Double.valueOf(s.toString())<Double.valueOf(uterusLengthCM);
                        referUHFWCCheckStatus(uterus_length);
                    }


                }catch (Exception e){

                }
            }
        }
    };

    public void setUHFWCReferCheckStatus() {
        boolean isChecked = false;
        for (int i = 0; i < viewList.size(); i++) {
            CompoundButton buttonView = viewList.get(i).view;
            String label = buttonView.getText().toString();
            if (label.equalsIgnoreCase("অচেতন অবস্থা") ||
                    label.equalsIgnoreCase("শ্বাসকষ্ট/ বুকে ব্যথা") ||
                    label.equalsIgnoreCase("দাঁড়িয়ে থাকলে দুর্বল লাগে") ||
                    label.equalsIgnoreCase("অতিরিক্ত ঘামানো") ||
                    label.equalsIgnoreCase("ঠান্ডা সহ্য করতে না পারা") ||
                    label.equalsIgnoreCase("অতিরিক্ত ওজন বৃদ্ধি")
            ) {
                if (buttonView.isChecked()) {
                    isChecked = true;
                    break;
                }

            }
        }
        //check refer viewList
        referUHFWCCheckStatus(isChecked);


    }
    public void setCCFWCReferCheckStatus() {
        boolean isChecked = false;
        for (int i = 0; i < viewList.size(); i++) {
            CompoundButton buttonView = viewList.get(i).view;
            String label = buttonView.getText().toString();
            if (label.equalsIgnoreCase("প্রস্রাবে জ্বালা") ||
                    label.equalsIgnoreCase("দূর্গন্ধযুক্ত স্রাব") ||
                    label.equalsIgnoreCase("যোনিপথে জ্বালা/ব্যথা") ||
                    label.equalsIgnoreCase("যৌনাঙ্গে অস্বাভাবিক পরিবর্তন") ||
                    label.equalsIgnoreCase("তলপেটে ব্যথা")
            ) {
                if (buttonView.isChecked()) {
                    isChecked = true;
                    break;
                }

            }
        }
        //check refer viewList
        referUHFWCCheckStatus(isChecked);


    }

    public void referUHFWCCheckStatus(boolean isChecked) {
        isChecked = isChecked||highTemperature||uterus_length||child_movement_in_last_24hr;
        for (int i = 0; i < viewList.size(); i++) {
            CompoundButton buttonView = viewList.get(i).view;
            String label = viewList.get(i).label;
            if (label.equalsIgnoreCase("ইউনিয়ন স্বাস্থ্য ও পরিবার কল্যাণ কেন্দ্র")){
                buttonView.setChecked(isChecked);
            }
        }
    }
    public void referUHCCheckStatus(boolean isChecked) {
        isChecked = isChecked||fastingSugar||randomSugar||blood_pressure_diastolic||blood_pressure_systolic||hemoglobin||bilirubin;
        for (int i = 0; i < viewList.size(); i++) {
            CompoundButton buttonView = viewList.get(i).view;
            String label = viewList.get(i).label;
            if (label.equalsIgnoreCase("উপজেলা স্বাস্থ্য কমপ্লেক্স")){
                buttonView.setChecked(isChecked);
            }
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(com.vijay.jsonwizard.R.id.action_save).setVisible(true);

    }

    class ViewObject {
        String label;
        AppCompatCheckBox view;
        MaterialEditText materialEditText;
        boolean value = false;
        String place_to_refer;
        ViewObject(String label, AppCompatCheckBox view) {
            this.label = label;
            this.view = view;
        }
        ViewObject(String label, MaterialEditText materialEditText) {
            this.label = label;
            this.materialEditText = materialEditText;
        }
        public boolean getValue(){
            if(view!=null)return view.isChecked()||value;
            else return value;
        }
    }
}
