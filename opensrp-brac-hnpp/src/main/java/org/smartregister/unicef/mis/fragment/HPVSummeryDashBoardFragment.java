package org.smartregister.unicef.mis.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.DashBoardAdapter;
import org.smartregister.unicef.mis.presenter.HPVSummeryDashBoardPresenter;
import org.smartregister.unicef.mis.presenter.ImmunizationSummeryDashBoardPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HPVSummeryDashBoardFragment extends BaseDashBoardFragment {

    private HPVSummeryDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new HPVSummeryDashBoardPresenter(this);
        monthView.setVisibility(View.GONE);
        dateView.setVisibility(View.GONE);
        fromDateView.setVisibility(View.GONE);
        toDateView.setVisibility(View.GONE);
        ssView.setVisibility(View.GONE);
        fromMonthView.setVisibility(View.VISIBLE);
        toMonthView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }
    @SuppressLint("SetTextI18n")
    private void updateSyncCount(){
        int totalUnsyncCount = HnppApplication.getOtherVaccineRepository().getUnSyncCount();
        if(totalUnsyncCount>0){
            unsyncCountTv.setVisibility(View.VISIBLE);
            unsyncCountTv.setText("Total Unsync Data:"+totalUnsyncCount+"");
            syncBtn.setVisibility(View.VISIBLE);
        }else{
            syncBtn.setVisibility(View.INVISIBLE);
            unsyncCountTv.setVisibility(View.INVISIBLE);
        }
    }
    private void syncData(){
        int totalUnsyncCount = HnppApplication.getOtherVaccineRepository().getUnSyncCount();
        if(totalUnsyncCount>0){
            showProgressDialog("Data syncing....");
            HnppConstants.postOtherVaccineData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {}

                        @Override
                        public void onNext(String s) {
                            Log.v("OTHER_VACCINE","onNext>>s:"+s);
                            try{
                                hideProgressDialog();
                            }catch (Exception e){

                            }


                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.v("OTHER_VACCINE",""+e);
                            try{
                                hideProgressDialog();
                            }catch (Exception e1){

                            }
                        }

                        @Override
                        public void onComplete() {
                            Log.v("OTHER_VACCINE","completed");
                            try{
                                hideProgressDialog();
                            }catch (Exception e){

                            }
                            updateSyncCount();
                        }
                    });
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateSyncCount();
        syncData();
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncData();
            }
        });
    }
    private ProgressDialog dialog;

    private void showProgressDialog(String text) {
        if (dialog == null) {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
    @Override
    void fetchData() {
       // presenter.filterData(ssName,month+"",year+"");
        filterByFromToMonth();

    }

    @Override
    void filterData() {
       // presenter.filterData(ssName,month+"",year+"");
        filterByFromToMonth();
    }
    void filterByFromToMonth() {
        long fromMonthFormat = 0;
        long toMonthFormat = 0;
        if((fromMonth == -1 || fromYear == -1) && (toMonth == -1 || toYear == -1 )){
            fromMonthFormat = -1;
            toMonthFormat = -1;
        }
        if(fromMonth == -1 && toMonth != -1 ){
            fromMonthFormat = -1;
            toMonthFormat = HnppConstants.getLongDateFormatForToMonth(String.valueOf(toYear),String.valueOf(toMonth));
        }
        if(fromMonth != -1 && toMonth == -1){
            fromMonthFormat = HnppConstants.getLongDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
            toMonthFormat = HnppConstants.getLongDateFormatForToMonth(String.valueOf(year),String.valueOf(month));
        }
        if(fromMonth != -1 && toMonth != -1) {
            fromMonthFormat = HnppConstants.getLongDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
            toMonthFormat = HnppConstants.getLongDateFormatForToMonth(String.valueOf(toYear),String.valueOf(toMonth));
        }




        presenter.filterByFromToMonth(fromMonthFormat,toMonthFormat,ssName);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateAdapter() {
        super.updateAdapter();
        if(adapter == null){
            adapter = new DashBoardAdapter(getActivity(), (position, content) -> {

            });
            adapter.setData(presenter.getDashBoardData());
            recyclerView.setAdapter(adapter);
        }else{
            adapter.setData(presenter.getDashBoardData());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    void updateTitle() {
        super.updateTitle("HPV টিকা প্রদানের তথ্য");

    }
}
