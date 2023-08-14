package org.smartregister.unicef.dghs.presenter;

import android.text.TextUtils;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.utils.CoreChildService;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Task;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.interactor.HnppChildProfileInteractor;
import org.smartregister.unicef.dghs.model.HnppChildRegisterModel;
import org.smartregister.unicef.dghs.utils.HnppDBConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.unicef.dghs.R;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import static org.smartregister.unicef.dghs.utils.HnppJsonFormUtils.makeReadOnlyFields;

import timber.log.Timber;

public class HnppChildProfilePresenter implements CoreChildProfileContract.Presenter, CoreChildProfileContract.InteractorCallBack, FamilyProfileExtendedContract.PresenterCallBack  {
    String houseHoldId = "";
    public CommonPersonObjectClient commonPersonObjectClient;
    public CoreChildProfileContract.Model model;
    public String childBaseEntityId;
    public String familyID;
    protected WeakReference<CoreChildProfileContract.View> view;
    private HnppChildProfileInteractor interactor;
    protected String dob;
    private String familyName;
    private String familyHeadID;
    private String primaryCareGiverID;
    private FormUtils formUtils;

    public HnppChildProfilePresenter(CoreChildProfileContract.View childView, CoreChildProfileContract.Model model, String houseHoldId, String childBaseEntityId) {
        this.houseHoldId = houseHoldId;
        this.view = new WeakReference<>(childView);
        interactor = new HnppChildProfileInteractor();
        interactor.setChildBaseEntityId(childBaseEntityId);
        this.model = model;
        this.childBaseEntityId = childBaseEntityId;
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

        String shrId = Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.SHR_ID, false);
        if(!TextUtils.isEmpty(shrId)){
            getView().setId(shrId);
        }else{
            String uniqueId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);

