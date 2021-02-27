package org.smartregister.brac.hnpp.job;

import android.content.Intent;
import androidx.annotation.NonNull;
import org.smartregister.AllConstants;
import org.smartregister.brac.hnpp.service.TargetFetchIntentService;
import org.smartregister.job.BaseJob;

public class TargetFetchJob extends BaseJob {
    public static final String TAG = "TargetFetchJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), TargetFetchIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
