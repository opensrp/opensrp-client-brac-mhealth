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
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
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
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.sk_name_form_field))) {
                if(isManuallyPressed){
                    processSSName(position);
                }
            }
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.ss_name_form_field))) {
                if(isManuallyPressed){
                    processVillageName(position);
                }
            }
//            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.village_name_form_field))) {
//                if(isManuallyPressed){
//                    processVillageName(position);
//                }
//            }

            //  hideKeyBoard();
        }
    }

    private void processVillageName(int position) {
        ArrayList<SSLocations> ssLocations = ssVillage.get(position);//SSLocationHelper.getInstance().getSsModels().get(position).locations;

        ArrayList<String> villageList = new ArrayList<>();
        ArrayList<String> villageIds = new ArrayList<>();
        for(SSLocations ssLocations1 : ssLocations){
            villageList.add(ssLocations1.village.name);
            villageIds.add(ssLocations1.village.id+"");
        }
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.village_name_form_field)))) {

                    try{
                        JSONObject villageNames = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "village_name");
                        JSONArray jsonArray = new JSONArray();
                        for(String villages : villageList){
                            jsonArray.put(villages);
                        }
                        villageNames.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, villageList);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.village_name_form_field))) {
                                if(position!=-1){
                                    selectedVillageId = villageIds.get(position);
                                    JSONObject villageId = null;
                                    try {
                                        villageId = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "village_id");
                                        villageId.put(org.smartregister.family.util.JsonFormUtils.VALUE,selectedVillageId);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    JSONObject villageNames = null;
                                    try {
                                        villageNames = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "village_name");
                                        villageNames.put(org.smartregister.family.util.JsonFormUtils.VALUE,adapter.getItem(position));
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

    ArrayList<ArrayList<SSLocations>> ssVillage = new ArrayList<>();
    private String selectedSSName,selectedVillageId;

    private void processSSName(int position) {
        ArrayList<SSModel> skLocationForms = SSLocationHelper.getInstance().getAllSks();
        SSModel ssModel = skLocationForms.get(position);
        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getAllSS(ssModel.skUserName);
        ArrayList<String> ssNames = new ArrayList<>();
        ssVillage.clear();
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.ss_name_form_field).trim()))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "ss_name");
                        JSONArray jsonArray = new JSONArray();
                        for(SSModel ssModel1 : ssLocationForms){
                            jsonArray.put(ssModel1.username);
                            ssNames.add(ssModel1.username);
                            ssVillage.add(ssModel1.locations);
                        }
                        oldWardNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, ssNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.ss_name_form_field).trim())) {
                                if(position!=-1){
                                    selectedSSName = adapter.getItem(position);
                                    JSONObject villageNames = null;
                                    try {
                                        villageNames = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "ss_name");
                                        villageNames.put(org.smartregister.family.util.JsonFormUtils.VALUE,selectedSSName);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    processVillageName(position);
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
