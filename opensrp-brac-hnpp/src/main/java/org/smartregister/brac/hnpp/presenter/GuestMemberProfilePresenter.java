package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.GuestMemberContract;

public class GuestMemberProfilePresenter  extends GuestMemberPresenter{
    public GuestMemberProfilePresenter(GuestMemberContract.View view) {
        super(view);
    }

    @Override
    public void successfullySaved() {
        getView().updateSuccessfullyFetchMessage();
    }

}
