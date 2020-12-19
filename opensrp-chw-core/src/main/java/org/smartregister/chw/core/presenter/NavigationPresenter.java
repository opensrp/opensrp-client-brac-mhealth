package org.smartregister.chw.core.presenter;

import android.app.Activity;

import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.interactor.NavigationInteractor;
import org.smartregister.chw.core.job.CoreBasePncCloseJob;
import org.smartregister.chw.core.job.HomeVisitServiceJob;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.model.NavigationSubModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncTaskServiceJob;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class NavigationPresenter implements NavigationContract.Presenter {

    private NavigationContract.Model mModel;
    private NavigationContract.Interactor mInteractor;
    private WeakReference<NavigationContract.View> mView;
    protected HashMap<String, String> tableMap = new HashMap<>();

    public NavigationPresenter(CoreApplication application, NavigationContract.View view, NavigationModel.Flavor modelFlavor) {
        mView = new WeakReference<>(view);

        mInteractor = NavigationInteractor.getInstance();
        mInteractor.setApplication(application);

        mModel = NavigationModel.getInstance();
        mModel.setNavigationFlavor(modelFlavor);

        initialize();
    }

    protected void initialize() {
        tableMap.put(CoreConstants.DrawerMenu.ALL_FAMILIES, CoreConstants.TABLE_NAME.FAMILY);
        tableMap.put(CoreConstants.DrawerMenu.ALL_MEMBER,  CoreConstants.TABLE_NAME.FAMILY_MEMBER);
        tableMap.put(CoreConstants.DrawerMenu.ELCO_CLIENT,"test");
        tableMap.put(CoreConstants.DrawerMenu.ADULT,"adult");
        tableMap.put(CoreConstants.DrawerMenu.FORUM,"");
        tableMap.put(CoreConstants.DrawerMenu.GUEST_MEMBER,"");
        tableMap.put(CoreConstants.DrawerMenu.CHILD_CLIENTS, CoreConstants.TABLE_NAME.CHILD);
        tableMap.put(CoreConstants.DrawerMenu.ANC_CLIENTS, CoreConstants.TABLE_NAME.ANC_MEMBER);
        tableMap.put(CoreConstants.DrawerMenu.ANC, CoreConstants.TABLE_NAME.ANC_MEMBER);
        tableMap.put(CoreConstants.DrawerMenu.ANC_RISK, "anc_risk");
        tableMap.put(CoreConstants.DrawerMenu.PNC_RISK, "pnc_risk");
        tableMap.put(CoreConstants.DrawerMenu.ELCO_RISK, "elco_risk");
        tableMap.put(CoreConstants.DrawerMenu.CHILD_RISK, "child_risk");
        tableMap.put(CoreConstants.DrawerMenu.ADULT_RISK, "adult_risk");
        tableMap.put(CoreConstants.DrawerMenu.PNC, CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME);
        tableMap.put(CoreConstants.DrawerMenu.REFERRALS, CoreConstants.TABLE_NAME.TASK);
        tableMap.put(CoreConstants.DrawerMenu.MALARIA, CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION);
    }

    public HashMap<String, String> getTableMap() {
        return tableMap;
    }

    public void setTableMap(HashMap<String, String> tableMap) {
        this.tableMap = tableMap;
    }

    @Override
    public NavigationContract.View getNavigationView() {
        return mView.get();
    }


    @Override
    public void refreshNavigationCount(final Activity activity) {

        int x = 0;
        while (x < mModel.getNavigationItems().size()) {

            final int finalX = x;
            NavigationOption option = mModel.getNavigationItems().get(x);
            if(option.isNeedToExpand()){
                mInteractor.getRegisterCount(tableMap.get(option.getNavigationSubModel().getType()), new NavigationContract.InteractorCallback<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        mModel.getNavigationItems().get(finalX).getNavigationSubModel().setSubCount(result);
                        getNavigationView().refreshCount();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
            mInteractor.getRegisterCount(tableMap.get(option.getMenuTitle()), new NavigationContract.InteractorCallback<Integer>() {
                @Override
                public void onResult(Integer result) {
                    mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                    getNavigationView().refreshCount();
                }

                @Override
                public void onError(Exception e) {
                    // getNavigationView().displayToast(activity, "Error retrieving count for " + tableMap.get(mModel.getNavigationItems().get(finalX).getMenuTitle()));
                    Timber.e("Error retrieving count for %s", tableMap.get(mModel.getNavigationItems().get(finalX).getMenuTitle()));
                }
            });
            x++;
        }

    }


    @Override
    public void refreshLastSync() {
        // get last sync date
        getNavigationView().refreshLastSync(mInteractor.sync());
    }

    @Override
    public void displayCurrentUser() {
        getNavigationView().refreshCurrentUser(mModel.getCurrentUser());
    }

//    @Override
//    public void sync(Activity activity) {
//        CoreBasePncCloseJob.scheduleJobImmediately(CoreBasePncCloseJob.TAG);
//        HomeVisitServiceJob.scheduleJobImmediately(HomeVisitServiceJob.TAG);
//        VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);
//        ImageUploadServiceJob.scheduleJobImmediately(ImageUploadServiceJob.TAG);
//        SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
//        PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
//        //PlanIntentServiceJob.scheduleJobImmediately(PlanIntentServiceJob.TAG);
//        SyncTaskServiceJob.scheduleJobImmediately(SyncTaskServiceJob.TAG);
//    }
    @Override
    public void sync(Activity activity) {
        SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
        PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);

    }

    @Override
    public void covid19(Activity activity) {

    }

    @Override
    public void forceSync(Activity activity) {

    }

    @Override
    public void browseSSInfo(Activity activity) {

    }

    @Override
    public void browseNotification(Activity activity) {

    }

    @Override
    public void browseMigration(Activity activity) {

    }

    @Override
    public void browsePayment(Activity activity) {

    }

    @Override
    public List<NavigationOption> getOptions() {
        return mModel.getNavigationItems();
    }


}
