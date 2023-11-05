package org.smartregister.chw.core.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import org.smartregister.chw.core.R;
import org.smartregister.chw.core.adapter.NavigationAdapter;
import org.smartregister.chw.core.utils.CoreConstants;

public class NavigationListener implements View.OnClickListener,NavigationSubMenu {

    private Activity activity;
    private NavigationAdapter navigationAdapter;

    public void setAdapter(NavigationAdapter adapter,Activity activity){

        this.navigationAdapter = adapter;
        this.activity = activity;
    }

    @Override
    public void onClickSubMenu(String v) {

    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof String) {
            String tag = (String) v.getTag();
          if (CoreConstants.DrawerMenu.CHILD_CLIENTS.equals(tag)) {
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.CHILD_REGISTER_ACTIVITY));
            } else if (CoreConstants.DrawerMenu.ALL_FAMILIES.equals(tag)) {
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.FAMILY_REGISTER_ACTIVITY));
            } else if (CoreConstants.DrawerMenu.ANC.equals(tag)) {
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY));
            } else if (CoreConstants.DrawerMenu.LD.equals(tag)) {
                Toast.makeText(activity.getApplicationContext(), CoreConstants.DrawerMenu.LD, Toast.LENGTH_SHORT).show();
            } else if (CoreConstants.DrawerMenu.PNC.equals(tag)) {
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.PNC_REGISTER_ACTIVITY));
            } else if (CoreConstants.DrawerMenu.FAMILY_PLANNING.equals(tag)) {
                Toast.makeText(activity.getApplicationContext(), CoreConstants.DrawerMenu.FAMILY_PLANNING, Toast.LENGTH_SHORT).show();
            } else if (CoreConstants.DrawerMenu.MALARIA.equals(tag)) {
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.MALARIA_REGISTER_ACTIVITY));
            } else if (CoreConstants.DrawerMenu.REFERRALS.equals(tag)) {
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY));
            }
            navigationAdapter.setSelectedView(tag);
        }
    }

    public void startRegisterActivity(Class registerClass) {
        Intent intent = new Intent(activity, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        activity.finish();
    }
    public void startRegisterActivityWithOutFinish(Class registerClass) {
        Intent intent = new Intent(activity, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }
    public void startRegisterActivityWithOutClearTop(Class registerClass) {
        Intent intent = new Intent(activity, registerClass);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }
    protected Class getActivity(String key) {
        return navigationAdapter.getRegisteredActivities().get(key);
    }
}
