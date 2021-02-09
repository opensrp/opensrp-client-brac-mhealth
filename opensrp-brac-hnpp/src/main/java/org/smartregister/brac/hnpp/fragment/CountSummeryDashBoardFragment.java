package org.smartregister.brac.hnpp.fragment;

import android.view.View;

import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.presenter.CountSummeryDashBoardPresenter;

public class CountSummeryDashBoardFragment extends BaseDashBoardFragment {

    private CountSummeryDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new CountSummeryDashBoardPresenter(this);
        monthView.setVisibility(View.VISIBLE);
        dateView.setVisibility(View.GONE);
    }

    @Override
    void fetchData() {
        presenter.filterData(ssName,month+"",year+"");

    }

    @Override
    void filterData() {
        presenter.filterData(ssName,month+"",year+"");
    }

    @Override
    void filterByFromToDate() {

    }

    @Override
    void filterByFromToMonth() {

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
