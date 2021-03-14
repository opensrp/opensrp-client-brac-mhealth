package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.job.HnppPncCloseJob;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.job.SSLocationFetchJob;
import org.smartregister.brac.hnpp.job.PullHouseholdIdsServiceJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.job.HomeVisitServiceJob;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.view.contract.BaseLoginContract;


public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {
    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    protected void scheduleJobsPeriodically() {
//        HnppSyncIntentServiceJob.scheduleJob(HnppSyncIntentServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
//                BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));
//        NotificationGeneratorJob.scheduleJob(NotificationGeneratorJob.TAG, TimeUnit.MINUTES.toMinutes(
//                BuildConfig.NOTIFICATION_GENERATE), getFlexValue(BuildConfig.NOTIFICATION_GENERATE));
//
//        VaccineRecurringServiceJob.scheduleJob(VaccineRecurringServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
//                BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES), getFlexValue(BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES));
//
//        try{
//            HomeVisitServiceJob.scheduleJob(HomeVisitServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
//                    BuildConfig.HOME_VISIT_MINUTES), getFlexValue(BuildConfig.HOME_VISIT_MINUTES));
//        }catch (Exception e){
//
//        }
////
////        SyncTaskServiceJob.scheduleJob(SyncTaskServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
////                BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

    }

    @Override
    protected void scheduleJobsImmediately() {
        try{
            PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
            SSLocationFetchJob.scheduleJobImmediately(SSLocationFetchJob.TAG);
            HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
            HomeVisitServiceJob.scheduleJobImmediately(HomeVisitServiceJob.TAG);
            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
            VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
            VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);

        }catch (Exception e){

        }

    }
}
