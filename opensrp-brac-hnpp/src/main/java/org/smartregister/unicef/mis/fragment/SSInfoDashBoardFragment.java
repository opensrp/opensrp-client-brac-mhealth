package org.smartregister.unicef.mis.fragment;

import android.view.View;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.SSDashboardAdapter;
import org.smartregister.unicef.mis.presenter.SSInfoDashBoardPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;

public class SSInfoDashBoardFragment extends BaseDashBoardFragment {

    private SSInfoDashBoardPresenter presenter;

    @Override
    void initilizePresenter() {
        presenter = new SSInfoDashBoardPresenter(this);
        if(HnppConstants.isPALogin()){
            ssView.setVisibility(View.GONE);
        }
        monthView.setVisibility(View.GONE);
        dateView.setVisibility(View.GONE);
        fromDateView.setVisibility(View.GONE);
        toDateView.setVisibility(View.GONE);

        fromMonthView.setVisibility(View.VISIBLE);
        toMonthView.setVisibility(View.VISIBLE);
    }

    @Override
    void fetchData() {
        //presenter.filterData(ssName,month+"",year+"");
       // filterByFromToMonth();

    }

    @Override
    void filterData() {
        filterByFromToMonth();
    }

    void filterByFromToMonth() {
        String toMonthFormatStr ="";
        String fromMonthFormatStr="";
        if((fromMonth == -1 || fromYear == -1) && (toMonth == -1 || toYear == -1 )){
            fromMonthFormatStr ="";
            toMonthFormatStr = "";
        }
        if(fromMonth == -1 && toMonth != -1 ){
            fromMonthFormatStr = "1970-01-01";
            toMonthFormatStr = HnppConstants.getStringDateFormatForToMonth(toYear+"",toMonth+"");
        }
        if(fromMonth != -1 && toMonth == -1){
            fromMonthFormatStr = HnppConstants.getStringDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
            toMonthFormatStr = "2030-01-01";
        }
        if(fromMonth != -1 && toMonth != -1) {
            fromMonthFormatStr = HnppConstants.getStringDateFormatForFromMonth(String.valueOf(fromYear),String.valueOf(fromMonth));
            toMonthFormatStr = HnppConstants.getStringDateFormatForToMonth(String.valueOf(toYear),String.valueOf(toMonth));
        }
        presenter.filterData(ssName,toMonthFormatStr,fromMonthFormatStr);
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();
        if(adapter == null){
            adapter = new SSDashboardAdapter(getActivity(), (position, content) -> {

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
        super.updateTitle(getString(R.string.nurse_dashboard));
    }
}
