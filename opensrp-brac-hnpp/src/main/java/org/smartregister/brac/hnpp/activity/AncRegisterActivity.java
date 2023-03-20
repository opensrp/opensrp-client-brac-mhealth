package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.brac.hnpp.R;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

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
                        Intent intent = new Intent(AncRegisterActivity.this, SimprintsIdentityActivity.class);
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
