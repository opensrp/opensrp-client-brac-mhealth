package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.utils.BkashStatus;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;

import java.util.ArrayList;

public interface MemberListContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void initializeMemberPresenter();
        Presenter getPresenter();
        Context getContext();

    }
    interface Presenter{
        void fetchMemberList(MemberTypeEnum memberType);
        View getView();
    }
    interface Interactor{
        ArrayList<Member> getMemberList();
        void fetchMember(MemberListContract.InteractorCallBack callBack,MemberTypeEnum memberTypeEnum);

    }

    interface InteractorCallBack{
        void fetchedSuccessfully();
    }
}

