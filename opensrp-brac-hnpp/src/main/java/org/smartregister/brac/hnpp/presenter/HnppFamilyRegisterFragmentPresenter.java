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
        super.processViewConfigurations();
        if (config.getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(getView().getContext().getString(R.string.search_name_or_id));
        }
    }

    @Override
    public String getDefaultSortQuery() {
        return isSortByLastVisit? HnppConstants.KEY.LAST_HOME_VISIT+" DESC ":DBConstants.KEY.UNIQUE_ID + " DESC ";
    }
    public boolean isSortByLastVisit;
}
