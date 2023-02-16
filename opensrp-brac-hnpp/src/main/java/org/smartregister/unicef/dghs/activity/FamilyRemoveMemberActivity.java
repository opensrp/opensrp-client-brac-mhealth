package org.smartregister.unicef.dghs.activity;

import android.view.View;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.fragment.FamilyRemoveMemberFragment;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class FamilyRemoveMemberActivity  extends SecuredActivity implements View.OnClickListener  {

    protected FamilyRemoveMemberFragment removeMemberFragment;
    protected void setRemoveMemberFragment() {
        this.removeMemberFragment = FamilyRemoveMemberFragment.newInstance(getIntent().getExtras());
    }
    @Override
    protected void onResumption() {
        //Overridden
    }
    @Override
    protected void onCreation() {
        setContentView(org.smartregister.chw.core.R.layout.activity_family_remove_member);
        // set up views
        findViewById(org.smartregister.chw.core.R.id.close).setOnClickListener(this);
        ((CustomFontTextView)findViewById(R.id.textview_title)).setText("খানা/সদস্য বাতিল করুন");
        ((CustomFontTextView)findViewById(R.id.tvDetails)).setText("আপনি কোন সদস্যটিকে সরাতে চান?");
        setRemoveMemberFragment();
        startFragment();
    }
    private void startFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(org.smartregister.chw.core.R.id.flFrame, removeMemberFragment)
                .commit();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == org.smartregister.chw.core.R.id.close) {
            finish();
        }
    }

}
