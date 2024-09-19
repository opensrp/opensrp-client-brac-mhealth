package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.fragment.BaseDashBoardFragment;
import org.smartregister.unicef.mis.fragment.CountSummeryDashBoardFragment;
import org.smartregister.unicef.mis.fragment.GMPDashBoardFragment;
import org.smartregister.unicef.mis.fragment.GMPFragment;
import org.smartregister.unicef.mis.fragment.HPVSummeryDashBoardFragment;
import org.smartregister.unicef.mis.fragment.ImmunizationSummeryDashBoardFragment;
import org.smartregister.unicef.mis.fragment.ReportFragment;
import org.smartregister.unicef.mis.fragment.ReportGrowthFalterFragment;
import org.smartregister.unicef.mis.fragment.WorkSummeryDashBoardFragment;
import org.smartregister.unicef.mis.utils.HnppConstants;
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
    }

    private void loadCountSummeryFragment(int position){
        switch (position){
            case 0:
                dashBoardFragment = new HPVSummeryDashBoardFragment();
                break;
            case 1:
                dashBoardFragment = new ImmunizationSummeryDashBoardFragment();
                break;
            case 2:
                dashBoardFragment = new GMPDashBoardFragment();
                break;
            case 3:
                dashBoardFragment = new ReportGrowthFalterFragment();
                break;
            case 4:
                dashBoardFragment = new WorkSummeryDashBoardFragment();
                break;
            case 5:
                    dashBoardFragment = new CountSummeryDashBoardFragment();
                    break;


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
        tabs = findViewById(R.id.tabs);
        if(HnppConstants.isVaccinator()){
            tabs.addTab(tabs.newTab().setText("HPV"));
        }else{
            tabs.addTab(tabs.newTab().setText("HPV"));
            tabs.addTab(tabs.newTab().setText(getString(R.string.immunization)));
            tabs.addTab(tabs.newTab().setText(R.string.report));
            tabs.addTab(tabs.newTab().setText(R.string.grow_filter));
            tabs.addTab(tabs.newTab().setText(R.string.activity_in_short));
            tabs.addTab(tabs.newTab().setText(R.string.people_in_short));
        }


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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                onBackPressed();
                break;
        }
    }
}
