package org.smartregister.unicef.mis.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.unicef.mis.service.HnppSyncIntentService;
import org.smartregister.job.BaseJob;

public class HnppSyncIntentServiceJob extends BaseJob {
    public static final String TAG = "HnppSyncIntentServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), HnppSyncIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
