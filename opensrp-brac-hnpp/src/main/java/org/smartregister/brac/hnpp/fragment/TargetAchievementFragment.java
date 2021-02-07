package org.smartregister.brac.hnpp.fragment;

import android.support.v7.widget.LinearLayoutManager;

import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.adapter.TargetAchievementAdapter;
import org.smartregister.brac.hnpp.presenter.TargetAchievmentPresenter;
import org.smartregister.brac.hnpp.presenter.WorkSummeryDashBoardPresenter;

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
        presenter.fetchDashBoardData(day+"",month+"",year+"",ssName);
    }


    @Override
    void filterData() {
        presenter.filterData(ssName,day+"",month+"",year+"");
    }


    public void filterByFromToDate() {
        String fromDateFormat = fromYear+"-"+fromMonth+"-"+fromDay;
        String toDateFormat = toYear+"-"+toMonth+"-"+toDay;
        presenter.filterByFromToDate(fromDateFormat,toDateFormat,ssName);
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
