package org.smartregister.brac.hnpp.presenter;

import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.presenter.CoreChildRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

public class HnppChildRegisterFragmentPresenter extends CoreChildRegisterFragmentPresenter {

    public HnppChildRegisterFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreChildRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s is null ", CoreConstants.TABLE_NAME.CHILD+ "." + DBConstants.KEY.DATE_REMOVED);
    }

    @Override
    public String getMainCondition(String tableName) {
        return String.format(" %s is null ", tableName + "." + DBConstants.KEY.DATE_REMOVED);
    }
    @Override
    public String getDefaultSortQuery() {
        return  CoreConstants.TABLE_NAME.CHILD+ "." +DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ";// AND "+ChildDBConstants.childAgeLimitFilter();
    }
}
