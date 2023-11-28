package org.smartregister.unicef.mis.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.unicef.mis.sync.intent.HfSyncTaskIntentService;
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
            case GlobalLocationFetchJob.TAG:
                return new GlobalLocationFetchJob();
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
            case VaccineServiceJob.TAG:
                return new VaccineServiceJob();
            case LocationStructureServiceJob.TAG:
                return new LocationStructureServiceJob();
            case SyncTaskServiceJob.TAG:
                return new SyncTaskServiceJob(HfSyncTaskIntentService.class);
            case PlanIntentServiceJob.TAG:
                return new PlanIntentServiceJob();
            case VisitLogServiceJob.TAG:
                return new VisitLogServiceJob();
            case VaccineDueUpdateServiceJob.TAG:
                return new VaccineDueUpdateServiceJob();
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
            case ZScoreRefreshServiceJob.TAG:
                return new ZScoreRefreshServiceJob();
            case HeightIntentServiceJob.TAG:
                return new HeightIntentServiceJob();
            case MuactIntentServiceJob.TAG:
                return new MuactIntentServiceJob();
            case WeightIntentServiceJob.TAG:
                return new WeightIntentServiceJob();
            case CampFetchJob.TAG:
                return new CampFetchJob();
            default:
                Timber.d("Please create job and specify the right job tag");
                return null;
        }
    }
}