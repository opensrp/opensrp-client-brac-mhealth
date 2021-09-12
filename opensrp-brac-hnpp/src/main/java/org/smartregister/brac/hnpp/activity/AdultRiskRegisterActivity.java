package org.smartregister.brac.hnpp.activity;

import org.smartregister.brac.hnpp.fragment.AdultRiskRegisterFragment;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AdultRiskRegisterActivity extends AdultMemberRegisterActivity {
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AdultRiskRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, findViewById(org.smartregister.R.id.register_toolbar));
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.ADULT_RISK);
        }
    }
}
