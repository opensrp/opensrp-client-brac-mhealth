package org.smartregister.unicef.mis.contract;
import android.content.Context;
import org.smartregister.unicef.mis.model.Payment;

import java.util.ArrayList;

public interface PaymentContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        void initializePresenter();
        Presenter getPresenter();
        Context getContext();

    }
    interface Presenter{
        void fetchPaymentService();
        View getView();
    }
    interface Interactor{
        ArrayList<Payment> getPaymentList();
        void fetchPaymentService(PaymentContract.InteractorCallBack callBack);

    }

    interface InteractorCallBack{
        void fetchedSuccessfully();
    }
    interface PaymentPostInteractorCallBack{
        void onSuccess(ArrayList<String> responses);
        void onFail(String message);
        void onSuccess(String message);
    }
}
