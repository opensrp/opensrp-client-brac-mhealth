package org.smartregister.brac.hnpp.listener;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.smartregister.chw.core.listener.NavigationListener;
import org.smartregister.chw.core.utils.CoreConstants;

public class HnppNavigationListener extends NavigationListener {

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getTag() instanceof String) {
            String tag = (String) v.getTag();
            if (CoreConstants.DrawerMenu.ALL_MEMBER.equals(tag)) {
                //Toast.makeText(v.getContext(),"coming soon",Toast.LENGTH_SHORT).show();
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.ALL_MEMBER_REGISTER_ACTIVITY));
            }else if(CoreConstants.DrawerMenu.ELCO_CLIENT.equals(tag)){
                startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.ELCO_REGISTER_ACTIVITY));
            }else if(CoreConstants.DrawerMenu.SIMPRINTS_IDENTITY.equals(tag)){

                startRegisterActivityWithoutFinish(getActivity(CoreConstants.REGISTERED_ACTIVITIES.SIMPRINTS_REGISTER_ACTIVITY));
            }
            else if(CoreConstants.DrawerMenu.FORUM.equals(tag)){

                startRegisterActivityWithOutFinish(getActivity(CoreConstants.REGISTERED_ACTIVITIES.FORUM_ACTIVITY));
            }
        }

    }
}
