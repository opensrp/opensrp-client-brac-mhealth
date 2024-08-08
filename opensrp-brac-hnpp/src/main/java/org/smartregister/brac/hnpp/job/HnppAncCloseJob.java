//package org.smartregister.brac.hnpp.job;
//
//import android.content.Intent;
//import android.support.annotation.NonNull;
//
//import org.smartregister.brac.hnpp.service.HnppAncCloseDateIntent;
//import org.smartregister.brac.hnpp.service.HnppPncCloseDateIntent;
//import org.smartregister.chw.core.job.CoreBasePncCloseJob;
//import org.smartregister.family.util.Constants;
//import org.smartregister.job.BaseJob;
//
//import timber.log.Timber;
//
//public class HnppAncCloseJob extends BaseJob {
//
//    public static final String TAG = "HnppAncCloseJob";
//
//    @NonNull
//    @Override
//    protected Result onRunJob(@NonNull Params params) {
//        Timber.v("%s started", TAG);
//        getApplicationContext().startService(new Intent(getApplicationContext(), HnppAncCloseDateIntent.class));
//        return params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
//    }
//}
