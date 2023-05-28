package org.smartregister.unicef.dghs.fragment;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.interactor.HnppJsonFormInteractor;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.unicef.dghs.repository.GlobalLocationRepository;

import java.util.ArrayList;
import java.util.Random;

public class GuestMemberJsonFormFragment extends JsonWizardFormFragment {
    public GuestMemberJsonFormFragment() {
        super();
    }

    public static GuestMemberJsonFormFragment getFormFragment(String stepName) {
        GuestMemberJsonFormFragment jsonFormFragment = new GuestMemberJsonFormFragment();
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
    protected JsonFormFragmentPresenter createPresenter() {
        return new JsonFormFragmentPresenter(this, HnppJsonFormInteractor.getInstance());
    }

    @Override
    protected JsonFormFragmentViewState createViewState() {
        return super.createViewState();
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

            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.division))) {
                if(isManuallyPressed){
                    processDistrict(position);
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.district))) {
                if(isManuallyPressed){
                    processUpazila(districtIds.get(position));
                }
            }
        }

    }
    ArrayList<String> districtIds = new ArrayList<>();
    ArrayList<String> upazilaIds = new ArrayList<>();
    private String selectedDivCode,selectedDistrictCode,selectedUpozilaCode,selectedDivId;
    private void processDistrict(int position) {

        GlobalLocationModel locationModel = HnppApplication.getGlobalLocationRepository().getLocationByTagId(GlobalLocationRepository.LOCATION_TAG.DIVISION.getValue()).get(position);
        selectedDivCode = locationModel.code;
        selectedDivId = locationModel.id+"";
        int selectedDivId= locationModel.id;
        ArrayList<String> districtNames = new ArrayList<>();
        ArrayList<String> districtCodes = new ArrayList<>();
        districtIds.clear();
        ArrayList<GlobalLocationModel> districts = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.DISTRICT.getValue(),selectedDivId);
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.district)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "district_per");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : districts){
                            jsonArray.put(globalLocationModel.name);
                            districtNames.add(globalLocationModel.name);
                            districtCodes.add(globalLocationModel.code);
                            districtIds.add(globalLocationModel.id+"");
                        }
                        oldWardNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, districtNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.district))) {
                                if(position!=-1){
                                    selectedDistrictCode = districtCodes.get(position);
                                    processUpazila(districtIds.get(position));
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject districtPer = getFieldJSONObject(jsonArray, "district_per");
                                        districtPer.put("value",  districtNames.get(position));

                                        JSONObject districtObj = getFieldJSONObject(jsonArray, "district_id");
                                        districtObj.put("value", districtIds.get(position));

                                        JSONObject divObj = getFieldJSONObject(jsonArray, "division_id");
                                        divObj.put("value", selectedDivId);
                                    }catch (Exception e){

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
    private void processUpazila(String selectedDistrictId) {
        ArrayList<String> upazilaNames = new ArrayList<>();
        ArrayList<String> upazilaCodes = new ArrayList<>();
        upazilaIds.clear();
        ArrayList<GlobalLocationModel> upazilaList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.UPAZILA.getValue(),Integer.parseInt(selectedDistrictId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.upazila)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "upazila_per");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : upazilaList){
                            jsonArray.put(globalLocationModel.name);
                            upazilaNames.add(globalLocationModel.name);
                            upazilaCodes.add(globalLocationModel.code+"");
                            upazilaIds.add(globalLocationModel.id+"");
                        }
                        oldWardNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, upazilaNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.upazila))) {
                                if(position!=-1){
                                    selectedUpozilaCode = upazilaCodes.get(position);
                                    generatedId();
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject upozilaObj = getFieldJSONObject(jsonArray, "upazila_per");
                                        upozilaObj.put("value", upazilaNames.get(position));
                                        JSONObject upozilaIdObj = getFieldJSONObject(jsonArray, "upazila_id");
                                        upozilaIdObj.put("value", upazilaIds.get(position));
                                    }catch (Exception e){

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
    private void generatedId(){
        @SuppressLint("DefaultLocale") String eightDigit = String.format("%08d", new Random().nextInt(10000000));
        Log.v("SYSTEM_ID","systemId:"+eightDigit);
        String uniqueId = "2"+selectedDivCode+""+selectedDistrictCode+selectedUpozilaCode+eightDigit;
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialEditText) {
                if (!TextUtils.isEmpty(((MaterialEditText) formdataviews.get(i)).getFloatingLabelText()) && ((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("সিস্টেম নাম্বার")) {
                    ((MaterialEditText) formdataviews.get(i)).setText(uniqueId);
                    break;
                }
            }
        }
    }

    @Override
    public JSONObject getStep(String stepName) {
        return super.getStep(stepName);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

}
