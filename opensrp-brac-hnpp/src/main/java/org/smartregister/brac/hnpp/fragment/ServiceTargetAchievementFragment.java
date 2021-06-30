package org.smartregister.brac.hnpp.fragment;

import android.support.v7.widget.LinearLayoutManager;

import org.smartregister.brac.hnpp.adapter.ServiceTargetAchievementAdapter;
import org.smartregister.brac.hnpp.presenter.ServiceTargetAchievmentPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;

public class ServiceTargetAchievementFragment extends BaseDashBoardFragment {

    private ServiceTargetAchievmentPresenter presenter;
    private ServiceTargetAchievementAdapter adapter;

    @Override
    void initilizePresenter() {
        presenter = new ServiceTargetAchievmentPresenter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    @Override
    void fetchData() {
        filterData();
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
        //presenter.filterData(ssName,day+"",month+"",year+"");
    }


    @Override
    public void updateAdapter() {
        super.updateAdapter();
        if(adapter == null){
            adapter = new ServiceTargetAchievementAdapter(getActivity(), (position, content) -> {

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
