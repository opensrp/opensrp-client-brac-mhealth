package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.util.Log;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HouseHoldFormTypeFragment;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.brac.hnpp.fragment.FamilyRemoveMemberFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.view.customcontrols.CustomFontTextView;

import timber.log.Timber;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);


        String from = getIntent().getStringExtra("from");

        if(!from.isEmpty()){
            String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
            HouseHoldVisitActivity.removedMemberListJson.add(jsonString);
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
