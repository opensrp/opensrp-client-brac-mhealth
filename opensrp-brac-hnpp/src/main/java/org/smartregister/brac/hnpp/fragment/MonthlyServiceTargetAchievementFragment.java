package org.smartregister.brac.hnpp.fragment;

import java.util.Calendar;

public class MonthlyServiceTargetAchievementFragment extends ServiceTargetAchievementFragment {
    @Override
    void fetchData() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = 0;
        super.fetchData();
    }
    @Override
    void updateTitle() {
        super.updateTitle("মাসিক সেবা");

    }
}
