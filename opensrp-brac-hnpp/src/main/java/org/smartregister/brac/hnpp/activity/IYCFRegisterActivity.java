package org.smartregister.brac.hnpp.activity;
import org.smartregister.brac.hnpp.fragment.IYCFRegisterFragment;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.fragment.BaseRegisterFragment;


public class IYCFRegisterActivity extends ChildRegisterActivity {



    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new IYCFRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.IYCF);
        }
    }
}
