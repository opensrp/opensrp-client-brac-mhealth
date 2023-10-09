package org.smartregister.unicef.mis.nativation.view;



import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;

public class HnppNavigationTopView implements NavigationMenu.FlavorTop {

    @Override
    public int getTopLogo() {
        return R.drawable.avatar_woman;
    }

    @Override
    public String topText() {
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);

        return fullName+"\n"+userName;
    }

    @Override
    public String appVersionText() {
        return "App version: " +BuildConfig.VERSION_NAME;
    }
}
