package org.smartregister.unicef.dghs.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.fragment.BaseDashBoardFragment;
import org.smartregister.unicef.dghs.fragment.CountSummeryDashBoardFragment;
import org.smartregister.unicef.dghs.fragment.DailyServiceTargetAchievementFragment;
import org.smartregister.unicef.dghs.fragment.DailyTargetAchievementFragment;
import org.smartregister.unicef.dghs.fragment.ForumTargetAchievementFragment;
import org.smartregister.unicef.dghs.fragment.MonthlyServiceTargetAchievementFragment;
import org.smartregister.unicef.dghs.fragment.MonthlyTargetAchievementFragment;
import org.smartregister.unicef.dghs.fragment.SSInfoDashBoardFragment;
import org.smartregister.unicef.dghs.fragment.StockDashBoardFragment;
import org.smartregister.unicef.dghs.fragment.WorkSummeryDashBoardFragment;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class NewDashBoardActivity extends SecuredActivity implements View.OnClickListener{
    private ImageView refreshIndicatorsIcon;
    private ProgressBar refreshIndicatorsProgressBar;
    private TextView titleText;
    private BaseDashBoardFragment dashBoardFragment;
    private TabLayout tabs;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_dashboard);
        setUpView();
        loadCountSummeryFragment(0);

    }

    @Override
    protected void onStart() {
        super.onStart();
       // VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
    }

    private void loadCountSummeryFragment(int position){
        if(HnppConstants.isPALogin()){
            switch (position){
                case 0:
                    dashBoardFragment = new DailyTargetAchievementFragment();
                    break;
                case 1:
                    dashBoardFragment = new MonthlyTargetAchievementFragment();
                    break;
                case 2:
                    dashBoardFragment = new ForumTargetAchievementFragment();
                    break;
                case 3:
                    dashBoardFragment = new StockDashBoardFragment();
                    break;
                case 4:
                    dashBoardFragment = new CountSummeryDashBoardFragment();
                    break;
                case 5:
                    dashBoardFragment = new WorkSummeryDashBoardFragment();
                    break;
                case 6:
                    dashBoardFragment = new SSInfoDashBoardFragment();
                    break;
            }
        }else {
            switch (position){

                case 0:
                    dashBoardFragment = new DailyTargetAchievementFragment();
                    break;
                case 1:
                    dashBoardFragment = new MonthlyTargetAchievementFragment();
                    break;
                case 2:
                    dashBoardFragment = new DailyServiceTargetAchievementFragment();
                    break;
                case 3:
                    dashBoardFragment = new MonthlyServiceTargetAchievementFragment();
                    break;

                case 4:
                    dashBoardFragment = new ForumTargetAchievementFragment();
                    break;
                case 5:
                    dashBoardFragment = new StockDashBoardFragment();
                    break;
                case 6:
                    dashBoardFragment = new CountSummeryDashBoardFragment();
                    break;
                case 7:
                    dashBoardFragment = new WorkSummeryDashBoardFragment();
                    break;
                case 8:
                    dashBoardFragment = new SSInfoDashBoardFragment();
                    break;
            }
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, dashBoardFragment);
        fragmentTransaction.commit();
    }
    private void setUpView() {
        refreshIndicatorsIcon = findViewById(R.id.refreshIndicatorsIcon);
        refreshIndicatorsIcon.setVisibility(View.GONE);
        refreshIndicatorsProgressBar = findViewById(R.id.refreshIndicatorsPB);
        findViewById(R.id.backBtn).setOnClickListener(this);
        // Initial view until we determined by the refresh function
        refreshIndicatorsProgressBar.setVisibility(View.GONE);
        titleText = findViewById(R.id.textview_detail_two);

        if(!HnppConstants.isReleaseBuild()){
            findViewById(R.id.toolbar).setBackgroundResource(R.color.test_app_color);

        }else{
            findViewById(R.id.toolbar).setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);

        }

//        refreshIndicatorsIcon.setOnClickListener(view -> {
//            refreshIndicatorsIcon.setVisibility(View.GONE);
//            FadingCircle circle = new FadingCircle();
//            refreshIndicatorsProgressBar.setIndeterminateDrawable(circle);
//            refreshIndicatorsProgressBar.setVisibility(View.VISIBLE);
//            new Handler().postDelayed(() -> dashBoardFragment.refreshData(() -> {
//                refreshIndicatorsProgressBar.setVisibility(View.GONE);
//                refreshIndicatorsIcon.setVisibility(View.VISIBLE);
//            }),500);
//
//        });
        tabs = findViewById(R.id.tabs);

        tabs.addTab(tabs.newTab().setText(R.string.everyday_visit));
        tabs.addTab(tabs.newTab().setText(R.string.monthly_visit));
        if(!HnppConstants.isPALogin()){
            tabs.addTab(tabs.newTab().setText(R.string.everyday_visit));
            tabs.addTab(tabs.newTab().setText(R.string.monthly_visit));
        }
        tabs.addTab(tabs.newTab().setText(R.string.forum));
        tabs.addTab(tabs.newTab().setText(R.string.stock));
        tabs.addTab(tabs.newTab().setText(R.string.people_in_short));
        tabs.addTab(tabs.newTab().setText(R.string.activity_in_short));
        tabs.addTab(tabs.newTab().setText(R.string.nurse));
        //tabs.addTab(tabs.newTab().setText("স্টক"));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadCountSummeryFragment(tabs.getSelectedTabPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    @Override
    protected void onResumption() {
        if(refreshIndicatorsProgressBar !=null && refreshIndicatorsProgressBar.getVisibility() == View.VISIBLE){
            refreshIndicatorsProgressBar.setVisibility(View.GONE);
        }

    }
    public void updateTitle(String title){
       if(titleText!=null) titleText.setText(title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                onBackPressed();
                break;
        }
    }
}