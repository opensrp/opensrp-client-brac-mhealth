package org.smartregister.unicef.dghs.fragment;

import android.view.View;

import org.smartregister.unicef.dghs.R;

import java.util.Calendar;

public class MonthlyServiceTargetAchievementFragment extends ServiceTargetAchievementFragment {
    @Override
    void initilizePresenter() {
        super.initilizePresenter();
        monthView.setVisibility(View.GONE);
        dateView.setVisibility(View.GONE);
        fromDateView.setVisibility(View.GONE);
        toDateView.setVisibility(View.GONE);

        fromMonthView.setVisibility(View.VISIBLE);
        toMonthView.setVisibility(View.VISIBLE);
    }
    @Override
    void fetchData() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = 0;
        super.fetchData();
    }
    @Override
    void filterData() {
        super.filterByFromToMonth();
    }
    @Override
    void updateTitle() {
        super.updateTitle(getString(R.string.monthly_service));

    }
}
