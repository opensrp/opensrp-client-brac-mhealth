package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.model.HnppAllMemberRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.AdultMemberRegisterFragmentPresenter;
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
}
