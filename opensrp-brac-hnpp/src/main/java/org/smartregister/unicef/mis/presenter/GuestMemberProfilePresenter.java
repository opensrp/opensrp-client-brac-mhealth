package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.contract.GuestMemberContract;

public class GuestMemberProfilePresenter  extends GuestMemberPresenter{
    public GuestMemberProfilePresenter(GuestMemberContract.View view) {
        super(view);
    }

    @Override
    public void successfullySaved() {
        getView().updateSuccessfullyFetchMessage();
    }

}
