package org.smartregister.unicef.mis.fragment;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.HnppAllMemberRegisterFragmentModel;
import org.smartregister.unicef.mis.presenter.AdoMemberRegisterFragmentPresenter;
import org.smartregister.unicef.mis.presenter.HnppAllMemberRegisterFragmentPresenter;
import org.smartregister.unicef.mis.provider.HnppAdoMemberRegisterProvider;
import org.smartregister.unicef.mis.provider.HnppAllMemberRegisterProvider;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.Set;

public class ReferralMemberRegisterFragment extends HnppAllMemberRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new HnppAllMemberRegisterFragmentPresenter(this, new HnppAllMemberRegisterFragmentModel(), viewConfigurationIdentifier);
        isNeedToShowDateFilter = true;
    }
    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HnppAllMemberRegisterProvider childRegisterProvider = new HnppAllMemberRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
    @Override
    public void countExecute() {
//        visitType = "and ec_visit_log.visit_type ='Adolescent package'";
//        super.countExecute();
    }

    @Override
    protected String filterandSortQuery() {
        referred = "and ec_family_member.is_refered ='true'";
        return super.filterandSortQuery();
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.menu_referral_clients;
    }
}
