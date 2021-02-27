package org.smartregister.brac.hnpp.job;

import android.content.Intent;
import androidx.annotation.NonNull;
import org.smartregister.brac.hnpp.service.GuestMemberIdIntentService;
import org.smartregister.brac.hnpp.sync.intent.PullHouseholdIdIntentService;
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
