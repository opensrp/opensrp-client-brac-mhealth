package org.smartregister.unicef.dghs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.smartregister.unicef.dghs.location.GeoLocationHelper;
import org.smartregister.unicef.dghs.location.SSModel;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.unicef.dghs.R;
import org.smartregister.helper.BottomNavigationHelper;

import java.util.ArrayList;

public class AncRegisterActivity extends CoreAncRegisterActivity {
    @Override
    public void onRegistrationSaved(boolean isEdit) {
        /*if (hasChildRegistration) {
            startReferralsRegisterActivity(PncRegisterActivity.class);
        } else*/
        if (!hasChildRegistration) {
            startRegisterActivity(AncRegisterActivity.class);
        }
        finish();
    }
    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }



    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void startRegisterActivity(Class registerClass) {
        Intent intent = new Intent(this, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        this.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        this.finish();
    }
}
