package org.smartregister.unicef.mis.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.unicef.mis.service.GuestMemberIdIntentService;
import org.smartregister.family.util.Constants;
import org.smartregister.job.BaseJob;

public class PullGuestMemberIdServiceJob extends BaseJob {

    public static final String TAG = "PullGuestMemberIdServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), GuestMemberIdIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
