package org.smartregister.unicef.mis.fragment;

import android.annotation.SuppressLint;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.DashBoardAdapter;
import org.smartregister.unicef.mis.presenter.GMPDashBoardPresenter;
import org.smartregister.unicef.mis.presenter.ImmunizationSummeryDashBoardPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;

public class GMPDashBoardFragment extends BaseDashBoardFragment {

    private GMPDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new GMPDashBoardPresenter(this);
        monthView.setVisibility(View.GONE);
        dateView.setVisibility(View.GONE);
        fromDateView.setVisibility(View.GONE);
        toDateView.setVisibility(View.GONE);
        ssView.setVisibility(View.GONE);
        fromMonthView.setVisibility(View.VISIBLE);
        toMonthView.setVisibility(View.INVISIBLE);
        fromTextView.setText(getString(R.string.month));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        toMonth = fromMonth;//as month view
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
        Log.v("GMP_REPORT","filterByFromToMonth>>>fromMonthFormat:"+fromMonthFormat+":toMonthFormat:"+toMonthFormat);


        presenter.filterByFromToMonth(fromMonthFormat,toMonthFormat,ssName);
    }
//    protected void filterByFromToMonth() {
//        String fromMonthStr="";
//        String toMonthStr="";
//        if(fromMonth == -1 && toMonth != -1 ){
//            fromMonthStr="1970-01-01";
//            toMonthStr = HnppConstants.getStringDateFormatForToMonth(String.valueOf(toYear),String.valueOf(toMonth));
//        }
//        if(fromMonth != -1 && toMonth == -1){
//            fromMonthStr=HnppConstants.getStringDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
//            toMonthStr = HnppConstants.getStringDateFormatForToMonth(String.valueOf(year),String.valueOf(month));
//        }
//        if(fromMonth != -1 && toMonth != -1) {
//            fromMonthStr=HnppConstants.getStringDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
//            toMonthStr = HnppConstants.getStringDateFormatForToMonth(String.valueOf(toYear),String.valueOf(toMonth));
//
//        }
//        presenter.filterByFromToMonth(fromMonthStr,toMonthStr,ssName);
//    }

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
        super.updateTitle(getActivity().getString(R.string.report));

    }
}
