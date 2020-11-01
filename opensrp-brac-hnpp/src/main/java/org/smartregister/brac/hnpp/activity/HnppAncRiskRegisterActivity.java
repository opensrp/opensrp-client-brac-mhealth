package org.smartregister.brac.hnpp.activity;

import org.smartregister.brac.hnpp.fragment.HnppRiskAncRegisterFragment;
import org.smartregister.chw.anc.interactor.BaseAncRegisterInteractor;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.view.fragment.BaseRegisterFragment;


public class HnppAncRiskRegisterActivity extends HnppAncRegisterActivity {

    @Override
    protected void initializePresenter() {
        presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new BaseAncRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppRiskAncRegisterFragment();
    }
}
