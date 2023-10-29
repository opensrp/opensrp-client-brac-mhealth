package org.smartregister.unicef.mis.job;


import android.content.Intent;
import android.support.annotation.NonNull;
import org.smartregister.AllConstants;
import org.smartregister.growthmonitoring.service.intent.ZScoreRefreshIntentService;
import org.smartregister.job.BaseJob;

public class ZScoreRefreshServiceJob extends BaseJob {
    public static final String TAG = "ZScoreRefreshServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), ZScoreRefreshIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
