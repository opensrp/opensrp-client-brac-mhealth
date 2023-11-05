package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBConstants;
import org.smartregister.chw.core.presenter.FamilyRegisterFragmentPresenter;
import org.smartregister.family.contract.FamilyRegisterFragmentContract;
import org.smartregister.family.util.DBConstants;


public class HnppFamilyRegisterFragmentPresenter extends FamilyRegisterFragmentPresenter {
    public HnppFamilyRegisterFragmentPresenter(FamilyRegisterFragmentContract.View view, FamilyRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }
    @Override
    public void processViewConfigurations() {
        try{
            super.processViewConfigurations();
            if (config.getSearchBarText() != null && getView() != null) {
                getView().updateSearchBarHint(getView().getContext().getString(R.string.search_name_or_id));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public String getDefaultSortQuery() {
        switch (HnppConstants.sSortedBy){
            case HnppConstants.SORT_BY.SORT_BY_LAST_VISIT:
                return HnppConstants.KEY.LAST_HOME_VISIT+" DESC ";
            case HnppConstants.SORT_BY.SORT_BY_REGIGTRATION:
                return DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ";
            case HnppConstants.SORT_BY.SORT_BY_SERIAL:
                return HnppConstants.KEY.SERIAL_NO + " DESC ";
        }
         return DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ";
    }

}
