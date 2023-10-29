package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.contract.PaymentContract;
import org.smartregister.unicef.mis.interactor.PaymentInteractor;
import org.smartregister.unicef.mis.model.Payment;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class PaymentPresenter implements PaymentContract.Presenter, PaymentContract.InteractorCallBack {
    private PaymentContract.View view;
    private PaymentContract.Interactor interactor;

    public PaymentPresenter(PaymentContract.View view){
        this.view = view;
        interactor = new PaymentInteractor(new AppExecutors());
    }

    @Override
    public void fetchPaymentService() {
        getView().showProgressBar();
        interactor.fetchPaymentService(this);
    }
    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateAdapter();
    }
    public ArrayList<Payment> getPaymentData(){
        return interactor.getPaymentList();
    }
    @Override
    public PaymentContract.View getView() {
        return view;
    }
}
