package org.smartregister.unicef.mis.adapter;

import android.content.Context;

import org.smartregister.unicef.mis.R;

public class SSDashboardAdapter extends DashBoardAdapter{
    public SSDashboardAdapter(Context context, OnClickAdapter onClickAdapter) {
        super(context, onClickAdapter);
    }

    @Override
    public int getAdapterLayout() {
        super.getAdapterLayout();
        return R.layout.view_ss_dashboard;
    }
}
