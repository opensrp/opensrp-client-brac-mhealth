//package org.smartregister.unicef.dghs.job;
//
//import android.content.Intent;
//import android.support.annotation.NonNull;
//
//import org.smartregister.unicef.dghs.service.HnppHomeVisitIntentService;
//import org.smartregister.family.util.Constants;
//import org.smartregister.job.BaseJob;
//
//import timber.log.Timber;
//
//public class HnppHomeVisitServiceJob extends BaseJob {
//    public static final String TAG = "HnppHomeVisitServiceJob";
//
//    @NonNull
//    @Override
//    protected Result onRunJob(@NonNull Params params) {
//        Timber.v("%s started", TAG);
//        getApplicationContext().startService(new Intent(getApplicationContext(), HnppHomeVisitIntentService.class));
//        return params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
//    }
//}
