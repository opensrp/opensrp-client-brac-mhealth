package org.smartregister.unicef.dghs.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.holder.DashBoardViewHolder;

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