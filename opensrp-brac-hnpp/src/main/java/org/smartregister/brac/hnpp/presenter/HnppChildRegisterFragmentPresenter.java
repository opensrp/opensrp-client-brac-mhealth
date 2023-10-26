package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.utils.ChildDBConstants;
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
        return String.format(" %s is null AND %s ", CoreConstants.TABLE_NAME.CHILD+ "." + DBConstants.KEY.DATE_REMOVED,ChildDBConstants.childAgeLimitFilter(CoreConstants.TABLE_NAME.CHILD));
    }

    @Override
    public String getMainCondition(String tableName) {
        return String.format(" %s is null AND %s", tableName + "." + DBConstants.KEY.DATE_REMOVED, ChildDBConstants.childAgeLimitFilter(tableName));
    }
    @Override
    public String getDefaultSortQuery() {
        return  CoreConstants.TABLE_NAME.CHILD+ "." +DBConstants.KEY.LAST_INTERACTED_WITH + " DESC" ;
    }
}
