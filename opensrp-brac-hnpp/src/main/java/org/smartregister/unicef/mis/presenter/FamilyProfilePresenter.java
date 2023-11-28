package org.smartregister.unicef.mis.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.CoreChildRegisterContract;
import org.smartregister.chw.core.domain.FamilyMember;
import org.smartregister.chw.core.interactor.CoreChildRegisterInteractor;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.presenter.BaseFamilyProfilePresenter;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.contract.FamilyRegisterInteractorCallBack;
import org.smartregister.unicef.mis.model.HnppChildRegisterModel;
import org.smartregister.unicef.mis.model.HnppFamilyRegisterModel;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.unicef.mis.interactor.HnppFamilyProfileInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.view.LocationPickerView;
import org.smartregister.view.activity.BaseProfileActivity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.unicef.mis.utils.HnppJsonFormUtils.makeReadOnlyFields;

public class FamilyProfilePresenter extends BaseFamilyProfilePresenter implements FamilyProfileExtendedContract.Presenter, CoreChildRegisterContract.InteractorCallBack, FamilyProfileExtendedContract.PresenterCallBack {
    String houseHoldId = "";
    HnppChildRegisterModel childProfileModel;
    BaseProfileActivity baseProfileActivity;
    WeakReference<FamilyProfileExtendedContract.View> viewReference;
    protected String baseEntityId;
    protected CoreChildRegisterInteractor childRegisterInteractor;
    public FamilyProfilePresenter(FamilyProfileExtendedContract.View loginView, FamilyProfileContract.Model model, String houseHoldId, String familyBaseEntityId, String familyHead, String primaryCaregiver, String familyName) {
        super(loginView, model, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
        viewReference = new WeakReference<>(loginView);
        childRegisterInteractor = new CoreChildRegisterInteractor();
        childProfileModel = new HnppChildRegisterModel(houseHoldId,familyBaseEntityId);
        baseProfileActivity =(BaseProfileActivity) loginView;
        this.houseHoldId = houseHoldId;
        interactor = new HnppFamilyProfileInteractor();
        getChildRegisterModel();
        verifyHasPhone();
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void updateHouseIdAndModuleId(String houseHoldId){
        this.houseHoldId = houseHoldId;
    }

    public void verifyHasPhone() {
        ((HnppFamilyProfileInteractor)interactor).verifyHasPhone(familyBaseEntityId,this);
    }
    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {
        if (client != null && client.getColumnmaps() != null) {
            String firstName = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), "first_name", true);

            String lastName = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), "last_name", true);
            String famName;
            String villageTown;
            if (org.smartregister.family.util.Utils.getBooleanProperty("family.head.first.name.enabled")) {
                villageTown = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), "family_head_name", true);
                famName = this.getView().getApplicationContext().getString(org.smartregister.family.R.string.family_profile_title_with_firstname, villageTown, firstName);
            } else {
                famName = this.getView().getApplicationContext().getString(org.smartregister.family.R.string.family_profile_title, firstName+" "+lastName);
            }

            this.getView().setProfileName(famName);
            villageTown = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), "block_name", false);
            this.getView().setProfileDetailOne(villageTown);
            this.getView().setProfileImage(client.getCaseId());
        }
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient client) {
//        HnppConstants.getGPSLocation(baseProfileActivity, new OnPostDataWithGps() {
//            @Override
//            public void onPost(double latitude, double longitude) {
                try {
                    JSONObject form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getFamilyDetailsRegister(), getView().getApplicationContext(), client, Utils.metadata().familyRegister.updateEventType);
                    HnppJsonFormUtils.updateFormWithUnionName(form, HnppApplication.getHALocationRepository().getAllUnion());
                    if(HnppConstants.isPALogin())makeReadOnlyFields(form);
//                    try{
//                        HnppJsonFormUtils.updateLatitudeLongitude(form,latitude,longitude,familyBaseEntityId);
//                    }catch (Exception e){
//
//                    }
                    getView().startFormActivity(form);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }
