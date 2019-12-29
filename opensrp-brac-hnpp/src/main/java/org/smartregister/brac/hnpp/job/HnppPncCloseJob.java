package org.smartregister.brac.hnpp.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.brac.hnpp.service.HnppPncCloseDateIntent;
import org.smartregister.chw.core.job.CoreBasePncCloseJob;
import org.smartregister.family.util.Constants;

import timber.log.Timber;

public class HnppPncCloseJob extends CoreBasePncCloseJob {

    public static final String TAG = "HnppPncCloseJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Timber.v("%s started", TAG);
        getApplicationContext().startService(new Intent(getApplicationContext(), HnppPncCloseDateIntent.class));
        return params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
