package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.smartregister.brac.hnpp.R;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.topbar).setVisibility(View.GONE);
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
    public void updateAdapter() {
        super.updateAdapter();
        if(adapter == null){
            adapter = new StockAdapter(getActivity(), (position, content) -> {
                StockDashBoardDialogFragment dialogFragment = StockDashBoardDialogFragment.getInstance();
                dialogFragment.setContent(content);
                dialogFragment.show(getChildFragmentManager(),"ds");
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