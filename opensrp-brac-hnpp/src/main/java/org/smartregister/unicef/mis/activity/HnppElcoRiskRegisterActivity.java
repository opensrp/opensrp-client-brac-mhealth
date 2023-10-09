package org.smartregister.unicef.mis.activity;

import org.smartregister.unicef.mis.fragment.HnppRiskElcoMemberRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.mis.nativation.view.NavigationMenu;
import org.smartregister.view.fragment.BaseRegisterFragment;


public class HnppElcoRiskRegisterActivity extends HnppElcoMemberRegisterActivity {



    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppRiskElcoMemberRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.ELCO_RISK);
        }
    }
}
