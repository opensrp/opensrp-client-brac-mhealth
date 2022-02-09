package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.view.contract.BaseLoginContract;
import java.util.concurrent.TimeUnit;


public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {
    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    protected void scheduleJobsPeriodically() {
//        SyncServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
//                BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));
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
        InValidateSyncDataServiceJob.scheduleJob(InValidateSyncDataServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.INVALID_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig
                .INVALID_SYNC_DURATION_MINUTES));

    }

    @Override
    protected void scheduleJobsImmediately() {


    }
}
