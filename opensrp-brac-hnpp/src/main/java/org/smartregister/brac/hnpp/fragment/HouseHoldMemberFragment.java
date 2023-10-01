package org.smartregister.brac.hnpp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.adapter.HouseHoldMemberListAdapter;
import org.smartregister.brac.hnpp.adapter.MemberListAdapter;
import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.listener.OnEachMemberDueValidate;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.MemberListPresenter;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;

public class HouseHoldMemberFragment extends Fragment implements MemberListContract.View {
    public static String TAG = "HouseHoldMemberFragment";

    HouseHoldMemberListAdapter adapter;
    private MemberListPresenter memberHistoryPresenter;
    private String familyId = "";

    HouseHoldMemberDueFragment profileMemberFragment;
    HouseHoldChildProfileDueFragment childProfileDueFragment;
    public ArrayList<Member> memberArrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_house_hold_member, container, false);
        initializeMemberPresenter();

        memberHistoryPresenter.fetchMemberList(MemberTypeEnum.MIGRATION);
        memberArrayList = memberHistoryPresenter.getMemberList();
        adapter = new HouseHoldMemberListAdapter(getActivity(), new HouseHoldMemberListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, Member content) {
                CommonPersonObjectClient commonPersonObjectClient = clientObject(content.getBaseEntityId());
                int age = FormApplicability.getAge(commonPersonObjectClient);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.INTENT_KEY.BASE_ENTITY_ID, content.getBaseEntityId());
                bundle.putAll(getArguments());
                bundle.putParcelable(HnppConstants.MEMBER, content);
                bundle.putInt(HnppConstants.POSITION, position);

                // for child
                if(age <= 5){
                    childProfileDueFragment = (HouseHoldChildProfileDueFragment) HouseHoldChildProfileDueFragment.newInstance(bundle);
                    childProfileDueFragment.setCommonPersonObjectClient(commonPersonObjectClient);
                    ((HouseHoldVisitActivity) getActivity()).setupFragment(childProfileDueFragment, HouseHoldChildProfileDueFragment.TAG, bundle);
                }
                // for member
                else {
                    profileMemberFragment = HouseHoldMemberDueFragment.newInstance(bundle);
                    profileMemberFragment.setCommonPersonObjectClient(commonPersonObjectClient);
                    ((HouseHoldVisitActivity) getActivity()).setupFragment(profileMemberFragment, HouseHoldMemberDueFragment.TAG, bundle);
                }
            }
        });

        adapter.setData(memberArrayList);
        RecyclerView recyclerView = view.findViewById(R.id.memberListRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        HouseHoldVisitActivity activity = ((HouseHoldVisitActivity) getActivity());

        //listening callback
        //if members due data valid or not
        //if valid then update status
        //this callback called from HouseHoldChildProfileDueFragment/HouseHoldMemberDueFragment
        activity.isValidateDueData(new OnEachMemberDueValidate() {
            @Override
            public void validate(int isValidate, int pos) {
                memberArrayList.get(pos).setStatus(isValidate);
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    /**
     * is validate all members
     * @return status
     */
    public boolean isValidateHHMembers() {
        for (Member member : memberArrayList){
            if(member.getStatus() == 3){
                return false;
            }
        }
        return true;
    }


    /**
     * checking is any data added or not for member
     * @return
     */
    public boolean isAnyDataAdded() {
        int countDefault = 0;
        for (Member member : memberArrayList){
            if(member.getStatus() == 3 || member.getStatus() == 2){
               countDefault++;
            }
        }
        return countDefault != memberArrayList.size();
    }

    /**
     * creating common person object
     * @param baseEntityId
     * @return
     */
    private CommonPersonObjectClient clientObject(String baseEntityId) {
        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void initializeMemberPresenter() {
        familyId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, "");
        memberHistoryPresenter = new MemberListPresenter(this, familyId);
    }

    @Override
    public MemberListContract.Presenter getPresenter() {
        return null;
    }
}