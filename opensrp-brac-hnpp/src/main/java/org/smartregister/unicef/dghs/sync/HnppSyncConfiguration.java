package org.smartregister.unicef.dghs.sync;


import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.unicef.dghs.BuildConfig;

/**
 * @author Elly Nerdstone
 */
public class HnppSyncConfiguration extends SyncConfiguration {
    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public String getSyncFilterValue() {
        return "584f7ad8-34de-4498-ba56-c43d62f52c0f";
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public boolean updateClientDetailsTable() {
        return false;
    }

    @Override
    public boolean disableActionService() {
        return true;
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        super.setReadTimeout(5*60000);
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        super.setConnectTimeout(5*60000);
    }

    @Override
    public int getConnectTimeout() {
        return 5*60000;
    }

    @Override
    public int getReadTimeout() {
        return 5*60000;
    }
}
