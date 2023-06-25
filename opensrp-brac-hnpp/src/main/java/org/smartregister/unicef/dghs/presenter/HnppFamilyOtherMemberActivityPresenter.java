package org.smartregister.unicef.dghs.presenter;

import static org.smartregister.util.Utils.getName;

import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.anc.util.NCUtils;

import org.smartregister.chw.core.contract.CoreChildRegisterContract;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.interactor.CoreChildRegisterInteractor;
import org.smartregister.chw.core.interactor.CoreFamilyInteractor;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.presenter.BaseFamilyOtherMemberProfileActivityPresenter;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.model.HnppChildRegisterModel;
import org.smartregister.unicef.dghs.model.HnppFamilyProfileModel;
import org.smartregister.chw.core.contract.FamilyOtherMemberProfileExtendedContract;
import org.smartregister.unicef.dghs.interactor.HnppFamilyInteractor;
import org.smartregister.unicef.dghs.interactor.HnppFamilyProfileInteractor;
import org.smartregister.family.contract.FamilyOtherMemberContract;
import org.smartregister.family.contract.FamilyProfileContract;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HnppFamilyOtherMemberActivityPresenter extends BaseFamilyOtherMemberProfileActivityPresenter implements FamilyOtherMemberProfileExtendedContract.Presenter, FamilyProfileContract.InteractorCallBack, FamilyProfileExtendedContract.PresenterCallBack, CoreChildRegisterContract.InteractorCallBack {
    protected FamilyProfileContract.Interactor profileInteractor;
    protected FamilyProfileContract.Model profileModel;
    protected HnppFamilyInteractor familyInteractor;
    private WeakReference<FamilyOtherMemberProfileExtendedContract.View> viewReference;
    private String familyBaseEntityId;
    private String familyName;

    public HnppFamilyOtherMemberActivityPresenter(FamilyOtherMemberProfileExtendedContract.View view,
                                                  FamilyOtherMemberContract.Model model, String viewConfigurationIdentifier,
                                                  String familyBaseEntityId, String baseEntityId, String familyHead,
                                                  String primaryCaregiver, String villageTown, String familyName) {
        super(view, model, viewConfigurationIdentifier, baseEntityId, familyHead, primaryCaregiver, villageTown);
        viewReference = new WeakReference<>(view);
        this.familyBaseEntityId = familyBaseEntityId;
        this.familyName = familyName;

        this.profileInteractor = getFamilyProfileInteractor();
        this.profileModel = getFamilyProfileModel(familyName);
        setProfileInteractor();
        verifyHasPhone();
        initializeServiceStatus();
    }
    @Override
    public void verifyHasPhone() {
        // ((CoreFamilyProfileInteractor) profileInteractor).verifyHasPhone(familyBaseEntityId, this);
    }
    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }

    public String getFamilyName() {
        return familyName;
    }
    @Override
    public void notifyHasPhone(boolean hasPhone) {
        if (viewReference.get() != null) {
            viewReference.get().updateHasPhone(hasPhone);
        }
    }
    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {
        super.refreshProfileTopSection(client);
        if (client != null && client.getColumnmaps() != null) {
            String firstName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
            String middleName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
            String lastName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);

            String dob = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, true);
            int age = StringUtils.isNotBlank(dob) ? Utils.getAgeFromDate(dob) : 0;
            String ageStr = org.smartregister.family.util.Utils.getTranslatedDate(org.smartregister.family.util.Utils.getDuration(dob),getView().getContext());

            this.getView().setProfileName(MessageFormat.format("{0}, {1}", getName(getName(firstName, middleName), lastName), ageStr));
            String gestationAge = HnppApplication.ancRegisterRepository().getGaIfAncWoman(client.getCaseId());
            if (gestationAge != null) {
                this.getView().setProfileDetailOne(NCUtils.gestationAgeString(gestationAge, viewReference.get().getContext(), true));
            }
        }
    }
    public void updateFamilyMember(String jsonString) {

        try {
            getView().showProgressDialog(org.smartregister.family.R.string.saving_dialog_title);

            FamilyEventClient familyEventClient = profileModel.processUpdateMemberRegistration(jsonString, familyBaseEntityId);
            if (familyEventClient == null) {
                getView().hideProgressDialog();
                return;
            }
            profileInteractor.saveRegistration(familyEventClient, jsonString, true, this);
        } catch (Exception e) {
            getView().hideProgressDialog();
            Timber.e(e);
        }
    }
    public void saveChildForm(String jsonString) {
        try {

            getView().showProgressDialog(org.smartregister.chw.core.R.string.saving_dialog_title);

            Pair<Client, Event> pair = new HnppChildRegisterModel("",familyBaseEntityId).processRegistration(jsonString);
            if (pair == null) {
                getView().hideProgressDialog();
                return;
            }
            new CoreChildRegisterInteractor().saveRegistration(pair, jsonString, false, this);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId, String familyId) {

    }

    public void startFormForEdit(CommonPersonObjectClient commonPersonObject) {
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
        //TODO Implement
        Timber.d("onUniqueIdFetched unimplemented");
    }

    @Override
    public void onNoUniqueId() {
        //TODO Implement
        Timber.d("onNoUniqueId unimplemented");
    }

    @Override
    public void onRegistrationSaved(boolean isEditMode) {
        onRegistrationSaved(isEditMode,"");
    }

    @Override
    public void onRegistrationSaved(boolean isEditMode, String baseEntityId) {
        if (isEditMode) {
            getView().hideProgressDialog();

            refreshProfileView();

            getView().refreshList();
        }else{
            getView().hideProgressDialog();
            getView().openProfile(baseEntityId);
        }
    }

    public FamilyOtherMemberProfileExtendedContract.View getView() {
        if (viewReference != null) {
            return viewReference.get();
        } else {
            return null;
        }
    }
    private void initializeServiceStatus() {
        familyInteractor.updateFamilyDueStatus(viewReference.get().getContext(), "", familyBaseEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.v("initializeServiceStatus onSubscribe");
                    }

                    @Override
                    public void onNext(String s) {
                        updateFamilyMemberServiceDue(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("initializeServiceStatus " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Timber.v("initializeServiceStatus onComplete");
                    }
                });
    }
    protected HnppFamilyProfileInteractor getFamilyProfileInteractor() {
        if (profileInteractor == null) {
            this.profileInteractor = new HnppFamilyProfileInteractor();
        }
        return (HnppFamilyProfileInteractor) profileInteractor;
    }

    protected FamilyProfileContract.Model getFamilyProfileModel(String familyName) {
        if (profileModel == null) {
            this.profileModel = new HnppFamilyProfileModel(familyName,null,null,null);
        }
        return profileModel;
    }

    protected void setProfileInteractor() {
        if (familyInteractor == null) {
            familyInteractor = new HnppFamilyInteractor();
        }
    }
    @Override
    public void updateFamilyMemberServiceDue(String serviceDueStatus) {
        if (getView() != null) {
            getView().setFamilyServiceStatus(serviceDueStatus);
        }

    }
}
