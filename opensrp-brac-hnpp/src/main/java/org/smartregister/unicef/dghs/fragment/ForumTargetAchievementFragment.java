package org.smartregister.unicef.dghs.fragment;

import android.support.v7.widget.LinearLayoutManager;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.adapter.ForumTargetAchievementAdapter;
import org.smartregister.unicef.dghs.adapter.TargetAchievementAdapter;
import org.smartregister.unicef.dghs.presenter.ForumTargetAchievementPresenter;
import org.smartregister.unicef.dghs.presenter.ServiceTargetAchievmentPresenter;

import java.util.Calendar;

public class ForumTargetAchievementFragment extends BaseDashBoardFragment {

    private ForumTargetAchievementPresenter presenter;
    private ForumTargetAchievementAdapter adapter;

    @Override
    void initilizePresenter() {
        presenter = new ForumTargetAchievementPresenter(this);
    }

    @Override
    void fetchData() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = 0;
        presenter.fetchDashBoardData(day+"",month+"",year+"",ssName);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    @Override
    void filterData() {
        day = 0;
        presenter.filterData(ssName,day+"",month+"",year+"");
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
        super.updateTitle(getActivity().getString(R.string.forum));

    }
}
