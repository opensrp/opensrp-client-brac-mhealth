package org.smartregister.brac.hnpp.task;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.simprint.SimPrintsHelper;

public class ConfirmIdentificationTask extends AsyncTask<Void, Void, Void> {

    private String sessiodId;
    private String selectedGuid;

    public ConfirmIdentificationTask(String sessiodId, String selectedGuid) {
        this.sessiodId = sessiodId;
        this.selectedGuid = selectedGuid;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        confirmSelectedGuid(sessiodId, selectedGuid);
        return null;
    }


    private void confirmSelectedGuid(String sessionid, String simPrintsGuid) {
        SimPrintsHelper simPrintsHelper = new SimPrintsHelper(HnppConstants.getSimPrintsProjectId(), CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
        if (TextUtils.isEmpty(simPrintsGuid)) {
            simPrintsHelper.confirmIdentity(HnppApplication.getHNPPInstance().getApplicationContext(), sessionid, "none_selected");

        } else {
            simPrintsHelper.confirmIdentity(HnppApplication.getHNPPInstance().getApplicationContext(), sessionid, simPrintsGuid);
        }
    }
}