            uniqueId = String.format(getView().getString(org.smartregister.family.R.string.unique_id_text), uniqueId);
            getView().setId(uniqueId);
        }




        getView().setProfileImage(client.getCaseId());

    }

    @Override
    public void fetchProfileData() {
        interactor.refreshProfileView(childBaseEntityId, false, this);
    }

    @Override
    public void verifyHasPhone() {
        //new HnppFamilyProfileInteractor().verifyHasPhone(familyID, this);
    }

    @Override
    public void startFormForEdit(String title, CommonPersonObjectClient client) {
        try {
            JSONObject form;

            form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getChildDetailsRegister(), getView().getApplicationContext(), client, CoreConstants.EventType.UPDATE_CHILD_REGISTRATION);

            HnppJsonFormUtils.updateFormWithBlockInfo(form,familyID);
            HnppJsonFormUtils.readOnlyChildDOb(form);
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
            getView().errorOccured(HnppApplication.getInstance().getApplicationContext().getString(R.string.household_address_not_found_edit));
            return;
        }

        interactor.saveRegistration(pair, jsonString, true, this);
    }
    public String getFamilyID() {
        return familyID;
    }

    @Override
    public void setFamilyID(String familyID) {
        this.familyID = familyID;
        verifyHasPhone();
    }
    @Override
    public void notifyHasPhone(boolean hasPhone) {
        if (view.get() != null) {
            view.get().updateHasPhone(hasPhone);
        }
    }
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyHeadID() {
        return familyHeadID;
    }
    @Override
    public void setFamilyHeadID(String familyHeadID) {
        this.familyHeadID = familyHeadID;
    }

    public String getPrimaryCareGiverID() {
        return primaryCareGiverID;
    }

    @Override
    public void setPrimaryCareGiverID(String primaryCareGiverID) {
        this.primaryCareGiverID = primaryCareGiverID;
    }
    @Override
    public void updateVisitNotDone() {
        hideProgressBar();
        getView().openVisitMonthView();

    }

    @Override
    public void updateAfterBackGroundProcessed() {
        if (getView() != null) {
            getView().updateAfterBackgroundProcessed();
        }
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (getView() != null) {
            getView().setClientTasks(taskList);
        }
    }
    @Override
    public CoreChildProfileContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }
    @Override
    public void fetchTasks() {
        // // TODO: 08/08/19  Change to use correct plan id
        interactor.getClientTasks("5270285b-5a3b-4647-b772-c0b3c52e2b71", childBaseEntityId, this);
    }

    @Override
    public void updateChildCommonPerson(String baseEntityId) {
        interactor.updateChildCommonPerson(baseEntityId);
    }

    @Override
    public void updateVisitNotDone(long value) {
        interactor.updateVisitNotDone(value, this);
    }

    @Override
    public void undoVisitNotDone() {
        hideProgressBar();
        getView().showUndoVisitNotDoneView();
    }

    @Override
    public void fetchVisitStatus(String baseEntityId) {
        interactor.refreshChildVisitBar(view.get().getContext(), childBaseEntityId, this);
    }

    @Override
    public void fetchUpcomingServiceAndFamilyDue(String baseEntityId) {
        interactor.refreshUpcomingServiceAndFamilyDue(view.get().getContext(), getFamilyId(), childBaseEntityId, this);
    }

    public String getFamilyId() {
        return familyID;
    }

    @Override
    public void processBackGroundEvent() {
        interactor.processBackGroundEvent(this);

    }

    @Override
    public void createSickChildEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception {
        interactor.createSickChildEvent(allSharedPreferences, jsonString);
    }

    public void setView(WeakReference<CoreChildProfileContract.View> view) {
        this.view = view;
    }
    @Override
    public void updateChildVisit(ChildVisit childVisit) {
        if (childVisit != null) {
            if (childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.DUE.name())) {
                getView().setVisitButtonDueStatus();
            }
            if (childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.OVERDUE.name())) {
                getView().setVisitButtonOverdueStatus();
            }
            if (childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.LESS_TWENTY_FOUR.name())) {
                getView().setVisitLessTwentyFourView(childVisit.getLastVisitMonthName());
            }
            if (childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.VISIT_THIS_MONTH.name())) {
                getView().setVisitAboveTwentyFourView();
            }
            if (childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.NOT_VISIT_THIS_MONTH.name())) {
                getView().setVisitNotDoneThisMonth();
            }
            if (childVisit.getLastVisitTime() != 0) {
                getView().setLastVisitRowView(childVisit.getLastVisitDays());
            }
            if (!childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.NOT_VISIT_THIS_MONTH.name()) && childVisit.getLastVisitTime() != 0) {
                getView().enableEdit(new Period(new DateTime(childVisit.getLastVisitTime()), DateTime.now()).getHours() <= 24);
            }

        }

    }

    @Override
    public void updateChildService(CoreChildService childService) {
        if (getView() != null) {
            if (childService != null) {
                if (childService.getServiceStatus().equalsIgnoreCase(CoreConstants.ServiceType.UPCOMING.name())) {
                    getView().setServiceNameUpcoming(childService.getServiceName().trim(), childService.getServiceDate());
                } else if (childService.getServiceStatus().equalsIgnoreCase(CoreConstants.ServiceType.OVERDUE.name())) {
                    getView().setServiceNameOverDue(childService.getServiceName().trim(), childService.getServiceDate());
                } else {
                    getView().setServiceNameDue(childService.getServiceName().trim(), childService.getServiceDate());
                }
            } else {
                getView().setServiceNameDue("", "");
            }
        }
    }

    @Override
    public void updateFamilyMemberServiceDue(String serviceDueStatus) {
        if (getView() != null) {
            if (serviceDueStatus.equalsIgnoreCase(CoreConstants.FamilyServiceType.DUE.name())) {
                getView().setFamilyHasServiceDue();
            } else if (serviceDueStatus.equalsIgnoreCase(CoreConstants.FamilyServiceType.OVERDUE.name())) {
                getView().setFamilyHasServiceOverdue();
            } else {
                getView().setFamilyHasNothingDue();
            }
        }

    }
    @Override
    public void startSickChildReferralForm() {
        try {
            getView().startFormActivity(HnppJsonFormUtils.getJsonObject(CoreConstants.JSON_FORM.getChildReferralForm()));
        } catch (Exception e) {
            Timber.e(e);
        }
    }
    @Override
    public void onRegistrationSaved(boolean isEditMode) {
        if (isEditMode) {
            getView().hideProgressDialog();
            getView().refreshProfile(FetchStatus.fetched);
        }
    }

    public FormUtils getFormUtils() throws Exception {
        if (this.formUtils == null) {
            this.formUtils = new FormUtils(getView().getApplicationContext());
        }
        return formUtils;
    }
    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        view = null;//set to null on destroy

        // Inform interactor
        interactor.onDestroy(isChangingConfiguration);

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            interactor = null;
        }
    }
    @Override
    public void hideProgressBar() {
        if (getView() != null) {
            getView().hideProgressBar();
        }
    }
    public CommonPersonObjectClient getChildClient() {
        return interactor.getpClient();
    }
    public String getDateOfBirth() {
        return dob;
    }
}
