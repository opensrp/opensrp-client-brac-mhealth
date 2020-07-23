package org.smartregister.brac.hnpp.activity;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Tier;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.IdentityAdapter;
import org.smartregister.brac.hnpp.fragment.HnppDashBoardFragment;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.IdentityModel;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.simprint.SimPrintsConstantHelper;
import org.smartregister.simprint.SimPrintsIdentification;
import org.smartregister.simprint.SimPrintsIdentifyActivity;
import org.smartregister.simprint.SimPrintsRegistration;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

import static com.simprints.libsimprints.Tier.TIER_1;
import static com.simprints.libsimprints.Tier.TIER_2;
import static com.simprints.libsimprints.Tier.TIER_3;
import static com.simprints.libsimprints.Tier.TIER_4;
import static org.smartregister.brac.hnpp.utils.HnppConstants.MEMBER_ID_SUFFIX;

public class SimprintsIdentityActivity extends SecuredActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_IDENTIFY = 1445;
    private LinearLayout selectionBar,notFoundPanel;
    private String moduleId = "";
    private RecyclerView recyclerView;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_simprints_identity);
        selectionBar = findViewById(R.id.selection_bar);
        notFoundPanel = findViewById(R.id.not_found_panel);
        recyclerView = findViewById(R.id.recycler_view);
        findViewById(R.id.filter_apply_button).setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.back_bn).setOnClickListener(this);
        findViewById(R.id.not_found_btn).setOnClickListener(this);

        loadSSLocation();
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filter_apply_button:
                Log.v("SIMPRINTS_IDENTITY","module_id:"+moduleId);
                if(!TextUtils.isEmpty(moduleId)){
                    SimPrintsIdentifyActivity.startSimprintsIdentifyActivity(this, moduleId, REQUEST_CODE_IDENTIFY);

                }else{
                    Toast.makeText(this,"Please select module id",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.back_btn:
            case R.id.back_bn:
                finish();
                break;
            case R.id.not_found_btn:
                showNotFoundDialog();
                break;
        }
    }
    String checkedItem = "";
    private void addCheckedText(String text){
        if(TextUtils.isEmpty(checkedItem)){
            checkedItem = text;
        }else{
            checkedItem = checkedItem+","+text;
        }
    }

    private void showNotFoundDialog(){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_not_found);
        Button close_btn = dialog.findViewById(R.id.close_btn);
        Button retry_btn = dialog.findViewById(R.id.retry_btn);
        CheckBox checkBox1 = dialog.findViewById(R.id.check_box_1);
        CheckBox checkBox2 = dialog.findViewById(R.id.check_box_2);
        CheckBox checkBox3 = dialog.findViewById(R.id.check_box_3);
        CheckBox checkBox4 = dialog.findViewById(R.id.check_box_4);
        CheckBox checkBox5 = dialog.findViewById(R.id.check_box_5);
        checkBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                addCheckedText(checkBox1.getText().toString());
            }
        });
        checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                addCheckedText(checkBox2.getText().toString());
            }
        });
        checkBox3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                addCheckedText(checkBox3.getText().toString());
            }
        });
        checkBox4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                addCheckedText(checkBox4.getText().toString());
            }
        });
        checkBox5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                checkBox1.setChecked(false);
                checkBox2.setChecked(false);
                checkBox3.setChecked(false);
                checkBox4.setChecked(false);
                checkedItem = checkBox5.getText().toString();
            }
        });
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(!TextUtils.isEmpty(moduleId)){
                    SimPrintsIdentifyActivity.startSimprintsIdentifyActivity(SimprintsIdentityActivity.this, moduleId, REQUEST_CODE_IDENTIFY);

                }else{
                    Toast.makeText(SimprintsIdentityActivity.this,"Please select module id",Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();

    }
    private void openProfile(String baseEntityId){

        CommonPersonObjectClient patient = HnppDBUtils.createFromBaseEntity(baseEntityId);
        String familyId = org.smartregister.util.Utils.getValue(patient.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
        patient.getColumnmaps().put(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        String houseHoldHead = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_NAME, true);
        String address = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, true);
        String houseHoldId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_ID, true);
        String moduleId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.MODULE_ID, true);
        Intent intent = new Intent(this, HnppFamilyOtherMemberProfileActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyId);
        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, patient);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, familyId);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, familyId);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.VILLAGE_TOWN, address);
        intent.putExtra(DBConstants.KEY.UNIQUE_ID,houseHoldId);
        intent.putExtra(HnppConstants.KEY.HOUSE_HOLD_ID,moduleId);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, houseHoldHead);
        intent.putExtra(HnppFamilyOtherMemberProfileActivity.IS_COMES_IDENTITY,true);
        startActivity(intent);
        finish();

    }
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    int ssIndex;
    private void loadSSLocation(){

        ArrayList<String> ssSpinnerArray = new ArrayList<>();


        ArrayList<String> villageSpinnerArray = new ArrayList<>();


        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
        for (SSModel ssModel : ssLocationForms) {
            ssSpinnerArray.add(ssModel.username);
        }


        ArrayAdapter<String> ssSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        ssSpinnerArray){
            @Override
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };

        villageSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        villageSpinnerArray){
            @Override
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };



        Spinner ss_spinner = findViewById(R.id.ss_filter_spinner);
        Spinner village_spinner = findViewById(R.id.village_filter_spinner);
        village_spinner.setAdapter(villageSpinnerArrayAdapter);
        ss_spinner.setAdapter(ssSpinnerArrayAdapter);
        ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    villageSpinnerArray.clear();
                    ArrayList<SSLocations> ssLocations = SSLocationHelper.getInstance().getSsModels().get(position).locations;
                    for (SSLocations ssLocations1 : ssLocations) {
                        villageSpinnerArray.add(ssLocations1.village.name);
                    }
                    villageSpinnerArrayAdapter = new ArrayAdapter<String>
                            (SimprintsIdentityActivity.this, android.R.layout.simple_spinner_item,
                                    villageSpinnerArray);
                    village_spinner.setAdapter(villageSpinnerArrayAdapter);
                    ssIndex = position;
