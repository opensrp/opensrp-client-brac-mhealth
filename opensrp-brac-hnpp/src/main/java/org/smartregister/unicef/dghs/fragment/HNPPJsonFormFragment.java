package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.event.Listener;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.domain.HouseholdId;
import org.smartregister.unicef.dghs.interactor.HnppJsonFormInteractor;
import org.smartregister.unicef.dghs.job.PullHouseholdIdsServiceJob;
import org.smartregister.unicef.dghs.location.BlockLocation;
import org.smartregister.unicef.dghs.location.HALocationHelper;
import org.smartregister.unicef.dghs.location.HALocation;
import org.smartregister.unicef.dghs.location.WardLocation;
import org.smartregister.unicef.dghs.lookup.MotherLookUpSmartClientsProvider;
import org.smartregister.unicef.dghs.repository.HouseholdIdRepository;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                isPressed = true;
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
    public Context context() {
        return HnppApplication.getInstance().getContext();
    }

    public Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> motherLookUpListener() {
        return motherLookUpListener;
    }
    private void updateResults(final HashMap<CommonPersonObject, List<CommonPersonObject>> map) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.lookup_results, null);

        ListView listView = view.findViewById(R.id.list_view);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity(), R.style.PathDialog);
        builder.setView(view).setNegativeButton(R.string.dismiss, null);
        builder.setCancelable(true);

        alertDialog = builder.create();

        final List<CommonPersonObject> mothers = new ArrayList<>();
        for (Map.Entry<CommonPersonObject, List<CommonPersonObject>> entry : map.entrySet()) {
            mothers.add(entry.getKey());
        }
        final MotherLookUpSmartClientsProvider motherLookUpSmartClientsProvider = new MotherLookUpSmartClientsProvider(getActivity());

        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mothers.size();
            }

            @Override
            public Object getItem(int position) {
                return mothers.get(position);
            }

            @Override
            public long getItemId(int position) {
                return Long.valueOf(mothers.get(position).getCaseId().replaceAll("\\D+", ""));
            }

            @SuppressLint("InflateParams")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                if (convertView == null) {
                    v = inflater.inflate(R.layout.mother_child_lookup_client, null);
                } else {
                    v = convertView;
                }
                CommonPersonObject commonPersonObject = mothers.get(position);
                List<CommonPersonObject> children = map.get(commonPersonObject);
                motherLookUpSmartClientsProvider.getView(commonPersonObject, children, v);

                v.setOnClickListener(lookUpRecordOnClickLister);
                v.setTag(Utils.convert(commonPersonObject));
                return v;
            }
        };

        listView.setAdapter(baseAdapter);
        alertDialog.show();

    }
    private boolean lookedUp = true;
    private boolean isPressed = false;
    private Snackbar snackbar = null;
    public static String lookuptype = "";
    private android.support.v7.app.AlertDialog alertDialog = null;
    private final View.OnClickListener lookUpRecordOnClickLister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                CommonPersonObjectClient client = null;
                if (view.getTag() != null && view.getTag() instanceof CommonPersonObjectClient) {
                    client = (CommonPersonObjectClient) view.getTag();
                }

                if (client != null) {
                    lookupDialogDismissed(client);
                }
            }
        }
    };
    private void lookupDialogDismissed(CommonPersonObjectClient pc) {
        if (pc != null) {

            Map<String, List<View>> lookupMap = getLookUpMap();
            if (lookupMap.containsKey(lookuptype)) {
                List<View> lookUpViews = lookupMap.get(lookuptype);
                if (lookUpViews != null && !lookUpViews.isEmpty()) {

                    for (View view : lookUpViews) {

                        String key = (String) view.getTag(com.vijay.jsonwizard.R.id.key);
                        String text = getValue(pc.getColumnmaps(), "first_name", true) + " " + getValue(pc.getColumnmaps(), "last_name", true);

                        if (view instanceof MaterialEditText) {
                            MaterialEditText materialEditText = (MaterialEditText) view;
                            materialEditText.setTag(com.vijay.jsonwizard.R.id.after_look_up, true);
                            materialEditText.setText(text);
                        }
                    }
                }
            }
        }
    }
    private final Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> motherLookUpListener = data -> {
        if (lookedUp && isPressed) {
            showMotherLookUp(data);
        }
    };
    private void showMotherLookUp(final HashMap<CommonPersonObject, List<CommonPersonObject>> map) {
        if (!map.isEmpty()) {
            tapToView(map);
        } else {
            if (snackbar != null) {
                snackbar.dismiss();
            }
        }
    }
    private void tapToView(final HashMap<CommonPersonObject, List<CommonPersonObject>> map) {
        snackbar = Snackbar
                .make(getMainView(), map.size() + " match(es).", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Tap to see results", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResults(map);
                //updateResultTree(map);
            }
        });
        show(snackbar, 30000);

    }
    private void show(final Snackbar snackbar, int duration) {
        if (snackbar == null) {
            return;
        }

        float drawablePadding = getResources().getDimension(R.dimen.register_drawable_padding);
        int paddingInt = Float.valueOf(drawablePadding).intValue();

        float textSize = getActivity().getResources().getDimension(R.dimen.snack_bar_text_size);

        View snackbarView = snackbar.getView();
        snackbarView.setMinimumHeight(Float.valueOf(textSize).intValue());
        snackbarView.setBackgroundResource(R.color.snackbar_background_yellow);

        final AppCompatButton actionView = snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        actionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        actionView.setGravity(Gravity.CENTER);
        actionView.setTextColor(getResources().getColor(R.color.text_black));

        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionView.performClick();
            }
        });
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cross, 0, 0, 0);
        textView.setCompoundDrawablePadding(paddingInt);
        textView.setPadding(paddingInt, 0, 0, 0);
        textView.setTextColor(getResources().getColor(R.color.text_black));

        snackbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionView.performClick();
            }
        });

        snackbar.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.dismiss();
            }
        }, duration);

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
                WardLocation wardLocation = HALocationHelper.getInstance().getWardList().get(index);
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
                HALocation HALocation = HnppApplication.getGeoLocationRepository().getLocationByBlock(blockIdList.get(index));
                unique_id = HALocationHelper.getInstance().generateHouseHoldId(HALocation,id);// hhid.getOpenmrsId() + "");

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
                        if (!TextUtils.isEmpty(((MaterialEditText) formdataviews.get(i)).getFloatingLabelText()) && ((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("সিস্টেম নাম্বার")) {
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
