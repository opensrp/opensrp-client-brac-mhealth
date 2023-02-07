package org.smartregister.unicef.dghs.activity;

import org.smartregister.unicef.dghs.R;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.unicef.dghs.fragment.FamilyRemoveMemberFragment;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class FamilyRemoveMemberActivity extends CoreFamilyRemoveMemberActivity {

    @Override
    protected void setRemoveMemberFragment() {
        this.removeMemberFragment = FamilyRemoveMemberFragment.newInstance(getIntent().getExtras());
    }
    @Override
    protected void onCreation() {
        super.onCreation();
        ((CustomFontTextView)findViewById(R.id.textview_title)).setText("খানা/সদস্য বাতিল করুন");
        ((CustomFontTextView)findViewById(R.id.tvDetails)).setText("আপনি কোন সদস্যটিকে সরাতে চান?");

//        getActionBar().setTitle("খানা/সদস্য বাতিল করুন");
    }

}
