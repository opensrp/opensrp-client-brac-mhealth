package org.smartregister.unicef.mis.presenter;

import android.util.Log;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.domain.FetchStatus;
import org.smartregister.unicef.mis.contract.FamilyRegisterInteractorCallBack;
import org.smartregister.unicef.mis.interactor.HnppFamilyRegisterInteractor;
import org.smartregister.unicef.mis.model.HnppFamilyRegisterModel;
import org.smartregister.family.contract.FamilyRegisterContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.presenter.BaseFamilyRegisterPresenter;

import java.util.List;

import timber.log.Timber;

public class FamilyRegisterPresenter  extends BaseFamilyRegisterPresenter implements FamilyRegisterInteractorCallBack {
    protected String baseEntityId;
    HnppFamilyRegisterInteractor interactor;
    public FamilyRegisterPresenter(FamilyRegisterContract.View view, FamilyRegisterContract.Model model) {
        super(view, model);
        interactor  = new HnppFamilyRegisterInteractor();
    }
    public String getBaseEntityId() {
        return baseEntityId;
    }
    @Override
    public void onRegistrationSaved(boolean isEditMode, String baseEntityId) {
        Log.v("OPEN_PROFILE","<<<onRegistrationSaved>>>"+baseEntityId);
        if (getView() != null) {
            this.baseEntityId = baseEntityId;
            this.getView().refreshList(FetchStatus.fetched);
            this.getView().hideProgressDialog();
        }

    }
    @Override
    public void saveForm(String jsonString, boolean isEditMode) {
        try {

//            if (getView() != null)
//                getView().showProgressDialog(R.string.saving);

            List<FamilyEventClient> familyEventClientList = ((HnppFamilyRegisterModel)model).processRegistration(jsonString);
            if (familyEventClientList == null || familyEventClientList.isEmpty()) {
                if (getView() != null) getView().hideProgressDialog();
                return;
            }

            interactor.saveRegistration(familyEventClientList, jsonString, isEditMode, new FamilyRegisterInteractorCallBack() {
                @Override
                public void onRegistrationSaved(boolean isEditMode, String baseId) {
                    Log.v("OPEN_PROFILE","onRegistrationSaved>>"+baseId);
                    if (getView() != null) {
                        baseEntityId = baseId;
                        getView().refreshList(FetchStatus.fetched);
                        getView().hideProgressDialog();
                    }
                }

                @Override
                public void onUniqueIdFetched(Triple<String, String, String> triple, String s) {

                }

                @Override
                public void onNoUniqueId() {

                }

                @Override
                public void onRegistrationSaved(boolean b) {
                    getView().refreshList(FetchStatus.fetched);
                    getView().hideProgressDialog();
                }
            });

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private FamilyRegisterContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }

}
