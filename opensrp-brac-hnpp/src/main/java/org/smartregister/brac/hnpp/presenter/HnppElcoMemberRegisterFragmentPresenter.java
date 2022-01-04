package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.HnppElcoMemberRegisterFragmentModel;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.presenter.CoreChildRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.family.util.DBConstants;

import java.util.List;

public class HnppElcoMemberRegisterFragmentPresenter extends CoreChildRegisterFragmentPresenter {

    private HnppElcoMemberRegisterFragmentModel model;

    public HnppElcoMemberRegisterFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreChildRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
        this.model = (HnppElcoMemberRegisterFragmentModel)model;
    }
    @Override
    public void processViewConfigurations() {
        super.processViewConfigurations();

    }

    @Override
    public void initializeQueries(String mainCondition) {
        String countSelect = model.countSelect(CoreConstants.TABLE_NAME.FAMILY_MEMBER, mainCondition);
        String mainSelect = model.mainSelect(null,CoreConstants.TABLE_NAME.FAMILY, CoreConstants.TABLE_NAME.FAMILY_MEMBER, mainCondition);
        getView().initializeQueryParams(CoreConstants.TABLE_NAME.FAMILY_MEMBER, countSelect, mainSelect);
        getView().initializeAdapter(visibleColumns);

        //getView().countExecute();

        getView().filterandSortInInitializeQueries();
    }
    @Override
    public void updateSortAndFilter(List<Field> filterList, Field sortField) {
        String filterText = model.getFilterText(filterList, getView().getString(org.smartregister.R.string.filter));
        String sortText = model.getSortText(sortField);

        getView().updateFilterAndFilterStatus(filterText, sortText);
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s is null AND %s", CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.DATE_REMOVED, ChildDBConstants.elcoFilter());
    }

    @Override
    public String getMainCondition(String tableName) {
        return String.format(" %s is null AND %s", tableName + "." + DBConstants.KEY.DATE_REMOVED, ChildDBConstants.elcoFilter());
    }
    @Override
    public String getDefaultSortQuery() {
        return CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ";// AND "+ChildDBConstants.childAgeLimitFilter();
    }
}
