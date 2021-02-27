package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.DashBoardViewHolder;

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
