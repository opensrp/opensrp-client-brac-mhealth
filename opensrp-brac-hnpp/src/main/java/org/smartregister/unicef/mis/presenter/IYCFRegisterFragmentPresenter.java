package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.utils.ChildDBConstants;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.presenter.CoreChildRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

public class IYCFRegisterFragmentPresenter extends CoreChildRegisterFragmentPresenter {

    public IYCFRegisterFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreChildRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s is null AND %s ", CoreConstants.TABLE_NAME.CHILD+ "." + DBConstants.KEY.DATE_REMOVED,ChildDBConstants.IycfFilterWithTableName());
    }

    @Override
    public String getMainCondition(String tableName) {
        return String.format(" %s is null AND %s ", tableName + "." + DBConstants.KEY.DATE_REMOVED,ChildDBConstants.IycfFilterWithTableName());
    }
    @Override
    public String getDefaultSortQuery() {
        return  CoreConstants.TABLE_NAME.CHILD+ "." +DBConstants.KEY.LAST_INTERACTED_WITH + " DESC" ;
    }
}
