package org.smartregister.unicef.mis.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.contract.FamilyRegisterInteractorCallBack;
import org.smartregister.unicef.mis.contract.GuestMemberContract;
import org.smartregister.unicef.mis.fragment.HnppAllMemberRegisterFragment;
import org.smartregister.unicef.mis.interactor.HnppFamilyProfileInteractor;
import org.smartregister.unicef.mis.listener.HnppFamilyBottomNavListener;
import org.smartregister.unicef.mis.model.GlobalLocationModel;
import org.smartregister.unicef.mis.model.HnppFamilyProfileModel;
import org.smartregister.unicef.mis.nativation.presenter.HnppNavigationPresenter;
import org.smartregister.unicef.mis.nativation.view.NavigationMenu;
import org.smartregister.unicef.mis.presenter.GuestMemberPresenter;
import org.smartregister.unicef.mis.repository.GlobalLocationRepository;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class HnppAllMemberRegisterActivity extends ChildRegisterActivity implements GuestMemberContract.View {
    private GuestMemberPresenter presenter;
    @Override
    public void onBackPressed() {
       finish();
    }
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppAllMemberRegisterFragment();
    }

    NavigationMenu navigationMenu;
    HnppNavigationPresenter hnppNavigationPresenter;
    @Override
    protected void onCreation() {
        super.onCreation();
        navigationMenu = NavigationMenu.getInstance(this, null, null);
        hnppNavigationPresenter = new HnppNavigationPresenter( HnppApplication.getHNPPInstance(),navigationMenu,HnppApplication.getHNPPInstance().getHnppNavigationModel());
        HnppApplication.getHNPPInstance().setupNavigation(hnppNavigationPresenter);
        presenter = new GuestMemberPresenter(this);
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.ALL_MEMBER);
        }
    }
    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, HnppAllMemberRegisterActivity.class);
        startActivity(intent);
        finish();
    }
    public void backToHomeScreen() {
        Intent intent = new Intent(this, HnppAllMemberRegisterActivity.class);
        intent.putExtra(HnppConstants.KEY_NEED_TO_OPEN,true);
        startActivity(intent);
        finish();
    }
    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
        bottomNavigationView.getMenu().getItem(1).setTitle("Member registration");
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_family);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);
        //bottomNavigationView.getMenu().getItem(2).setTitle("Referred List");
        bottomNavigationView.setOnNavigationItemSelectedListener(new HnppFamilyBottomNavListener(this, bottomNavigationView));
    }
    @Override
    public void startRegistration() {
        AddMemberSearchActivity.startMigrationFilterActivity(HnppAllMemberRegisterActivity.this,false);

    }
    String idType,hid,firstName,dob,gender,baseEntityId,nationalId,birthRegistrationID,father_name_english,mother_name_english,mobileNo,shr_id;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==AddMemberSearchActivity.REQUEST_CODE && resultCode == RESULT_OK){
            if(data.getBooleanExtra("offline_reg",false)){
                startFormActivity(false);
                return;
            }
            idType = data.getStringExtra("idType");
            hid = data.getStringExtra("hid");
            firstName =data.getStringExtra("firstName");
            dob =data.getStringExtra("dob");
            gender =data.getStringExtra("gender");
            baseEntityId =data.getStringExtra("baseEntityId");
            nationalId =data.getStringExtra("nationalId");
            birthRegistrationID = data.getStringExtra("birthRegistrationID")+"";
            father_name_english =data.getStringExtra("father_name_english")+"";
            mother_name_english = data.getStringExtra("mother_name_english")+"";
            mobileNo = data.getStringExtra("Mobile_Number")+"";
            shr_id = !TextUtils.isEmpty(hid)?hid:data.getStringExtra("shr_id");
            startFormActivity(true);


        }
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject formWithConsent = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(formWithConsent);
                presenter.saveMember(formWithConsent.toString(),false);

            }catch (JSONException e){

            }
        }
    }
    private void startFormActivity(boolean isNeedToUpdateData){
        try{
            Intent intent = new Intent(HnppAllMemberRegisterActivity.this, GuestAddMemberJsonFormActivity.class);
            JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM);
            if(isNeedToUpdateData)HnppJsonFormUtils.updateMemberInformationFromSearch(jsonForm,firstName,shr_id,mobileNo,getString(R.string.yes),dob,gender,nationalId,birthRegistrationID,mother_name_english,father_name_english);

            JSONArray divJsonArray = new JSONArray();
            ArrayList<GlobalLocationModel> divModels = HnppApplication.getGlobalLocationRepository().getLocationByTagId(GlobalLocationRepository.LOCATION_TAG.DIVISION.getValue());
            for (GlobalLocationModel globalLocationModel:divModels){
                divJsonArray.put(globalLocationModel.name);
            }
            HnppJsonFormUtils.updateFormWithUnionName(jsonForm, HnppApplication.getHALocationRepository().getAllUnion());
            //HnppJsonFormUtils.updateFormWithDivision(jsonForm, divJsonArray);
            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
            Form form = new Form();
            form.setWizard(true);
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void updateAdapter() {

    }

    @Override
    public void updateSuccessfullyFetchMessage() {

    }

    @Override
    public GuestMemberContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void openProfile(String baseEntityId) {

    }
}
