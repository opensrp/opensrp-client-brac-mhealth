package org.smartregister.brac.hnpp.fragment;

import static com.vijay.jsonwizard.utils.Utils.hideProgressDialog;
import static com.vijay.jsonwizard.utils.Utils.showProgressDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.rey.material.widget.Button;
import com.rey.material.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.FamilyRemoveMemberActivity;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.MemberListPresenter;
import org.smartregister.brac.hnpp.utils.HnppDBConstants;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class HouseHoldFormTypeFragment extends Fragment implements MemberListContract.View {
    public static String TAG = "HouseHoldFormTypeFragment";

    RadioButton newBornRadio;
    RadioButton deathInfoRadio;
    RadioButton pregnancyRegRadio;
    RadioButton hhUpdate;

    Button noNewBornBt;
    Button noDeathBornBt;
    Button noPregnancyBt;

    TextView newBornCountTv;
    public static MemberListPresenter memberHistoryPresenter;

    public HouseHoldFormTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_house_hold_form_type, container, false);
        initUi(view);


        return view;
    }

    private void initUi(View view) {
        initializeMemberPresenter();

        newBornRadio = view.findViewById(R.id.new_born_radio);
        deathInfoRadio = view.findViewById(R.id.death_info_radio);
        pregnancyRegRadio = view.findViewById(R.id.pregnancy_reg_radio);
        hhUpdate = view.findViewById(R.id.hh_info_update_radio);

        noNewBornBt = view.findViewById(R.id.no_new_born_bt);
        noDeathBornBt = view.findViewById(R.id.no_death_bt);
        noPregnancyBt = view.findViewById(R.id.no_pregnancy_bt);

        newBornCountTv = view.findViewById(R.id.new_born_count_tv);

        ///new born button handle
        noNewBornBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if(HouseHoldVisitActivity.memberListJson.size()>0){
                    newBornRadio.setChecked(true);
                    AddCustomMemberFragment addmemberFragment = AddCustomMemberFragment.newInstance();
                    addmemberFragment.setContext(getActivity());
                    addmemberFragment.show(getActivity().getFragmentManager(), AddCustomMemberFragment.DIALOG_TAG);
                    newBornRadio.setButtonTintList(getActivity().getResources().getColorStateList(R.color.deep_green));
                }else {
                    newBornRadio.setChecked(true);
                    newBornRadio.setButtonTintList(getActivity().getResources().getColorStateList(R.color.pnc_circle_yellow));
                }
            }
        });

        newBornRadio.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                newBornRadio.setChecked(true);
                AddCustomMemberFragment addmemberFragment = AddCustomMemberFragment.newInstance();
                addmemberFragment.setContext(getActivity());
                addmemberFragment.show(getActivity().getFragmentManager(), AddCustomMemberFragment.DIALOG_TAG);
                newBornRadio.setButtonTintList(getActivity().getResources().getColorStateList(R.color.deep_green));
            }
        });


        ///death info button handle
        noDeathBornBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                deathInfoRadio.setChecked(true);
               /* Intent frm_intent = new Intent(getActivity(), FamilyRemoveMemberActivity.class);
                frm_intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID,""));
                frm_intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, getArguments().getString(Constants.INTENT_KEY.FAMILY_HEAD,""));
                frm_intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, getArguments().getString(Constants.INTENT_KEY.PRIMARY_CAREGIVER,""));
                frm_intent.putExtra("from", "true");
                startActivityForResult(frm_intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);*/
                memberHistoryPresenter.fetchMemberList(MemberTypeEnum.DEATH);
                ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
                MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
                memberListDialogFragment.setContext(getActivity());
                memberListDialogFragment.setData(memberArrayList);
                memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
                deathInfoRadio.setButtonTintList(getActivity().getResources().getColorStateList(R.color.pnc_circle_yellow));
            }
        });

        deathInfoRadio.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                deathInfoRadio.setChecked(true);
                deathInfoRadio.setButtonTintList(getActivity().getResources().getColorStateList(R.color.deep_green));
            }
        });

        ///pregnancy registration button handle
        noPregnancyBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                pregnancyRegRadio.setChecked(true);
                memberHistoryPresenter.fetchMemberList(MemberTypeEnum.ELCO);
                ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
                MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
                memberListDialogFragment.setContext(getActivity());
                memberListDialogFragment.setData(memberArrayList);
                memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
                pregnancyRegRadio.setButtonTintList(getActivity().getResources().getColorStateList(R.color.pnc_circle_yellow));
            }
        });

        pregnancyRegRadio.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                pregnancyRegRadio.setChecked(true);
                pregnancyRegRadio.setButtonTintList(getActivity().getResources().getColorStateList(R.color.deep_green));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if(HouseHoldVisitActivity.memberListJson.size()>0){
            newBornCountTv.setVisibility(View.VISIBLE);
            newBornCountTv.setText(String.format(Locale.US,"%d%s", HouseHoldVisitActivity.memberListJson.size()+HouseHoldVisitActivity.removedMemberListJson.size(), getString(R.string.record_added)));
            noNewBornBt.setText(R.string.add_more_member);
            //noNewBornBt.setdrawablel
            newBornRadio.setClickable(false);
        }else {
            newBornCountTv.setVisibility(View.GONE);
        }
    }


    @Override
    public void showProgressBar() {
        Log.d("mmmm","stared prog");
        showProgressDialog(R.string.loading_location,R.string.exit_app_message,getActivity());
    }

    @Override
    public void hideProgressBar() {
        hideProgressDialog();
        Log.d("mmmm","end prog");
    }

    @Override
    public void initializeMemberPresenter() {
        memberHistoryPresenter = new MemberListPresenter(this,getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID,""));
    }

    @Override
    public MemberListContract.Presenter getPresenter() {
        return null;
    }

    @Override
    public Context getContext() {
        return null;
    }
}