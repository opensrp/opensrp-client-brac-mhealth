package org.smartregister.brac.hnpp.fragment;

import android.view.View;

import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.adapter.StockAdapter;
import org.smartregister.brac.hnpp.presenter.StockDashBoardPresenter;

public class StockDashBoardFragment extends BaseDashBoardFragment {

    private StockDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new StockDashBoardPresenter(this);
        dateView.setVisibility(View.GONE);
    }

    @Override
    void fetchData() {
        presenter.filterData(ssName,month+"");

    }

    @Override
    void filterData() {
        presenter.filterData(ssName,month+"");
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();
        if(adapter == null){
            adapter = new StockAdapter(getActivity(), (position, content) -> {

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
        super.updateTitle("হাতে থাকা প্যাকেজ");

    }
}