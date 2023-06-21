package org.smartregister.unicef.dghs.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.fragment.HnppPncRegisterFragment;
import org.smartregister.unicef.dghs.listener.HnppFamilyBottomNavListener;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class HnppPncRegisterActivity extends AncRegisterActivity {

    public static void startHnppPncRegisterActivity(Activity activity, String memberBaseEntityID) {
        Intent intent = new Intent(activity, org.smartregister.unicef.dghs.activity.HnppPncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);

        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppPncRegisterFragment();
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
    public void startFormActivity(String formName, String entityId, String metaData) {

    }
    public static String getFormTable() {

        return CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME;
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, findViewById(org.smartregister.R.id.register_toolbar));
        if (menu != null) {
            menu.getNavigationAdapter()
                    .setSelectedView(CoreConstants.DrawerMenu.PNC);
        }
    }
}