package org.smartregister.brac.hnpp.activity;


import org.smartregister.brac.hnpp.fragment.WomenServiceRegisterFragment;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class WomenServiceRegisterActivity extends HnppAllMemberRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new WomenServiceRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.WOMEN);
        }
    }
}
