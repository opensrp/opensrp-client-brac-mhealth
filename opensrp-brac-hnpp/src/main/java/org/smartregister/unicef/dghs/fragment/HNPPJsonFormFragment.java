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
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.domain.HouseholdId;
import org.smartregister.unicef.dghs.interactor.HnppJsonFormInteractor;
import org.smartregister.unicef.dghs.job.PullGuestMemberIdServiceJob;
import org.smartregister.unicef.dghs.job.PullHouseholdIdsServiceJob;
import org.smartregister.unicef.dghs.location.BlockLocation;
import org.smartregister.unicef.dghs.location.GeoLocationHelper;
import org.smartregister.unicef.dghs.location.GeoLocation;
import org.smartregister.unicef.dghs.location.WardLocation;
import org.smartregister.unicef.dghs.repository.GuestMemberIdRepository;
import org.smartregister.unicef.dghs.repository.HouseholdIdRepository;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Random;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

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
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.ss_name_form_field))) {
                if(isManuallyPressed){
                    processVillageList(position);
                }
            }
            else if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.village_name_form_field))) {
                if(isManuallyPressed){
                    processHouseHoldId(position);
                }
            }

          //  hideKeyBoard();
        }


    }


    @Override
    public JSONObject getStep(String stepName) {
        return super.getStep(stepName);
    }
    ArrayList<String> blockNameList = new ArrayList<>();
    ArrayList<String> blockIdList = new ArrayList<>();
    String selectedWardId = "";
    @SuppressLint("StaticFieldLeak")
    public void processVillageList(final int index) {



        Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                WardLocation wardLocation = GeoLocationHelper.getInstance().getWardList().get(index);
                selectedWardId = wardLocation.ward.id+"";
                ArrayList<BlockLocation> blockLocations = HnppApplication.getGeoLocationRepository().getOnlyBlockLocationByWardId(wardLocation.ward.id+"");
                for(BlockLocation geoLocation1 : blockLocations){
                    blockNameList.add(geoLocation1.block.name);
                    blockIdList.add(geoLocation1.block.id+"");
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
                                        .equalsIgnoreCase(getContext().getResources().getString(R.string.village_name_form_field)))) {

                            try{
                                JSONObject villageNames = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "block_name");
                                JSONArray jsonArray = new JSONArray();
                                for(String villages : blockNameList){
                                    jsonArray.put(villages);
                                }
                                villageNames.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
                            }catch (Exception e){

                            }


                            MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                            spinner.setEnabled(true);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, blockNameList);
                            spinner.setAdapter(adapter);
                            spinner.setSelection(0, true);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.village_name_form_field))) {
                                        if(position!=-1){
                                            processHouseHoldId(position);
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
        }, null);
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
                Log.v("SELECTED_BLOCK","block_id:"+blockIdList.get(index));
                HouseholdIdRepository householdIdRepo = HnppApplication.getHNPPInstance().getHouseholdIdRepository();
                HouseholdId hhid = householdIdRepo.getNextHouseholdId(blockIdList.get(index));
//                if(hhid == null){
//                    return blockIdList.get(index);
//                }
                @SuppressLint("DefaultLocale") String id = String.format("%04d", new Random().nextInt(10000));
                GeoLocation geoLocation = HnppApplication.getGeoLocationRepository().getLocationByBlock(blockIdList.get(index));
                unique_id = GeoLocationHelper.getInstance().generateHouseHoldId(geoLocation,id);// hhid.getOpenmrsId() + "");

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(o instanceof String){
                    String str = (String)o;
                    if(!TextUtils.isEmpty(str)){
                        HnppApplication.getHNPPInstance().getHouseholdIdRepository().insertVillageId(blockIdList.get(index));
                        PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
                        showNewIdRetriveaPopup();
                        return;
                    }

                }
                ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
                for (int i = 0; i < formdataviews.size(); i++) {
                    if (formdataviews.get(i) instanceof MaterialEditText) {
                        if (!TextUtils.isEmpty(((MaterialEditText) formdataviews.get(i)).getFloatingLabelText()) && ((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("খানা নাম্বার")) {
                            ((MaterialEditText) formdataviews.get(i)).setText(unique_id);
                            try {
                                JSONArray jsonArray = getStep("step1").getJSONArray("fields");
                                JSONObject villageId = getFieldJSONObject(jsonArray, "block_id");
                                villageId.put("value", blockIdList.get(index));
                                JSONObject wardIdObj = getFieldJSONObject(jsonArray, "ward_id");
                                wardIdObj.put("value", selectedWardId);
                                JSONObject blockName = getFieldJSONObject(jsonArray, "block_name");
                                blockName.put("value", blockNameList.get(index));
//                                if (hhid != null) {
//                                    getStep("step1").put("hhid", hhid.getOpenmrsId());
//                                }
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
