package org.smartregister.unicef.dghs.fragment;

import android.support.v7.widget.LinearLayoutManager;
import org.smartregister.unicef.dghs.adapter.TargetAchievementAdapter;
import org.smartregister.unicef.dghs.presenter.TargetAchievmentPresenter;
import org.smartregister.unicef.dghs.utils.HnppConstants;

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
        String fromDateStr="";
        String toDateStr="";
        if(fromMonth == -1 && toMonth != -1 ){
            fromDateStr="1970-01-01";
            toDateStr = HnppConstants.getStringFormatedDate(String.valueOf(toYear),String.valueOf(toMonth),String.valueOf(toDay));

        }
        if(fromMonth != -1 && toMonth == -1){
            fromDateStr = HnppConstants.getStringFormatedDate(String.valueOf(fromYear),String.valueOf(fromMonth),String.valueOf(fromDay));
            toDateStr = HnppConstants.getStringFormatedDate(String.valueOf(year),String.valueOf(month),String.valueOf(day));

        }
        if(fromMonth != -1 && toMonth != -1) {
            fromDateStr = HnppConstants.getStringFormatedDate(String.valueOf(fromYear),String.valueOf(fromMonth),String.valueOf(fromDay));
            toDateStr = HnppConstants.getStringFormatedDate(String.valueOf(toYear),String.valueOf(toMonth),String.valueOf(toDay));

        }

        presenter.filterByFromToDate(fromDateStr,toDateStr,ssName);
    }
    protected void filterByFromToMonth() {
        String fromMonthStr="";
        String toMonthStr="";
        if(fromMonth == -1 && toMonth != -1 ){
            fromMonthStr="1970-01-01";
            toMonthStr = HnppConstants.getStringDateFormatForToMonth(String.valueOf(toYear),String.valueOf(toMonth));
        }
        if(fromMonth != -1 && toMonth == -1){
            fromMonthStr=HnppConstants.getStringDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
            toMonthStr = HnppConstants.getStringDateFormatForToMonth(String.valueOf(year),String.valueOf(month));
        }
        if(fromMonth != -1 && toMonth != -1) {
            fromMonthStr=HnppConstants.getStringDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
            toMonthStr = HnppConstants.getStringDateFormatForToMonth(String.valueOf(toYear),String.valueOf(toMonth));

        }
        presenter.filterByFromToMonth(fromMonthStr,toMonthStr,ssName);
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
