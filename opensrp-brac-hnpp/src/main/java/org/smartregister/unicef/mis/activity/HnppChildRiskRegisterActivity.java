package org.smartregister.unicef.mis.activity;

import org.smartregister.unicef.mis.fragment.HnppRiskChildRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.mis.nativation.view.NavigationMenu;
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
