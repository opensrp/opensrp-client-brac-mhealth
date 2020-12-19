package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HnppAllMemberRegisterFragment;
import org.smartregister.brac.hnpp.listener.HnppFamilyBottomNavListener;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.activity.CoreChildRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.ArrayList;

public class HnppAllMemberRegisterActivity extends CoreChildRegisterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!HnppConstants.isPALogin()){
            ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
            if(ssLocationForms.size() > 0){
                boolean simPrintsEnable = ssLocationForms.get(0).simprints_enable;
                if(simPrintsEnable){
                    findViewById(R.id.simprints_identity).setVisibility(View.VISIBLE);
                    findViewById(R.id.simprints_identity).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(HnppAllMemberRegisterActivity.this, SimprintsIdentityActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransition(org.smartregister.chw.core.R.anim.slide_in_up, org.smartregister.chw.core.R.anim.slide_out_up);
                        }
                    });
                }else{
                    findViewById(R.id.simprints_identity).setVisibility(View.GONE);
                }
            }
        }else{
            findViewById(R.id.simprints_identity).setVisibility(View.GONE);
            findViewById(R.id.ss_info_browse).setVisibility(View.GONE);
            findViewById(R.id.migration_view).setVisibility(View.GONE);
            findViewById(R.id.sk_change).setVisibility(View.VISIBLE);
            findViewById(R.id.sk_change).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HnppAllMemberRegisterActivity.this, SkSelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(SkSelectionActivity.IS_COMES_FROM_UPDATE,true);
                    startActivity(intent);
                    overridePendingTransition(org.smartregister.chw.core.R.anim.slide_in_up, org.smartregister.chw.core.R.anim.slide_out_up);
                }
            });
        }

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
