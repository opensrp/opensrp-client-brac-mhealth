package org.smartregister.unicef.mis.model;

import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.model.NavigationSubModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.mis.R;

import java.util.ArrayList;
import java.util.List;

public class HnppNavigationModel implements org.smartregister.chw.core.model.NavigationModel.Flavor {
    private List<NavigationOption> navigationOptions = new ArrayList<>();

    @Override
    public List<NavigationOption> getNavigationItems() {
        if (navigationOptions.size() == 0) {
           // navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, CoreConstants.DrawerMenu.ALL_FAMILIES, 0));
            navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_member, CoreConstants.DrawerMenu.ALL_MEMBER, 0));
            if(HnppConstants.isPALogin()){
                NavigationOption optionElco = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_adult, CoreConstants.DrawerMenu.ADULT, 0);
                optionElco.setNeedToExpand(true);
                optionElco.setNavigationSubModel(new NavigationSubModel(R.string.menu_adult_risk,0,CoreConstants.DrawerMenu.ADULT_RISK));
                navigationOptions.add(optionElco);
            }else{
                NavigationOption optionElco = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_elco_clients, CoreConstants.DrawerMenu.ELCO_CLIENT, 0);
                optionElco.setNeedToExpand(false);
//                optionElco.setNavigationSubModel(new NavigationSubModel(R.string.menu_anc_risk_clients,0,CoreConstants.DrawerMenu.ELCO_RISK));
                navigationOptions.add(optionElco);

                NavigationOption option = new NavigationOption(R.mipmap.sidemenu_anc, R.mipmap.sidemenu_anc_active, R.string.menu_anc_clients, CoreConstants.DrawerMenu.ANC, 0);
                option.setNeedToExpand(true);
                option.setNavigationSubModel(new NavigationSubModel(R.string.menu_anc_risk_clients,0,CoreConstants.DrawerMenu.ANC_RISK));
                navigationOptions.add(option);

                NavigationOption optionPnc = new NavigationOption(R.mipmap.sidemenu_pnc, R.mipmap.sidemenu_pnc_active, R.string.menu_pnc_clients, CoreConstants.DrawerMenu.PNC, 0);
                optionPnc.setNeedToExpand(true);
                optionPnc.setNavigationSubModel(new NavigationSubModel(R.string.menu_anc_risk_clients,0,CoreConstants.DrawerMenu.PNC_RISK));
                navigationOptions.add(optionPnc);

//                NavigationOption optionWomen = new NavigationOption(R.drawable.ic_women_un, R.drawable.ic_women_s, R.string.menu_women_clients, CoreConstants.DrawerMenu.WOMEN, 0);
//                navigationOptions.add(optionWomen);

                NavigationOption optionAdo = new NavigationOption(R.drawable.ic_adolescent_un, R.drawable.ic_adolescent_s, R.string.menu_ado_clients, CoreConstants.DrawerMenu.ADO, 0);
                navigationOptions.add(optionAdo);

                NavigationOption optionChild = new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.menu_child_clients, CoreConstants.DrawerMenu.CHILD_CLIENTS, 0);
                optionChild.setNeedToExpand(true);
                optionChild.setNavigationSubModel(new NavigationSubModel(R.string.menu_anc_risk_clients,0,CoreConstants.DrawerMenu.CHILD_RISK));
                navigationOptions.add(optionChild);

//                NavigationOption iycfAdo = new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.menu_iycf_clients, CoreConstants.DrawerMenu.IYCF, 0);
//                navigationOptions.add(iycfAdo);
            }


           // navigationOptions.add(new NavigationOption(R.drawable.ic_forum_un, R.drawable.ic_forum, R.string.menu_forum, CoreConstants.DrawerMenu.FORUM, -1));

            navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_guest_member, CoreConstants.DrawerMenu.GUEST_MEMBER, -1));

//            if(!HnppConstants.isPALogin()){
//                navigationOptions.add(new NavigationOption(R.drawable.ic_ss_icon_un, R.drawable.ic_ss_icon, R.string.menu_ss_info, CoreConstants.DrawerMenu.SS_INFO, -1));
//            }

        }

        return navigationOptions;
    }
}
