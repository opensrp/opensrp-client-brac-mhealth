package org.smartregister.unicef.dghs.activity;

import org.smartregister.unicef.dghs.fragment.HnppRiskChildRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.view.fragment.BaseRegisterFragment;


public class HnppChildRiskRegisterActivity extends ChildRegisterActivity {



    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppRiskChildRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.CHILD_RISK);
        }
    }
}