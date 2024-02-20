package org.smartregister.unicef.mis.fragment;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.GlobalLocationModel;
import org.smartregister.unicef.mis.repository.DistrictListRepository;
import org.smartregister.unicef.mis.repository.GlobalLocationRepository;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Collection;

public class IndividualProfileRemoveJsonFormFragment extends JsonWizardFormFragment {
    private int districtIndex = -1;
    private boolean isManuallyPressed = false;

    public static IndividualProfileRemoveJsonFormFragment getFormFragment(String stepName) {
        IndividualProfileRemoveJsonFormFragment jsonFormFragment = new IndividualProfileRemoveJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
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
    String selectedPerUpazilaId = "";
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        if (position != -1 && parent instanceof MaterialSpinner) {
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(getString(R.string.district_only))) {
                districtIndex = position;
                if(isManuallyPressed){
                    processUpazilaList(position);
                }
            }
//            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.division))) {
//                if(isManuallyPressed){
//                    processDistrict(position);
//                }
//            }
//            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.district))) {
//                if(isManuallyPressed){
//                    Log.v("LOCATION","districtIds>>"+districtIds);
//                    processUpazila(districtIds.get(position));
//                }
//            }
//            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.paurosova))) {
//                if(isManuallyPressed){
//                    Log.v("LOCATION","districtIds>>"+districtIds);
//                    processPaurosova(upazilaIds.get(position));
//                }
//            }
//            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.upazila))) {
//                if(isManuallyPressed){
//                    processUnion(paurosovaIds.get(position));
//                }
//            }
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
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.district).trim()))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "district");
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

        ArrayList<String> upazilaNames = new ArrayList<>();
        upazilaIds.clear();
        ArrayList<GlobalLocationModel> upazilaList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.UPAZILA.getValue(),Integer.parseInt(selectedDistrictId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.upazila).trim()))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "upazila");
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

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.upazila))) {
                                if(position!=-1){
                                    processPaurosova(upazilaIds.get(position));
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject upozilaObj = getFieldJSONObject(jsonArray, "upazila");
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
        this.selectedPerUpazilaId = selectedUpozilaId;
        paurosovaIds.clear();
        ArrayList<GlobalLocationModel> paurosovaList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.PAUROSOVA.getValue(),Integer.parseInt(selectedUpozilaId));
        ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                        (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.paurosova).trim()))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "paurosova");
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
                                .equalsIgnoreCase(getContext().getResources().getString(R.string.union_zone).trim()))) {

                    try{
                        JSONObject oldWardNameObj = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "union");
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

                            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.union_zone))) {
                                if(position!=-1){
                                    selectedUnionId = unionIds.get(position);
                                    try{
                                        JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                        JSONObject unionObj = getFieldJSONObject(jsonArray, "union");
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
    ArrayList<String>upazillalist = new ArrayList<>();

    ArrayList<String>districtList = new ArrayList<>();
    DistrictListRepository districtListRepository;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        districtListRepository = new DistrictListRepository(HnppApplication.getInstance().getRepository());

        Collection<View> formDataViews = getJsonApi().getFormDataViews();
        for (View v : formDataViews) {
            if (v instanceof MaterialSpinner) {
                if (((MaterialSpinner) v).getFloatingLabelText().toString().equalsIgnoreCase(getString(R.string.district_only))) {
                    districtList = districtListRepository.getDistrictNames();
                    ((MaterialSpinner) v).setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, districtList);
                    ((MaterialSpinner) v).setAdapter(adapter);
                    ((MaterialSpinner) v).setOnItemSelectedListener(this);
                    break;
                }
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isManuallyPressed = true;
                processUpazilaList(-1);

            }
        }, 1000);
    }

    @SuppressLint("StaticFieldLeak")
    public void processUpazilaList(final int index) {


        Utils.startAsyncTask(new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                upazillalist.clear();
                if(districtListRepository!=null&&index!=-1){
                    upazillalist = districtListRepository.getUpazilaFromDistrict(districtList.get(index));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
                for (int i = 0; i < formdataviews.size(); i++) {
                    if (formdataviews.get(i) instanceof MaterialSpinner) {
                        if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                                (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                        .equalsIgnoreCase(getString(R.string.upazila_city_corp)))) {
                            MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                            spinner.setEnabled(true);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, upazillalist);
                            spinner.setAdapter(adapter);
                            spinner.setSelection(0, true);
                            spinner.setOnItemSelectedListener(IndividualProfileRemoveJsonFormFragment.this);
                            break;
                        }
                    }
                }
            }
        }, null);
    }
}