//                        villageSpinnerArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    SSLocations ssLocations = SSLocationHelper.getInstance().getSsModels().get(ssIndex).locations.get(position);
                    if(HnppConstants.isReleaseBuild()){
                        moduleId = ssLocations.city_corporation_upazila.name+"_"+ssLocations.union_ward.name;
                    }else{
                        moduleId = HnppConstants.MODULE_ID_TRAINING;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private ProgressDialog dialog;
    private void showProgressDialog(){
        dialog = new ProgressDialog(this);
        dialog.setMessage("ম্যাচ করা হচ্চে....");
        dialog.setCancelable(false);
        dialog.show();
    }
    private void hideProgressDialog(){
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
    private AppExecutors appExecutors = new AppExecutors();
    ArrayList<IdentityModel> identityModelList = new ArrayList<>();
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data !=null){

            switch (requestCode){
                case REQUEST_CODE_IDENTIFY:
                    Boolean check = data.getBooleanExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK,true);
                    if(check){
                        showProgressDialog();
                        appExecutors.diskIO().execute(() -> {
                            try {


                                ArrayList<SimPrintsIdentification> identifications = (ArrayList<SimPrintsIdentification>) data.getSerializableExtra(SimPrintsConstantHelper.INTENT_DATA);
                                identityModelList.clear();
                                for(int i= 0; i< identifications.size();i++){
                                    SimPrintsIdentification identification = identifications.get(i);
                                    Log.v("SIMPRINTS_IDENTITY","tier:"+identification.getTier()+":guid:"+identification.getGuid());
                                    switch (identification.getTier()){
                                        case TIER_1:
                                        case TIER_2:
                                        case TIER_3:
                                        case TIER_4:
                                            IdentityModel identityModel = new IdentityModel();
                                            String[] ourPut = HnppDBUtils.getBaseEntityByGuId(identification.getGuid());
                                            if(ourPut!=null && !TextUtils.isEmpty(ourPut[1])){
                                                Log.v("SIMPRINTS_IDENTITY","baseid:"+ourPut[0]+":identification.getGuid()"+identification.getGuid());
                                                identityModel.setBaseEntityId(ourPut[0]);
                                                identityModel.setName(ourPut[1]);
                                                identityModel.setTier(identification.getTier().toString().replace("_"," "));
                                                if(!TextUtils.isEmpty(ourPut[2])) {
                                                   String id = ourPut[2].replace(org.smartregister.family.util.Constants.IDENTIFIER.FAMILY_SUFFIX,"")
                                                            .replace(HnppConstants.IDENTIFIER.FAMILY_TEXT,"");
                                                    id = id.substring(id.length() - MEMBER_ID_SUFFIX);
                                                    identityModel.setGuid("ID: " + id);
                                                }
                                                if(identityModelList.size()!=3){
                                                    identityModelList.add(identityModel);
                                                }

                                            }

                                            break;

                                    }

                                }

                                appExecutors.mainThread().execute(() -> {
                                    hideProgressDialog();
                                    updateAdapter();
                                } );
                            } catch (final Exception e) {
                                appExecutors.mainThread().execute(() -> {
                                    Toast.makeText(this,"Failed to retrive",Toast.LENGTH_LONG).show();
                                });
                            }
                        });

                    }else{
                        Toast.makeText(this,"SIMPRINTS_BIOMETRICS_COMPLETE_CHECK false",Toast.LENGTH_LONG).show();
                    }

                    break;

            }
        }

    }

    private void updateAdapter() {
        if(identityModelList.size()>0){
            selectionBar.setVisibility(View.GONE);
            ((TextView)findViewById(R.id.count_tv)).setText(identityModelList.size()+" "+" টি রেজাল্ট পাওয়া গিয়েছে");
            findViewById(R.id.found_panel).setVisibility(View.VISIBLE);
            IdentityAdapter adapter = new IdentityAdapter( new IdentityAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, IdentityModel content) {
                    if(!content.getBaseEntityId().isEmpty()){
                        openProfile(content.getBaseEntityId());
                    }else {
                        Toast.makeText(SimprintsIdentityActivity.this,getString(R.string.not_match),Toast.LENGTH_SHORT).show();
                    }
                }
            });
            adapter.setData(identityModelList);
            recyclerView.setAdapter(adapter);

        }else{
            notFoundPanel.setVisibility(View.VISIBLE);
            selectionBar.setVisibility(View.GONE);
        }
    }
}
