package org.smartregister.unicef.mis.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.fragment.HnppAncJsonFormFragment;
import org.smartregister.family.activity.FamilyWizardFormActivity;

public class HnppAncJsonFormActivity extends FamilyWizardFormActivity {
    @Override
    public void initializeFormFragment() {
        boolean isNeedToShowSaveButton = getIntent().getBooleanExtra("IS_NEED_SAVE",true);
        HnppAncJsonFormFragment jsonWizardFormFragment = HnppAncJsonFormFragment.getFormFragment("step1",isNeedToShowSaveButton);
        this.getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, jsonWizardFormFragment).commit();
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage(R.string.form_back_confirm_dialog_message)
                .setTitle(R.string.form_back_confirm_dialog_title).setCancelable(false)
                .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                }).setNegativeButton(R.string.no_button_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

}
