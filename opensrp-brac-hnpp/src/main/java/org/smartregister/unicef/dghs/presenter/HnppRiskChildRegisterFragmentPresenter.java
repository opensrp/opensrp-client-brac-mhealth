package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.utils.ChildDBConstants;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.presenter.CoreChildRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

public class HnppRiskChildRegisterFragmentPresenter extends CoreChildRegisterFragmentPresenter {

    public HnppRiskChildRegisterFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreChildRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s is null AND %s %s", CoreConstants.TABLE_NAME.CHILD+ "." + DBConstants.KEY.DATE_REMOVED,ChildDBConstants.childAgeLimitFilter(CoreConstants.TABLE_NAME.CHILD),ChildDBConstants.riskChildPatient());
    }

    @Override
    public String getMainCondition(String tableName) {
        return String.format(" %s is null AND %s %s", tableName + "." + DBConstants.KEY.DATE_REMOVED, ChildDBConstants.childAgeLimitFilter(tableName),ChildDBConstants.riskChildPatient());
    }
    @Override
    public String getDefaultSortQuery() {
        return  CoreConstants.TABLE_NAME.CHILD+ "." +DBConstants.KEY.LAST_INTERACTED_WITH + " DESC" ;
    }
}
