package org.smartregister.unicef.dghs.fragment;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.model.HnppAllMemberRegisterFragmentModel;
import org.smartregister.unicef.dghs.presenter.AdultMemberRegisterFragmentPresenter;
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
