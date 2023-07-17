package org.smartregister.brac.hnpp.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.brac.hnpp.sync.intent.HfSyncTaskIntentService;
import org.smartregister.job.CompareDataServiceJob;
import org.smartregister.job.DataSyncByBaseEntityServiceJob;
import org.smartregister.job.ExtendedSyncServiceJob;
import org.smartregister.job.ForceSyncDataServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.job.LocationStructureServiceJob;
import org.smartregister.job.PlanIntentServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncTaskServiceJob;
import org.smartregister.job.ValidateSyncDataServiceJob;

import timber.log.Timber;

/**
 *
 */
public class HnppJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SSLocationFetchJob.TAG:
                return new SSLocationFetchJob();
            case HnppSyncIntentServiceJob.TAG:
                return new HnppSyncIntentServiceJob();
            case HHVisitDurationFetchJob.TAG:
                return new HHVisitDurationFetchJob();
            case EventFetchJob.TAG:
                return new EventFetchJob();
//            case HnppHomeVisitServiceJob.TAG:
//                return new HnppHomeVisitServiceJob();
            case ExtendedSyncServiceJob.TAG:
                return new ExtendedSyncServiceJob();
            case PullUniqueIdsServiceJob.TAG:
                return new PullUniqueIdsServiceJob();
            case PullHouseholdIdsServiceJob.TAG:
                return new PullHouseholdIdsServiceJob();
            case ValidateSyncDataServiceJob.TAG:
                return new ValidateSyncDataServiceJob();
            case InValidateSyncDataServiceJob.TAG:
                return new InValidateSyncDataServiceJob();
            case DataSyncByBaseEntityServiceJob.TAG:
                return new DataSyncByBaseEntityServiceJob();
            case ForceSyncDataServiceJob.TAG:
                return new ForceSyncDataServiceJob();
            case CompareDataServiceJob.TAG:
                return new CompareDataServiceJob();
            case ImageUploadServiceJob.TAG:
                return new ImageUploadServiceJob();
            case VaccineRecurringServiceJob.TAG:
                return new VaccineRecurringServiceJob();
            case LocationStructureServiceJob.TAG:
                return new LocationStructureServiceJob();
            case SyncTaskServiceJob.TAG:
                return new SyncTaskServiceJob(HfSyncTaskIntentService.class);
            case PlanIntentServiceJob.TAG:
                return new PlanIntentServiceJob();
            case VisitLogServiceJob.TAG:
                return new VisitLogServiceJob();
            case HnppPncCloseJob.TAG:
                return new HnppPncCloseJob();
            case TargetFetchJob.TAG:
                return new TargetFetchJob();
            case StockFetchJob.TAG:
                return new StockFetchJob();
            case MigrationFetchJob.TAG:
                return new MigrationFetchJob();
            case PullGuestMemberIdServiceJob.TAG:
                return new PullGuestMemberIdServiceJob();
            case NotificationGeneratorJob.TAG:
                return new NotificationGeneratorJob();
            case DataDeleteJob.TAG:
                return new DataDeleteJob();
            case SurveyHistoryJob.TAG:
                return new SurveyHistoryJob();
            default:
                Timber.d("Please create job and specify the right job tag");
                return null;
        }
    }
}