//        });

    }
    public void startMemberProfileForMigration(CommonPersonObjectClient client){
        try {
            JSONObject form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(HnppConstants.JSON_FORM.getFamilyMemberRegister(), getView().getApplicationContext(), client, Utils.metadata().familyMemberRegister.updateEventType);
            String moduleId = HnppDBUtils.getModuleId(familyHead);
            HnppJsonFormUtils.updateFormWithChampType(form,moduleId,familyBaseEntityId);
            if (!StringUtils.isBlank(client.getColumnmaps().get(ChildDBConstants.KEY.RELATIONAL_ID))) {
                JSONObject metaDataJson = form.getJSONObject("metadata");
                JSONObject lookup = metaDataJson.getJSONObject("look_up");
                lookup.put("entity_id", "family");
                lookup.put("value", client.getColumnmaps().get(ChildDBConstants.KEY.RELATIONAL_ID));
            }
            getView().startFormActivity(form);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void startChildProfileForMigration(CommonPersonObjectClient client, String familyBaseEntityId){
        try {
            JSONObject form;

            form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getChildRegister(), getView().getApplicationContext(), client, CoreConstants.EventType.UPDATE_CHILD_REGISTRATION);

            HnppJsonFormUtils.updateFormWithBlockInfo(form,familyBaseEntityId);
            if (!StringUtils.isBlank(client.getColumnmaps().get(ChildDBConstants.KEY.RELATIONAL_ID))) {
                JSONObject metaDataJson = form.getJSONObject("metadata");
                JSONObject lookup = metaDataJson.getJSONObject("look_up");
                lookup.put("entity_id", "family");
                lookup.put("value", client.getColumnmaps().get(ChildDBConstants.KEY.RELATIONAL_ID));
            }
            if(HnppConstants.isPALogin()) makeReadOnlyFields(form);
            getView().startFormActivity(form);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startChildFromWithMotherInfo(String motherEntityId) throws Exception {
        JSONObject form = HnppJsonFormUtils.getJsonObject(CoreConstants.JSON_FORM.CHILD_REGISTER);
        Map<String, String> womenInfo = HnppDBUtils.getMotherName(motherEntityId);

        HnppJsonFormUtils.updateFormWithMotherName(form,womenInfo.get("first_name")+" "+womenInfo.get("last_name"),womenInfo.get("member_name_bengla"),motherEntityId,familyBaseEntityId,womenInfo.get("phone_number"));
        HnppJsonFormUtils.updateFormWithBlockInfo(form,familyBaseEntityId);
        HnppJsonFormUtils.updateFormWithMemberId(form,houseHoldId,familyBaseEntityId);
        HnppJsonFormUtils.updateChildFormWithMetaData(form, houseHoldId,familyBaseEntityId);
        getView().startFormActivity(form);
    }

    @Override
    public void startChildForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        if (StringUtils.isBlank(entityId)) {
            Triple<String, String, String> triple = Triple.of(formName, metadata, currentLocationId);
            childRegisterInteractor.getNextUniqueId(triple, this, familyBaseEntityId);
            return;
        }
        if(TextUtils.isEmpty(familyBaseEntityId)){
            getView().errorOccured("familyBaseEntityId null");
            return;
        }

        JSONObject form = childProfileModel.getFormAsJson(formName, entityId, currentLocationId, familyBaseEntityId);
        getView().startFormActivity(form);
    }
    @Override
    public void saveChildForm(String jsonString, boolean isEditMode) {
        try {

            getView().showProgressDialog(org.smartregister.chw.core.R.string.saving_dialog_title);

            Pair<Client, Event> pair = getChildRegisterModel().processRegistration(jsonString);
            if (pair == null) {
                getView().hideProgressDialog();
                getView().errorOccured(baseProfileActivity.getString(R.string.household_not_found_edit));
                return;
            }
            saveChildRegistration(pair, jsonString, isEditMode, this);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void updateFamilyRegister(String jsonString) {

        List<FamilyEventClient> familyEventClientList = new HnppFamilyRegisterModel().processRegistration(jsonString);
        if (familyEventClientList == null || familyEventClientList.isEmpty()) {
            if (getView() != null) getView().hideProgressDialog();
            if (getView() != null) getView().errorOccured(baseProfileActivity.getString(R.string.household_not_found_edit));
            return;
        }

        interactor.saveRegistration(familyEventClientList.get(0), jsonString, true, this);
    }
    protected CoreChildRegisterModel getChildRegisterModel() {
        childProfileModel = new HnppChildRegisterModel(houseHoldId,familyBaseEntityId);
        return childProfileModel;
    }
    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId, String familyId) {
        try {
            startChildForm(triple.getLeft(), entityId, triple.getMiddle(), triple.getRight());
        } catch (Exception e) {
            Timber.e(e);
            getView().displayToast(org.smartregister.chw.core.R.string.error_unable_to_start_form);
        }
    }
    @Override
    public void notifyHasPhone(boolean hasPhone) {
        if (viewReference.get() != null) {
            viewReference.get().updateHasPhone(hasPhone);
        }
    }
    @Override
    public FamilyProfileExtendedContract.View getView() {
        if (viewReference != null) {
            return viewReference.get();
        } else {
            return null;
        }
    }

    @Override
    public void saveChildRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode, CoreChildRegisterContract.InteractorCallBack callBack) {
        childRegisterInteractor.saveRegistration(pair, jsonString, isEditMode, this);
    }

    @Override
    public void onRegistrationSaved(boolean isEdit, String baseEntityId) {
        this.baseEntityId = baseEntityId;
        getView().refreshMemberList(FetchStatus.fetched);
        getView().hideProgressDialog();
    }

    @Override
    public String saveChwFamilyMember(String jsonString) {
        org.smartregister.util.Utils.appendLog("SAVE_VISIT","saveChwFamilyMember>>familyBaseEntityId:"+familyBaseEntityId);

        if(TextUtils.isEmpty(familyBaseEntityId)) return null;
        try {
            getView().showProgressDialog(org.smartregister.family.R.string.saving_dialog_title);

            FamilyEventClient familyEventClient = model.processMemberRegistration(jsonString, familyBaseEntityId);
            if (familyEventClient == null) {
                getView().hideProgressDialog();
                return null;
            }
            org.smartregister.util.Utils.appendLog("SAVE_VISIT","familyEventClient>>baseentityid:"+familyEventClient.getClient().getBaseEntityId());

//            interactor.saveRegistration(familyEventClient, jsonString, false, this);
            ((HnppFamilyProfileInteractor)interactor).saveRegistration(familyEventClient, jsonString, false, new FamilyRegisterInteractorCallBack() {
                @Override
                public void onRegistrationSaved(boolean isEditMode, String baseId) {
                    baseEntityId = baseId;
                    getView().refreshMemberList(FetchStatus.fetched);
                    getView().hideProgressDialog();
                }

                @Override
                public void onUniqueIdFetched(Triple<String, String, String> triple, String s) {

                }

                @Override
                public void onNoUniqueId() {

                }

                @Override
                public void onRegistrationSaved(boolean b) {
                    getView().refreshMemberList(FetchStatus.fetched);
                    getView().hideProgressDialog();
                }
            });
            return familyEventClient.getClient().getBaseEntityId();
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public boolean updatePrimaryCareGiver(Context context, String jsonString, String familyBaseEntityId, String entityID) {
        boolean res = false;
        try {
            FamilyMember member = CoreJsonFormUtils.getFamilyMemberFromRegistrationForm(jsonString, familyBaseEntityId, entityID);
            if (member != null && member.getPrimaryCareGiver()) {
                LocationPickerView lpv = new LocationPickerView(context);
                lpv.init();
                res = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return res;
    }
}