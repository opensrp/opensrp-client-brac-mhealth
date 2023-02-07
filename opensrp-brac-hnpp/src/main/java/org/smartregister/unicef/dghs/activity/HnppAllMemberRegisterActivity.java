package org.smartregister.unicef.dghs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.fragment.HnppAllMemberRegisterFragment;
import org.smartregister.unicef.dghs.listener.HnppFamilyBottomNavListener;
import org.smartregister.unicef.dghs.location.GeoLocationHelper;
import org.smartregister.unicef.dghs.location.SSModel;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.activity.CoreChildRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.ArrayList;

public class HnppAllMemberRegisterActivity extends CoreChildRegisterActivity {

    @Override
    public void onBackPressed() {
        Fragment fragment = findFragmentByPosition(currentPage);
        if (fragment instanceof BaseRegisterFragment) {
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients);
            BaseRegisterFragment registerFragment = (BaseRegisterFragment) fragment;
            if (registerFragment.onBackPressed()) {
                return;
            }
        }

        backToHomeScreen();
        setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients);
    }
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppAllMemberRegisterFragment();
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }
    public void backToHomeScreen() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(HnppConstants.KEY_NEED_TO_OPEN,true);
        startActivity(intent);
        finish();
    }
    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
        bottomNavigationView.setOnNavigationItemSelectedListener(new HnppFamilyBottomNavListener(this, bottomNavigationView));
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.ALL_MEMBER);
        }
    }

}
