package org.smartregister.brac.hnpp.fragment;

import android.support.v7.widget.LinearLayoutManager;

import org.smartregister.brac.hnpp.adapter.ForumTargetAchievementAdapter;
import org.smartregister.brac.hnpp.adapter.TargetAchievementAdapter;
import org.smartregister.brac.hnpp.presenter.ForumTargetAchievementPresenter;
import org.smartregister.brac.hnpp.presenter.ServiceTargetAchievmentPresenter;

public class ForumTargetAchievementFragment extends BaseDashBoardFragment {

    private ForumTargetAchievementPresenter presenter;
    private int day,month,year;
    private ForumTargetAchievementAdapter adapter;

    @Override
    void initilizePresenter() {
        presenter = new ForumTargetAchievementPresenter(this);
    }

    public void setDayWise(int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
    }
    public void setMonthWise(int month, int year){
        this.day = 0;
        this.month = month;
        this.year = year;
    }

    @Override
    void fetchData() {
        presenter.fetchDashBoardData(day,month,year,ssName);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    @Override
    void filterData() {
        presenter.filterData(ssName,day,month,year);
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();
        if(adapter == null){
            adapter = new ForumTargetAchievementAdapter(getActivity(), (position, content) -> {

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
        super.updateTitle("ফোরাম");

    }
}
