package org.smartregister.brac.hnpp.fragment;

import android.view.View;

import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.presenter.CountSummeryDashBoardPresenter;

public class CountSummeryDashBoardFragment extends BaseDashBoardFragment {

    private CountSummeryDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new CountSummeryDashBoardPresenter(this);
        monthView.setVisibility(View.INVISIBLE);
        dateView.setVisibility(View.INVISIBLE);
    }

    @Override
    void fetchData() {
        presenter.fetchDashBoardData();

    }

    @Override
    void filterData() {
        presenter.filterData(ssName,"");
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
        super.updateTitle("পরিদর্শন ড্যাশবোর্ড");

    }
}