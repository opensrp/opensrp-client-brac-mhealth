package org.smartregister.brac.hnpp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.interactor.HnppChildHomeVisitInteractor;
import org.smartregister.brac.hnpp.job.HomeVisitServiceJob;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.core.activity.CoreChildHomeVisitActivity;
import org.smartregister.chw.core.interactor.CoreChildHomeVisitInteractor;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;

public class ChildHomeVisitActivity extends CoreChildHomeVisitActivity {

    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new CoreChildHomeVisitInteractor(new HnppChildHomeVisitInteractor()));
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

    @Override
    protected void displayExitDialog(Runnable onConfirm) {
        onBackPressed();
    }

    @Override
    public void submittedAndClose() {
        super.submittedAndClose();
        HomeVisitServiceJob.scheduleJobImmediately(HomeVisitServiceJob.TAG);
        HnppChildProfileActivity.startMe(this, false, memberObject, HnppChildProfileActivity.class);
    }
}
