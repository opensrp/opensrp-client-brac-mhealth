package org.smartregister.unicef.dghs.presenter;

import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.interactor.HnppChildProfileInteractor;
import org.smartregister.unicef.dghs.model.HnppChildRegisterModel;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.core.presenter.CoreChildProfilePresenter;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.interactor.HnppFamilyProfileInteractor;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import static org.smartregister.unicef.dghs.utils.HnppJsonFormUtils.makeReadOnlyFields;

public class HnppChildProfilePresenter extends CoreChildProfilePresenter {
    String houseHoldId = "";
    public CommonPersonObjectClient commonPersonObjectClient;

    public HnppChildProfilePresenter(CoreChildProfileContract.View childView, CoreChildProfileContract.Model model, String houseHoldId, String childBaseEntityId) {
        this.houseHoldId = houseHoldId;
        setView(new WeakReference<>(childView));
        HnppChildProfileInteractor interactor = new HnppChildProfileInteractor();
        interactor.setChildBaseEntityId(childBaseEntityId);
        setInteractor(interactor);
        setModel(model);
        setChildBaseEntityId(childBaseEntityId);
    }
    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {
        if (client == null || client.getColumnmaps() == null) {
            return;
        }
        this.commonPersonObjectClient = client;

        String motherEntityId = Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.MOTHER_ENTITY_ID, false);
        String relationId = Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
        String motherName = Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.CHILD_MOTHER_NAME, true);

        motherName = HnppDBUtils.getMotherName(motherEntityId,relationId,motherName);
        String parentName = view.get().getContext().getResources().getString(org.smartregister.chw.core.R.string.care_giver_initials,motherName);
        getView().setParentName(parentName);
        String firstName = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String middleName = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String childName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);
        String ageStr = org.smartregister.family.util.Utils.getTranslatedDate(org.smartregister.family.util.Utils.getDuration(org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false)), view.get().getContext());
        String age = view.get().getContext().getResources().getString(org.smartregister.chw.core.R.string.age,ageStr);
        getView().setProfileName(childName);
        getView().setAge(age);

        dob = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);

        //dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
        String address = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.FAMILY_HOME_ADDRESS, true);
        String gender = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.GENDER, true);
        String getGender = HnppConstants.getGender(gender);
        getView().setAddress(address);
        getView().setGender(getGender+", "+parentName);

        String uniqueId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
        uniqueId = String.format(getView().getString(org.smartregister.family.R.string.unique_id_text), uniqueId);
        getView().setId(uniqueId);


        getView().setProfileImage(client.getCaseId());

    }

    @Override
    public void fetchProfileData() {
        super.fetchProfileData();
    }

    @Override
    public void verifyHasPhone() {
        //new HnppFamilyProfileInteractor().verifyHasPhone(familyID, this);
    }

    @Override
    public void startFormForEdit(String title, CommonPersonObjectClient client) {
        try {
            JSONObject form;

            form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getChildRegister(), getView().getApplicationContext(), client, CoreConstants.EventType.UPDATE_CHILD_REGISTRATION);


            ArrayList<String> womenList = HnppDBUtils.getAllWomenInHouseHold(familyID);
            HnppJsonFormUtils.updateFormWithMotherName(form,womenList,familyID);
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

    @Override
    public void updateChildProfile(String jsonString) {
        getView().showProgressDialog(R.string.updating);
        Pair<Client, Event> pair = new HnppChildRegisterModel(houseHoldId,familyID).processRegistration(jsonString);
        if (pair == null) {
            getView().hideProgressDialog();
            getView().errorOccured("হাউসহোল্ড এড্রেস খুঁজে পাওয়া যায়নি। হাউসহোল্ড এডিট করুন");
            return;
        }

        getInteractor().saveRegistration(pair, jsonString, true, this);
    }


}
