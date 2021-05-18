package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.PaymentHistoryContract;
import org.smartregister.brac.hnpp.interactor.PaymentHistoryInteractor;
import org.smartregister.brac.hnpp.model.PaymentHistory;
import org.smartregister.family.util.AppExecutors;
import java.util.ArrayList;

public class PaymentHistoryPresenter implements PaymentHistoryContract.Presenter, PaymentHistoryContract.InteractorCallBack {
    private PaymentHistoryContract.View view;
    private PaymentHistoryContract.Interactor interactor;

    public PaymentHistoryPresenter(PaymentHistoryContract.View view){
        this.view = view;
        this.interactor = new PaymentHistoryInteractor(new AppExecutors());
    }

    @Override
    public void fetchPaymentService() {
        getView().showProgressBar();
        interactor.fetchPaymentService(this,false);
    }
    public void fetchLocalData() {
        getView().showProgressBar();
        interactor.fetchPaymentService(this,true);
    }
    @Override
    public void filterByFromToDate(String fromDate, String toDate) {
        getView().showProgressBar();
        interactor.filterByFromToDate(this,fromDate,toDate);
    }

    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateAdapter();
    }
    public ArrayList<PaymentHistory> getPaymentData(){
        return interactor.getPaymentHistoryList();
    }
    @Override
    public PaymentHistoryContract.View getView() {
        return view;
    }
}

