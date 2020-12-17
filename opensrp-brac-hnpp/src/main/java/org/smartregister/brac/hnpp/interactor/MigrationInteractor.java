package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.family.util.AppExecutors;

public class MigrationInteractor  {
    private static final String MIGRATION_POST = "/rest/event/migrate?";
    private AppExecutors appExecutors;
    public MigrationInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public void migrateHH(MigrationSearchContentData migrationSearchContentData, MigrationContract.MigrationPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            boolean isSuccess = postHHData();
            if(isSuccess){
                appExecutors.mainThread().execute(callBack::onSuccess);
            }else{
                appExecutors.mainThread().execute(callBack::onFail);
            }

        };
        appExecutors.diskIO().execute(runnable);


    }

    private boolean postHHData() {
        return true;
    }
}
