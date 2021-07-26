package org.smartregister.brac.hnpp.fragment;

import android.support.v7.widget.LinearLayoutManager;
import org.smartregister.brac.hnpp.adapter.TargetAchievementAdapter;
import org.smartregister.brac.hnpp.presenter.TargetAchievmentPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;

public class TargetAchievementFragment extends BaseDashBoardFragment {

    private TargetAchievmentPresenter presenter;
    private TargetAchievementAdapter adapter;

    @Override
    void initilizePresenter() {
        presenter = new TargetAchievmentPresenter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    void fetchData() {
    }


    @Override
    void filterData() {
        long fromDateFormat = 0;
        long toDateFormat = 0;
        if((fromMonth == -1 || fromYear == -1) && (toMonth == -1 || toYear == -1 )){
            fromDateFormat = -1;
            toDateFormat = -1;
        }
        if(fromMonth == -1 && toMonth != -1 ){
            fromDateFormat = -1;
            toDateFormat = HnppConstants.getLongDateFormate(String.valueOf(toYear),String.valueOf(toMonth),String.valueOf(toDay));
        }
        if(fromMonth != -1 && toMonth == -1){
            fromDateFormat = HnppConstants.getLongDateFormate(String.valueOf(fromYear),String.valueOf(fromMonth),String.valueOf(fromDay));
            toDateFormat = HnppConstants.getLongDateFormate(String.valueOf(year),String.valueOf(month),String.valueOf(day));
        }
        if(fromMonth != -1 && toMonth != -1) {
            fromDateFormat = HnppConstants.getLongDateFormate(String.valueOf(fromYear),String.valueOf(fromMonth),String.valueOf(fromDay));
            toDateFormat = HnppConstants.getLongDateFormate(String.valueOf(toYear),String.valueOf(toMonth),String.valueOf(toDay));
        }

        presenter.filterByFromToDate(fromDateFormat,toDateFormat,ssName);

    }

    protected void filterByFromToMonth() {
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
            adapter = new TargetAchievementAdapter(getActivity(), (position, content) -> {

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
        super.updateTitle("দৈনিক পরিদর্শন");

    }
}
