package org.smartregister.brac.hnpp.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.brac.hnpp.service.HHVisitDurationFetchIntentService;
import org.smartregister.brac.hnpp.service.SSLocationFetchIntentService;
import org.smartregister.job.BaseJob;

public class HHVisitDurationFetchJob extends BaseJob {
    public static final String TAG = "HHVisitDurationFetchJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), HHVisitDurationFetchIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
