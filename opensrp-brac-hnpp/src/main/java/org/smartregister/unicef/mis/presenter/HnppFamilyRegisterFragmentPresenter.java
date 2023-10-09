package org.smartregister.unicef.mis.presenter;

import org.smartregister.family.contract.FamilyRegisterFragmentContract;
import org.smartregister.family.presenter.BaseFamilyRegisterFragmentPresenter;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.family.util.DBConstants;

import static org.smartregister.unicef.mis.utils.HnppConstants.isSortByLastVisit;

public class HnppFamilyRegisterFragmentPresenter extends BaseFamilyRegisterFragmentPresenter implements FamilyRegisterFragmentContract.Presenter {
    public HnppFamilyRegisterFragmentPresenter(org.smartregister.family.contract.FamilyRegisterFragmentContract.View view, org.smartregister.family.contract.FamilyRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
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
        return isSortByLastVisit? HnppConstants.KEY.LAST_HOME_VISIT+" DESC ":DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ";
    }

}
