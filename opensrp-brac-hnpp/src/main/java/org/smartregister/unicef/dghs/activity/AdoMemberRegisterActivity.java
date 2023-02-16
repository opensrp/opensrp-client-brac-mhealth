package org.smartregister.unicef.dghs.activity;


import org.smartregister.unicef.dghs.fragment.AdolescentMemberRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AdoMemberRegisterActivity extends HnppAllMemberRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AdolescentMemberRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.ADO);
        }
    }
}
