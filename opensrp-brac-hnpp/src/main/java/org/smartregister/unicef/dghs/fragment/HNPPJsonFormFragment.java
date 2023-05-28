package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import org.smartregister.Context;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.domain.HouseholdId;
import org.smartregister.unicef.dghs.interactor.HnppJsonFormInteractor;
import org.smartregister.unicef.dghs.job.PullHouseholdIdsServiceJob;
import org.smartregister.unicef.dghs.location.BlockLocation;
import org.smartregister.unicef.dghs.location.HALocationHelper;
import org.smartregister.unicef.dghs.location.HALocation;
import org.smartregister.unicef.dghs.location.WardLocation;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.unicef.dghs.repository.GlobalLocationRepository;
import org.smartregister.unicef.dghs.repository.HouseholdIdRepository;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Random;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;
import static org.smartregister.util.Utils.getValue;

public class HNPPJsonFormFragment extends JsonWizardFormFragment {
    public HNPPJsonFormFragment() {
        super();
    }

    public static HNPPJsonFormFragment getFormFragment(String stepName) {
        HNPPJsonFormFragment jsonFormFragment = new HNPPJsonFormFragment();
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
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.union_zone))) {
                if(isManuallyPressed){
                    processOldWard(position);
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.old_ward))) {
                if(isManuallyPressed){
                    processNewWard(oldWardIds.get(position));
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.new_ward))) {
                if(isManuallyPressed){
                    processBlock(newWardIds.get(position));
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.block_outreach))) {
                if(isManuallyPressed){
                    processHouseHoldId(position);
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.division_per))) {
                if(isManuallyPressed){
                    processDistrict(position);
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.district_per))) {
                if(isManuallyPressed){
                    Log.v("LOCATION","districtIds>>"+districtIds);
                    processUpazila(districtIds.get(position));
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.paurosova_per))) {
                if(isManuallyPressed){
                    Log.v("LOCATION","districtIds>>"+districtIds);
                    processPaurosova(upazilaIds.get(position));
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.upazila_per))) {
                if(isManuallyPressed){
                    processUnion(paurosovaIds.get(position));
                }
            }
          //  hideKeyBoard();
        }


    }

    private void processDistrict(int position) {

        int selectedDivId = HnppApplication.getGlobalLocationRepository().getLocationByTagId(GlobalLocationRepository.LOCATION_TAG.DIVISION.getValue()).get(position).id;

        ArrayList<String> districtNames = new ArrayList<>();
        districtIds.clear();
        ArrayList<GlobalLocationModel> districts = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.DISTRICT.getValue(),selectedDivId);
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.district_per)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "district_per");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : districts){
                            jsonArray.put(globalLocationModel.name);
                            districtNames.add(globalLocationModel.name);
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

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.district_per))) {
                                if(position!=-1){

                                    processUpazila(districtIds.get(position));
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject districtPer = getFieldJSONObject(jsonArray, "district_per");
                                        districtPer.put("value",  districtNames.get(position));
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
        this.selectedPerDistrictId = selectedDistrictId;
        processPermanentPO();
        ArrayList<String> upazilaNames = new ArrayList<>();
        upazilaIds.clear();
        ArrayList<GlobalLocationModel> upazilaList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.UPAZILA.getValue(),Integer.parseInt(selectedDistrictId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.upazila_per)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "upazila_per");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : upazilaList){
                            jsonArray.put(globalLocationModel.name);
                            upazilaNames.add(globalLocationModel.name);
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

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.upazila_per))) {
                                if(position!=-1){
                                    processPaurosova(upazilaIds.get(position));
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject upozilaObj = getFieldJSONObject(jsonArray, "upazila_per");
                                        upozilaObj.put("value", upazilaNames.get(position));
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
    private void processPaurosova(String selectedUpozilaId) {
        ArrayList<String> paurosovaNames = new ArrayList<>();
        paurosovaIds.clear();
        ArrayList<GlobalLocationModel> paurosovaList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.PAUROSOVA.getValue(),Integer.parseInt(selectedUpozilaId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.paurosova_per)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "paurosova_per");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : paurosovaList){
                            jsonArray.put(globalLocationModel.name);
                            paurosovaNames.add(globalLocationModel.name);
                            paurosovaIds.add(globalLocationModel.id+"");
                        }
                        oldWardNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, paurosovaNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.paurosova_per))) {
                                if(position!=-1){
                                    processUnion(paurosovaIds.get(position));
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject upozilaObj = getFieldJSONObject(jsonArray, "paurosova_per");
                                        upozilaObj.put("value", paurosovaNames.get(position));
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
    private void processUnion(String selectedUpazilaId) {
        ArrayList<String> unionNames = new ArrayList<>();
        ArrayList<GlobalLocationModel> unionList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.UNION.getValue(),Integer.parseInt(selectedUpazilaId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        unionIds.clear();
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.union_per)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "union_per");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : unionList){
                            jsonArray.put(globalLocationModel.name);
                            unionNames.add(globalLocationModel.name);
                            unionIds.add(globalLocationModel.id+"");
                        }
                        oldWardNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, unionNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.union_per))) {
                                if(position!=-1){
                                    selectedUnionId = unionIds.get(position);
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject unionObj = getFieldJSONObject(jsonArray, "union_per");
                                        unionObj.put("value", unionNames.get(position));
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
    private void processPermanentPO() {
        ArrayList<String> poNames = new ArrayList<>();
        ArrayList<GlobalLocationModel> poList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.POST_OFFICE.getValue(),Integer.parseInt(selectedPerDistrictId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.post_office_per)))) {

                    try{
                        JSONObject poPerNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "post_office_permanent");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : poList){
                            jsonArray.put(globalLocationModel.name);
                            poNames.add(globalLocationModel.name);
                        }
                        poPerNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, poNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.post_office_per))) {
                                if(position!=-1){

                                    try {
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject poPerObj = getFieldJSONObject(jsonArray, "post_office_permanent");
                                        poPerObj.put("value", poNames.get(position));
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
    private void processPresentPO(String districtId) {
        ArrayList<String> poNames = new ArrayList<>();
        ArrayList<GlobalLocationModel> poList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.POST_OFFICE.getValue(),Integer.parseInt(districtId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.post_office_present)))) {

                    try{
                        JSONObject poPerNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "post_office_present");
                        JSONArray jsonArray = new JSONArray();
                        for(GlobalLocationModel globalLocationModel : poList){
                            jsonArray.put(globalLocationModel.name);
                            poNames.add(globalLocationModel.name);
                        }
                        poPerNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, poNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.post_office_present))) {
                                if(position!=-1){

                                    try {
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject poPerObj = getFieldJSONObject(jsonArray, "post_office_present");
                                        poPerObj.put("value", poNames.get(position));
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
    ArrayList<String> oldWardIds = new ArrayList<>();
    ArrayList<String> newWardIds = new ArrayList<>();
    ArrayList<String> blocksIds = new ArrayList<>();
    ArrayList<String> districtIds = new ArrayList<>();
    ArrayList<String> upazilaIds = new ArrayList<>();
    ArrayList<String> paurosovaIds = new ArrayList<>();
    ArrayList<String> unionIds = new ArrayList<>();
    String selectedUnionId = "";
    String selectedOldWardName = "";
    String selectedNewWardName = "";
    String selectedPerDistrictId = "";
    private void processOldWard(int position) {
        int selectedUnionId = HALocationHelper.getInstance().getUnionList().get(position).ward.id;
        ArrayList<String> oldWardNames = new ArrayList<>();
        oldWardIds.clear();
        ArrayList<WardLocation> oldWards = HnppApplication.getHALocationRepository().getOldWardByUnionId(selectedUnionId);
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.old_ward)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "old_ward");
                        JSONArray jsonArray = new JSONArray();
                        for(WardLocation wardLocation : oldWards){
                            jsonArray.put(wardLocation.ward.name);
                            oldWardNames.add(wardLocation.ward.name);
                            oldWardIds.add(wardLocation.ward.id+"");
                        }
                        oldWardNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, oldWardNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.old_ward))) {
                                if(position!=-1){
                                    selectedOldWardName = adapter.getItem(position);
                                    processNewWard(oldWardIds.get(position));
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
    private void processNewWard(String oldWardId) {
        ArrayList<String> newWardNames = new ArrayList<>();
        newWardIds.clear();
        ArrayList<WardLocation> newWards = HnppApplication.getHALocationRepository().getAllWardByOldWardId(Integer.parseInt(oldWardId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.new_ward)))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "ward_name");
                        JSONArray jsonArray = new JSONArray();
                        for(WardLocation wardLocation : newWards){
                            jsonArray.put(wardLocation.ward.name);
                            newWardNames.add(wardLocation.ward.name);
                            newWardIds.add(wardLocation.ward.id+"");
                        }
                        oldWardNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){

                    }


                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, newWardNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.new_ward))) {
                                if(position!=-1){
                                    selectedNewWardName = adapter.getItem(position);
                                    processBlock(newWardIds.get(position));
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
    private void processBlock(String newWardId) {
        String districtId = HnppApplication.getHALocationRepository().getDistrictIdByBlockId(Integer.parseInt(newWardId));
        Log.v("PROCESS_PRESENT","processPresentPO>>"+districtId);
        processPresentPO(districtId);
        ArrayList<BlockLocation> blocks = HnppApplication.getHALocationRepository().getOnlyBlockLocationByWardId(newWardId);
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        ArrayList<String> blockNames = new ArrayList<>();
        blocksIds.clear();
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.block_outreach)))) {

                    try{
                        JSONObject blockNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "block_name");
                        JSONArray jsonArray = new JSONArray();
                        for(BlockLocation blockLocation : blocks){
                            jsonArray.put(blockLocation.block.name);
                            blockNames.add(blockLocation.block.name);
                            blocksIds.add(blockLocation.block.id+"");
                        }
                        blockNameObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                    }catch (Exception e){

                    }


                    MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                    spinner.setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, blockNames);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0, true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.block_outreach))) {
                                if(position!=-1){
                                    processHouseHoldId(position);
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject blockName = getFieldJSONObject(jsonArray, "block_name");
                                        blockName.put("value", blockNames.get(position));
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

    public Context context() {
        return HnppApplication.getInstance().getContext();
    }

    @Override
    public JSONObject getStep(String stepName) {
        return super.getStep(stepName);
    }

    @SuppressLint("StaticFieldLeak")
    public void processHouseHoldId(final int index) {

        if(index==-1) return;

        Utils.startAsyncTask(new AsyncTask() {
            String block_id = "";
            String unique_id = "";
            HouseholdId hhid = null;

            @Override
            protected Object doInBackground(Object[] objects) {
                Log.v("SELECTED_BLOCK","block_id:"+blocksIds.get(index));
//                if(hhid == null){
//                    return blockIdList.get(index);
//                }
                @SuppressLint("DefaultLocale") String id = String.format("%02d", new Random().nextInt(100));
                HALocation HALocation = HnppApplication.getHALocationRepository().getLocationByBlock(blocksIds.get(index));
                unique_id = HALocationHelper.getInstance().generateHouseHoldId(HALocation,id);// hhid.getOpenmrsId() + "");

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(o instanceof String){
                    String str = (String)o;
                    if(!TextUtils.isEmpty(str)){
                        HnppApplication.getHNPPInstance().getHouseholdIdRepository().insertVillageId(blocksIds.get(index));
                        PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
                        showNewIdRetriveaPopup();
                        return;
                    }

                }
                ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
                for (int i = 0; i < formdataviews.size(); i++) {
                    if (formdataviews.get(i) instanceof MaterialEditText) {
                        if (!TextUtils.isEmpty(((MaterialEditText) formdataviews.get(i)).getFloatingLabelText()) && ((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("সিস্টেম নাম্বার")) {
                            ((MaterialEditText) formdataviews.get(i)).setText(unique_id);
                            try {
                                JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                JSONObject villageId = getFieldJSONObject(jsonArray, "block_id");
                                villageId.put("value", blocksIds.get(index));
                                JSONObject oldWard = getFieldJSONObject(jsonArray, "old_ward");
                                oldWard.put("value", selectedOldWardName);
                                JSONObject newWard = getFieldJSONObject(jsonArray, "ward_name");
                                newWard.put("value", selectedNewWardName);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
        private void showNewIdRetriveaPopup(){
            new AlertDialog.Builder(getActivity()).setMessage("নতুন আইডি আনা হচ্ছে ........। দয়া করে ইন্টারনেট অন রাখুন")
                    .setTitle("আইডি শেষ হয়ে গিয়েছে !!!!").setCancelable(false)
                    .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            getActivity().finish();

                        }
                    }).show();
        }

}
