//package org.smartregister.unicef.dghs.job;
//
//import android.content.Intent;
//import android.support.annotation.NonNull;
//
//import org.smartregister.AllConstants;
//import org.smartregister.job.BaseJob;
//import org.smartregister.unicef.dghs.service.CampFetchIntentService;
//import org.smartregister.unicef.dghs.service.OtherVaccineDueIntentService;
//
//public class OtherVaccineJob extends BaseJob {
//    public static final String TAG = "OtherVaccineJob";
//
//    @NonNull
//    @Override
//    protected Result onRunJob(@NonNull Params params) {
//        Intent intent = new Intent(getApplicationContext(), OtherVaccineDueIntentService.class);
//        getApplicationContext().startService(intent);
//        return params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
//    }
//}
