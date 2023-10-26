package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.interactor.MemberListInteractor;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;

import java.util.ArrayList;


public class MemberListPresenter implements MemberListContract.Presenter, MemberListContract.InteractorCallBack {
    private MemberListContract.View view;
    private MemberListContract.Interactor interactor;
    String familyId;

    public MemberListPresenter(MemberListContract.View view,String familyId){
        this.view = view;
        this.familyId = familyId;
        interactor = new MemberListInteractor(familyId);
    }



    @Override
    public void fetchMemberList(MemberTypeEnum memberType) {
        getView().showProgressBar();
        interactor.fetchMember(this,memberType);
    }

    public ArrayList<Member> getMemberList(){
        return interactor.getMemberList();
    }

    @Override
    public MemberListContract.View getView() {
        return view;
    }

    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
    }
}
