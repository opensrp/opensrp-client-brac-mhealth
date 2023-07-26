package org.smartregister.brac.hnpp.interactor;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;

import java.util.ArrayList;

public class MemberListInteractor implements MemberListContract.Interactor {
    private ArrayList<Member> memberArrayList;
    String familyId;

    public MemberListInteractor(String familyId) {
        this.memberArrayList = new ArrayList<>();
        this.familyId = familyId;
    }

    @Override
    public ArrayList<Member> getMemberList() {
        return memberArrayList;
    }

    @Override
    public void fetchMember(MemberListContract.InteractorCallBack callBack, MemberTypeEnum memberTypeEnum) {
        memberArrayList =  HnppApplication.getMemberListRepository().getMemberList(familyId,memberTypeEnum);
        callBack.fetchedSuccessfully();
    }
}
