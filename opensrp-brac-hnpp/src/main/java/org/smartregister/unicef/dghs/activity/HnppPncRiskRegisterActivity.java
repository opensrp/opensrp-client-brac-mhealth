package org.smartregister.unicef.dghs.activity;

import org.smartregister.unicef.dghs.fragment.HnppRiskAncRegisterFragment;
import org.smartregister.unicef.dghs.fragment.HnppRiskPncRegisterFragment;
import org.smartregister.chw.anc.interactor.BaseAncRegisterInteractor;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.fragment.BaseRegisterFragment;


public class HnppPncRiskRegisterActivity extends HnppPncRegisterActivity{

    @Override
    protected void initializePresenter() {
        presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new BaseAncRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppRiskPncRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, findViewById(org.smartregister.R.id.register_toolbar));
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.PNC_RISK);
        }
    }
}
