package org.smartregister.unicef.dghs.activity;

import android.content.Intent;
import android.view.View;

import org.json.JSONObject;
import org.smartregister.family.util.Constants;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.fragment.FamilyRemoveMemberFragment;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import timber.log.Timber;

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
        ((CustomFontTextView)findViewById(R.id.textview_title)).setText(R.string.remove_house_and_member);
        ((CustomFontTextView)findViewById(R.id.tvDetails)).setText(R.string.which_member_want_to_remove);
        setRemoveMemberFragment();
        startFragment();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d("JSONResult : %s", jsonString);

                JSONObject form = new JSONObject(jsonString);
                removeMemberFragment.confirmRemove(form);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
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
