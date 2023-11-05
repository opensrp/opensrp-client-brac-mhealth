package org.smartregister.brac.hnpp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HnppAncJsonFormFragment;
import org.smartregister.brac.hnpp.fragment.HnppFormViewFragment;
import org.smartregister.family.activity.FamilyWizardFormActivity;

public class HnppAncJsonFormActivity extends FamilyWizardFormActivity {
    @Override
    public void initializeFormFragment() {
        HnppAncJsonFormFragment jsonWizardFormFragment = HnppAncJsonFormFragment.getFormFragment("step1");
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
