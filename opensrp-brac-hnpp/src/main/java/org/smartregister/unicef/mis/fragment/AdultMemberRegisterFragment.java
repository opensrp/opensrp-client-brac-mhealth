package org.smartregister.unicef.mis.fragment;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.HnppAllMemberRegisterFragmentModel;
import org.smartregister.unicef.mis.presenter.AdultMemberRegisterFragmentPresenter;
import org.smartregister.view.activity.BaseRegisterActivity;

public class AdultMemberRegisterFragment extends HnppAllMemberRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new AdultMemberRegisterFragmentPresenter(this, new HnppAllMemberRegisterFragmentModel(), viewConfigurationIdentifier);
        isNeedToShowDateFilter = true;
    }
    @Override
    protected int getToolBarTitle() {
        return R.string.menu_adult;
    }
}
