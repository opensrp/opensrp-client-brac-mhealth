package org.smartregister.unicef.mis.adapter;

import android.content.Context;

import org.smartregister.unicef.mis.R;

public class StockAdapter extends DashBoardAdapter{
    public StockAdapter(Context context, OnClickAdapter onClickAdapter) {
        super(context, onClickAdapter);
    }

    @Override
    public int getAdapterLayout() {
        super.getAdapterLayout();
        return R.layout.view_stock_dashboard;
    }
}
