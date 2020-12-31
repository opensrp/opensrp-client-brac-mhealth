package org.smartregister.chw.core.contract;

import android.app.Activity;

import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.model.NavigationOption;

import java.util.Date;
import java.util.List;

public interface NavigationContract {

    interface Presenter {

        NavigationContract.View getNavigationView();

        void refreshNavigationCount(Activity activity);

        void refreshLastSync();

        void displayCurrentUser();

        void sync(Activity activity);
        void covid19(Activity activity);
        void forceSync(Activity activity);
        void browseSSInfo(Activity activity);
        void browseNotification(Activity activity);
        void browseMigration(Activity activity);
        void browsePayment(Activity activity);
        void browseDashboard(Activity activity);
        List<NavigationOption> getOptions();
    }

    interface View {

        void prepareViews(Activity activity);

        void refreshLastSync(Date lastSync);

        void refreshCurrentUser(String name);

        void logout(Activity activity);

        void refreshCount();

        void displayToast(Activity activity, String message);
    }

    interface Model {

        void setNavigationFlavor(NavigationModel.Flavor flavor);

        List<NavigationOption> getNavigationItems();

        String getCurrentUser();
    }

    interface Interactor {

        Date getLastSync();

        void getRegisterCount(String tableName, InteractorCallback<Integer> callback);

        Date sync();

        void setApplication(CoreApplication coreApplication);
    }

    interface InteractorCallback<T> {
        void onResult(T result);

        void onError(Exception e);
    }

}
