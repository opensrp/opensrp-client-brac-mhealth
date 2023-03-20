package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.listener.HnppFamilyBottomNavListener;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.activity.CoreChildRegisterActivity;
import org.smartregister.brac.hnpp.fragment.HnppChildRegisterFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.ArrayList;

public class ChildRegisterActivity extends CoreChildRegisterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
        if(ssLocationForms.size() > 0){
            boolean simPrintsEnable = ssLocationForms.get(0).simprints_enable;
            if(simPrintsEnable){
                findViewById(R.id.simprints_identity).setVisibility(View.VISIBLE);
                findViewById(R.id.simprints_identity).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChildRegisterActivity.this, SimprintsIdentityActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(org.smartregister.chw.core.R.anim.slide_in_up, org.smartregister.chw.core.R.anim.slide_out_up);
                    }
                });
            }else{
                findViewById(R.id.simprints_identity).setVisibility(View.GONE);
            }
//            boolean paymentEnable = ssLocationForms.get(0).payment_enable;
//            if(paymentEnable){
                findViewById(R.id.payment_view).setVisibility(View.VISIBLE);
//            }else{
//                findViewById(R.id.payment_view).setVisibility(View.GONE);
//            }
        }
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
        form.setWizard(false);
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
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
        bottomNavigationView.setOnNavigationItemSelectedListener(new HnppFamilyBottomNavListener(this, bottomNavigationView));
    }
}
