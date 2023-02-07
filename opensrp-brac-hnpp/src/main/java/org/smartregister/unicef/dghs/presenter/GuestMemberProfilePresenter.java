package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.contract.GuestMemberContract;

public class GuestMemberProfilePresenter  extends GuestMemberPresenter{
    public GuestMemberProfilePresenter(GuestMemberContract.View view) {
        super(view);
    }

    @Override
    public void successfullySaved() {
        getView().updateSuccessfullyFetchMessage();
    }

}
