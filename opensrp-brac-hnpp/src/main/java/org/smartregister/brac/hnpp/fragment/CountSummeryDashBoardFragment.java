package org.smartregister.brac.hnpp.fragment;

import android.view.View;

import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.presenter.CountSummeryDashBoardPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;

public class CountSummeryDashBoardFragment extends BaseDashBoardFragment {

    private CountSummeryDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new CountSummeryDashBoardPresenter(this);
        monthView.setVisibility(View.GONE);
        dateView.setVisibility(View.GONE);
        fromDateView.setVisibility(View.GONE);
        toDateView.setVisibility(View.GONE);

        fromMonthView.setVisibility(View.VISIBLE);
        toMonthView.setVisibility(View.VISIBLE);
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
        super.updateTitle("জনসংখ্যা সারসংক্ষেপ");

    }
}
