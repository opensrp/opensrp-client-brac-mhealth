package org.smartregister.brac.hnpp.fragment;

import android.support.v7.widget.LinearLayoutManager;

import org.smartregister.brac.hnpp.adapter.ServiceTargetAchievementAdapter;
import org.smartregister.brac.hnpp.presenter.ServiceTargetAchievmentPresenter;

public class ServiceTargetAchievementFragment extends BaseDashBoardFragment {

    private ServiceTargetAchievmentPresenter presenter;
    private ServiceTargetAchievementAdapter adapter;

    @Override
    void initilizePresenter() {
        presenter = new ServiceTargetAchievmentPresenter(this);
    }


    @Override
    void fetchData() {
        presenter.fetchDashBoardData(day+"",month+"",year+"",ssName);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    @Override
    void filterData() {
        presenter.filterData(ssName,day+"",month+"",year+"");
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
