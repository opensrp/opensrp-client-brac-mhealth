package org.smartregister.unicef.dghs.activity;

import org.smartregister.unicef.dghs.fragment.HnppRiskAncRegisterFragment;
import org.smartregister.chw.anc.interactor.BaseAncRegisterInteractor;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
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
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.ANC_RISK);
        }
    }
}
