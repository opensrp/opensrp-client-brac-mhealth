package org.smartregister.unicef.dghs.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.unicef.dghs.service.NotificationGeneratorIntentService;
import org.smartregister.job.BaseJob;

public class NotificationGeneratorJob extends BaseJob {
    public static final String TAG = "NotificationGeneratorJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), NotificationGeneratorIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
