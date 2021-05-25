package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

public class HnppJsonWizardFormFragment extends JsonWizardFormFragment {


    public HnppJsonWizardFormFragment() {
        super();
    }

    public static HnppJsonWizardFormFragment getFormFragment(String stepName) {
        HnppJsonWizardFormFragment jsonFormFragment = new HnppJsonWizardFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }
    private boolean isManuallyPressed = false;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isManuallyPressed = true;
            }
        }, 1000);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        if (position != -1 && parent instanceof MaterialSpinner) {
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.ss_year))) {
                String value = (String)((MaterialSpinner) parent).getItemAtPosition(position);
                ArrayList<String> monthList = new ArrayList<>();
                boolean isCurrentYear = HnppJsonFormUtils.isCurrentYear(value);
                if(isCurrentYear){
                    int cMonth = HnppJsonFormUtils.getCurrentMonth();
                    for(int i = 0;i< cMonth;i++){
                        monthList.add(HnppJsonFormUtils.monthStr[i]);
                    }

                }else{
                    monthList.addAll(Arrays.asList(HnppJsonFormUtils.monthStr));
                }
                Log.v("SPINNER_VALUE","value:"+value);
                if(isManuallyPressed){
                    processMonth(monthList);
                }
            }


            //  hideKeyBoard();
        }
    }

    private void processMonth(ArrayList<String> monthList) {

        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.ss_month)))) {

                    try{
                        JSONObject villageNames = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "month");
                        JSONArray jsonArray = new JSONArray();
                        for(String villages : monthList){
                            jsonArray.put(villages);
                        }
                        villageNames.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){

                    }


                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, monthList);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position != -1 && parent instanceof MaterialSpinner) {
                                if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.ss_month))) {
                                    try {
                                        String value = (String)((MaterialSpinner) parent).getItemAtPosition(position);
                                        JSONObject villageNames = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "month");
                                        villageNames.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
                                        isManuallyPressed = false;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                }
                            }


                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    break;
                }
            }
        }


    }
}
