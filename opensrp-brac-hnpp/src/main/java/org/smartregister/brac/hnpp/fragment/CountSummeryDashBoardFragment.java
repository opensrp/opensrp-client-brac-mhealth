package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.presenter.CountSummeryDashBoardPresenter;

public class CountSummeryDashBoardFragment extends BaseDashBoardFragment {

    private CountSummeryDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new CountSummeryDashBoardPresenter(this);
    }

    @Override
    void fetchData() {
        presenter.fetchDashBoardData();

    }

    @Override
    void filterData() {

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
        super.updateTitle("মাসিক পরিদর্শন ড্যাশবোর্ড");

    }
}
