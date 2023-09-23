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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_house_hold_member, container, false);
        initializeMemberPresenter();

        memberHistoryPresenter.fetchMemberList(MemberTypeEnum.MIGRATION);
        ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
        adapter = new HouseHoldMemberListAdapter(getActivity(), new HouseHoldMemberListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, Member content) {

                Bundle bundle = new Bundle();
                bundle.putString(Constants.INTENT_KEY.BASE_ENTITY_ID, content.getBaseEntityId());
                bundle.putAll(getArguments());
                bundle.putParcelable(HnppConstants.MEMBER, content);
                bundle.putInt(HnppConstants.POSITION, position);

                profileMemberFragment = (HouseHoldMemberDueFragment) HouseHoldMemberDueFragment.newInstance(bundle);
                profileMemberFragment.setCommonPersonObjectClient(clientObject(content.getBaseEntityId()));
                ((HouseHoldVisitActivity) getActivity()).setupFragment(profileMemberFragment, HouseHoldMemberDueFragment.TAG, bundle);
                ((HouseHoldVisitActivity) getActivity()).currentFragmentIndex++;
            }
        });

        adapter.setData(memberArrayList);
        RecyclerView recyclerView = view.findViewById(R.id.memberListRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        HouseHoldVisitActivity activity = ((HouseHoldVisitActivity) getActivity());
        activity.isValidateDueData(new OnEachMemberDueValidate() {
            @Override
            public void validate(boolean isValidate, int pos) {
                Log.d("ppppppp2", pos + "" + isValidate);

                memberArrayList.get(pos).setStatus(isValidate);
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

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