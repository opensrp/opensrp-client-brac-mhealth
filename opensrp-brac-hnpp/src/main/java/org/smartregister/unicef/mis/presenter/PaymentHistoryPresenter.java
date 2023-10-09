package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.contract.PaymentHistoryContract;
import org.smartregister.unicef.mis.interactor.PaymentHistoryInteractor;
import org.smartregister.unicef.mis.model.PaymentHistory;
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
    public void fetchAllData() {
        getView().showProgressBar();
        interactor.fetchAllData(this);
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
    public int getTotalPayment(){
        return interactor.getTotalPayment();
    }
    @Override
    public PaymentHistoryContract.View getView() {
        return view;
    }
}

