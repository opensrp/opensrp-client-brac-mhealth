package org.smartregister.brac.hnpp.fragment;

import android.view.View;

import java.util.Calendar;

public class DailyServiceTargetAchievementFragment extends ServiceTargetAchievementFragment {
    @Override
    void initilizePresenter() {
        super.initilizePresenter();
        monthView.setVisibility(View.GONE);
        dateView.setVisibility(View.VISIBLE);
    }

    @Override
    void fetchData() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        super.fetchData();
    }

    @Override
    void filterData() {
        super.filterData();
    }

    @Override
    void updateTitle() {
        super.updateTitle("দৈনিক সেবা");

    }
}
