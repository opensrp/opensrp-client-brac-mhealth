package org.smartregister.unicef.mis.activity;
import org.smartregister.unicef.mis.fragment.IYCFRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.mis.nativation.view.NavigationMenu;
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
