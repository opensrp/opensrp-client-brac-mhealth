package org.smartregister.unicef.mis.listener;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.smartregister.unicef.mis.activity.NewDashBoardActivity;
import org.smartregister.unicef.mis.activity.QRScannerActivity;
import org.smartregister.unicef.mis.activity.ReferralMemberRegisterActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

public class HnppFamilyBottomNavListener extends org.smartregister.family.listener.FamilyBottomNavigationListener {
    private Activity context;
    private BottomNavigationView bottomNavigationView;

    public HnppFamilyBottomNavListener(Activity context, BottomNavigationView bottomNavigationView) {
        super(context);
        this.context = context;
        this.bottomNavigationView = bottomNavigationView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) this.context;

        if (item.getItemId() == org.smartregister.family.R.id.action_register) {
            bottomNavigationView.setSelectedItemId(org.smartregister.family.R.id.action_family);
            baseRegisterActivity.startRegistration();
            return false;

        }else if (item.getItemId() == org.smartregister.family.R.id.action_job_aids) {
           // bottomNavigationView.setSelectedItemId(org.smartregister.family.R.id.action_job_aids);
            Intent intent = new Intent(baseRegisterActivity, ReferralMemberRegisterActivity.class);
//            Intent intent = new Intent(baseRegisterActivity, NewDashBoardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            baseRegisterActivity.startActivity(intent);
            return true;
        }else if(item.getItemId() == org.smartregister.family.R.id.action_scan_qr){
            baseRegisterActivity.startActivity(new Intent(baseRegisterActivity, QRScannerActivity.class));
        }
        else {
            super.onNavigationItemSelected(item);
        }

        return true;
    }
}
