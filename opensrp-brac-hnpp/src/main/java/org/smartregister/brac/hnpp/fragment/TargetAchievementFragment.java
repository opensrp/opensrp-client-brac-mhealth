package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.adapter.TargetAchievementAdapter;
import org.smartregister.brac.hnpp.presenter.TargetAchievmentPresenter;
import org.smartregister.brac.hnpp.presenter.WorkSummeryDashBoardPresenter;

public class TargetAchievementFragment extends BaseDashBoardFragment {

    private TargetAchievmentPresenter presenter;
    private int day,month,year;
    private TargetAchievementAdapter adapter;

    @Override
    void initilizePresenter() {
        presenter = new TargetAchievmentPresenter(this);
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
        presenter.fetchDashBoardData(day,month,year);

    }


    @Override
    void filterData() {

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
