package org.smartregister.brac.hnpp.presenter;

import android.text.TextUtils;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.model.HnppChildRegisterModel;
import org.smartregister.brac.hnpp.model.HnppFamilyRegisterModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.chw.core.presenter.CoreFamilyProfilePresenter;
import org.smartregister.brac.hnpp.interactor.HnppFamilyProfileInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.view.activity.BaseProfileActivity;

import java.util.List;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.makeReadOnlyFields;

public class FamilyProfilePresenter extends CoreFamilyProfilePresenter {
    String houseHoldId = "";
    HnppChildRegisterModel childProfileModel;
    BaseProfileActivity baseProfileActivity;
    public FamilyProfilePresenter(FamilyProfileExtendedContract.View loginView, FamilyProfileContract.Model model, String houseHoldId, String familyBaseEntityId, String familyHead, String primaryCaregiver, String familyName) {
        super(loginView, model, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
        baseProfileActivity =(BaseProfileActivity) loginView;
        this.houseHoldId = houseHoldId;
        interactor = new HnppFamilyProfileInteractor();
        getChildRegisterModel();
        verifyHasPhone();
    }
    public void updateHouseIdAndModuleId(String houseHoldId){
        this.houseHoldId = houseHoldId;
    }

    @Override
    public void verifyHasPhone() {
        ((HnppFamilyProfileInteractor)interactor).verifyHasPhone(familyBaseEntityId,this);
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient client) {
        HnppConstants.getGPSLocation(baseProfileActivity, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    JSONObject form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getFamilyDetailsRegister(), getView().getApplicationContext(), client, Utils.metadata().familyRegister.updateEventType);
                    String ssName = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.SS_NAME, false);
                    String villageName = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, false);
                    HnppJsonFormUtils.updateFormWithSSName(form, SSLocationHelper.getInstance().getSsModels());
                    HnppJsonFormUtils.updateFormWithVillageName(form,ssName,villageName);
                    if(HnppConstants.isPALogin())makeReadOnlyFields(form);
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitudeFamily(form,latitude,longitude);
                    }catch (Exception e){

                    }
                    getView().startFormActivity(form);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
                getView().errorOccured("হাউসহোল্ড এড্রেস খুঁজে পাওয়া যায়নি। হাউসহোল্ড এডিট করুন");
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
            if (getView() != null) getView().errorOccured("হাউসহোল্ড এড্রেস খুঁজে পাওয়া যায়নি। হাউসহোল্ড এডিট করুন");
            return;
        }

        interactor.saveRegistration(familyEventClientList.get(0), jsonString, true, this);
    }

    @Override
    protected CoreChildRegisterModel getChildRegisterModel() {
        childProfileModel = new HnppChildRegisterModel(houseHoldId,familyBaseEntityId);
        return childProfileModel;
    }

}
