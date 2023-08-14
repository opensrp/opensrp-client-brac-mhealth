package org.smartregister.unicef.dghs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.v4.app.Fragment;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.core.contract.CoreChildRegisterContract;
import org.smartregister.chw.core.listener.CoreBottomNavigationListener;
import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.listener.HnppFamilyBottomNavListener;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.unicef.dghs.presenter.HnppChildRegisterPresenter;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.fragment.HnppChildRegisterFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;
import java.util.Arrays;
import java.util.List;
import timber.log.Timber;

public class ChildRegisterActivity extends BaseRegisterActivity implements CoreChildRegisterContract.View {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppChildRegisterFragment();
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
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().familyFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        Form form = new Form();
        form.setName(getString(R.string.add_family));
        form.setSaveLabel(getString(R.string.save));
        form.setWizard(true);
        if(!HnppConstants.isReleaseBuild()){
            form.setActionBarBackground(R.color.test_app_color);

        }else{
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

        }
        if(HnppConstants.isPALogin()){
            form.setHideSaveLabel(true);
            form.setSaveLabel("");
        }
        form.setNavigationBackground(org.smartregister.family.R.color.family_navigation);
        form.setHomeAsUpIndicator(org.smartregister.family.R.mipmap.ic_cross_white);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.core.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.core.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.core.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.core.R.id.action_library);

            bottomNavigationView.inflateMenu(org.smartregister.chw.core.R.menu.bottom_nav_family_menu);

            bottomNavigationHelper.disableShiftMode(bottomNavigationView);

            CoreBottomNavigationListener childBottomNavigationListener = new CoreBottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(childBottomNavigationListener);

        }

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
        bottomNavigationView.setOnNavigationItemSelectedListener(new HnppFamilyBottomNavListener(this, bottomNavigationView));
    }

    @Override
    protected void initializePresenter() {
        presenter = new HnppChildRegisterPresenter(this, new CoreChildRegisterModel());
    }
    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.CHILD_CLIENTS);
        }
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            if (mBaseFragment instanceof HnppChildRegisterFragment) {
                String locationId = Utils.context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                presenter().startForm(formName, entityId, metaData, locationId, "");
            }
        } catch (Exception e) {
            Timber.e(e);
            displayToast(getString(org.smartregister.chw.core.R.string.error_unable_to_start_form));
        }
    }
    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d("JSONResult : %s", jsonString);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.registerEventType)
                        || form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.CHILD_REGISTRATION)
                ) {
                    presenter().saveForm(jsonString, false);
                }
            } catch (Exception e) {
                Timber.e(e);
            }

        }
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(Utils.metadata().familyRegister.config);
    }

    @Override
    public CoreChildRegisterContract.Presenter presenter() {
        return (CoreChildRegisterContract.Presenter) presenter;
    }

    @Override
    public void openFamilyListView() {
        bottomNavigationView.setSelectedItemId(org.smartregister.chw.core.R.id.action_family);
    }

    @Override
    public void startRegistration() {
        startFormActivity(Utils.metadata().familyRegister.formName, null, null);
    }
}
