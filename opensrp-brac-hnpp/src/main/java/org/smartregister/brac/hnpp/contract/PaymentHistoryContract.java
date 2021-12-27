package org.smartregister.brac.hnpp.contract;

import android.content.Context;
import org.smartregister.brac.hnpp.model.PaymentHistory;
import java.util.ArrayList;

public interface PaymentHistoryContract {
    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        void initializePresenter();
        PaymentHistoryContract.Presenter getPresenter();
        Context getContext();

    }
    interface Presenter{
        void fetchAllData();
        void fetchPaymentService();
        void filterByFromToDate(String fromDate, String toDate);
        PaymentHistoryContract.View getView();
    }
    interface Interactor{
        ArrayList<PaymentHistory> getPaymentHistoryList();
        int getTotalPayment();
        void fetchAllData(PaymentHistoryContract.InteractorCallBack callBack);
        void fetchPaymentService(PaymentHistoryContract.InteractorCallBack callBack,boolean isLocal);
        void filterByFromToDate(PaymentHistoryContract.InteractorCallBack callBack,String fromDate, String toDate);

    }

    interface InteractorCallBack{
        void fetchedSuccessfully();
    }
}
