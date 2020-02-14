package org.smartregister.brac.hnpp.presenter;

import org.smartregister.chw.anc.contract.BaseAncRegisterContract;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;

import timber.log.Timber;


public class HnppBaseAncRegisterPresenter extends BaseAncRegisterPresenter {
    public HnppBaseAncRegisterPresenter(BaseAncRegisterContract.View view, BaseAncRegisterContract.Model model, BaseAncRegisterContract.Interactor interactor) {
        super(view, model, interactor);
    }
    @Override
    public void saveForm(String jsonString, boolean isEditMode, String table) {
        try {
            interactor.saveRegistration(jsonString, isEditMode, this, table);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
