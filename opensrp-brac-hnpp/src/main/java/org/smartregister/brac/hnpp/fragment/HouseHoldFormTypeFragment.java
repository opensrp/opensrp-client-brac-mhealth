package org.smartregister.brac.hnpp.fragment;

import static com.vijay.jsonwizard.utils.Utils.hideProgressDialog;
import static com.vijay.jsonwizard.utils.Utils.showProgressDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rey.material.widget.Button;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.MemberListPresenter;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.family.util.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class HouseHoldFormTypeFragment extends Fragment implements MemberListContract.View {
    public static String TAG = "HouseHoldFormTypeFragment";

    LinearLayout newBornLay;
    LinearLayout deathInfoLay;
    LinearLayout migrationInfoLay;
    LinearLayout pregnancyRegLay;
    LinearLayout hhUpdateLay;

    Button noNewBornBt;
    Button noDeathBt;
    Button noMigrationBt;
    Button noPregnancyBt;

    AppCompatImageView newBornCheckIm;
    AppCompatImageView deathCheckIm;
    AppCompatImageView migrationCheckIm;
    AppCompatImageView pregnancyCheckIm;

    AppCompatImageView hh_info_CheckIm;

    AppCompatTextView newBornCountTv;
    AppCompatTextView deathCountTv;
    AppCompatTextView migrationCountTv;

    boolean isValidateNewborn = false;
    boolean isValidateDeath = false;
    boolean isValidateMigration = false;
    public static MemberListPresenter memberHistoryPresenter;

    public HouseHoldFormTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_house_hold_form_type, container, false);
        initUi(view);


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void initUi(View view) {
        initializeMemberPresenter();

        newBornLay = view.findViewById(R.id.newborn_lay);
        deathInfoLay = view.findViewById(R.id.death_info_lay);
        migrationInfoLay = view.findViewById(R.id.migration_info_lay);
        pregnancyRegLay = view.findViewById(R.id.pregnancy_lay);
        hhUpdateLay = view.findViewById(R.id.hh_info_update_lay);

        noNewBornBt = view.findViewById(R.id.no_new_born_bt);
        noDeathBt = view.findViewById(R.id.dead_info_bt);
        noMigrationBt = view.findViewById(R.id.migration_info_bt);
        noPregnancyBt = view.findViewById(R.id.pregnancy_bt);

        newBornCheckIm = view.findViewById(R.id.newborn_check_im);
        deathCheckIm = view.findViewById(R.id.death_info_im);
        migrationCheckIm = view.findViewById(R.id.migration_info_im);
        pregnancyCheckIm = view.findViewById(R.id.pregnancy_check_im);
        hh_info_CheckIm = view.findViewById(R.id.hh_info_check_im);


        newBornCountTv = view.findViewById(R.id.new_born_count_tv);
        deathCountTv = view.findViewById(R.id.death_count_tv);
        migrationCountTv = view.findViewById(R.id.migration_count_tv);

        ///new born button handle
        noNewBornBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if ((view.getTag()) == null) {
                    newBornCheckIm.setImageResource(R.drawable.success);
                    newBornCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    newBornLay.setClickable(true);
                    isValidateNewborn = true;
                    return;
                }

                if (HouseHoldVisitActivity.memberListJson.size() > 0) {
                    //newBornLay.setChecked(true);
                    AddCustomMemberFragment addmemberFragment = AddCustomMemberFragment.newInstance();
                    addmemberFragment.setContext(getActivity());
                    addmemberFragment.show(getActivity().getFragmentManager(), AddCustomMemberFragment.DIALOG_TAG);
                } else {
                }

            }
        });

        newBornLay.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                AddCustomMemberFragment addmemberFragment = AddCustomMemberFragment.newInstance();
                addmemberFragment.setContext(getActivity());
                addmemberFragment.show(getActivity().getFragmentManager(), AddCustomMemberFragment.DIALOG_TAG);
            }
        });


        ///death info button handle
        deathInfoLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberHistoryPresenter.fetchMemberList(MemberTypeEnum.DEATH);
                ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
                MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
                memberListDialogFragment.setContext(getActivity());
                memberListDialogFragment.setData(memberArrayList,MemberTypeEnum.DEATH);
                memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
            }
        });

        noDeathBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if ((view.getTag()) == null) {
                    deathCheckIm.setImageResource(R.drawable.success);
                    deathCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    deathInfoLay.setClickable(true);
                    isValidateDeath = true;
                    return;
                }

                if (HouseHoldVisitActivity.removedMemberListJson.size() > 0) {
                    memberHistoryPresenter.fetchMemberList(MemberTypeEnum.DEATH);
                    ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
                    MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
                    memberListDialogFragment.setContext(getActivity());
                    memberListDialogFragment.setData(memberArrayList, MemberTypeEnum.DEATH);
                    memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
                } else {

                }
            }
        });

        migrationInfoLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberHistoryPresenter.fetchMemberList(MemberTypeEnum.DEATH);
                ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
                MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
                memberListDialogFragment.setContext(getActivity());
                memberListDialogFragment.setData(memberArrayList, MemberTypeEnum.MIGRATION);
                memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
            }
        });

        noMigrationBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if ((view.getTag()) == null) {
                    migrationCheckIm.setImageResource(R.drawable.success);
                    migrationCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    migrationInfoLay.setClickable(true);
                    isValidateMigration = true;
                    return;
                }

                if (HouseHoldVisitActivity.migratedMemberListJson.size() > 0) {
                    memberHistoryPresenter.fetchMemberList(MemberTypeEnum.DEATH);
                    ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
                    MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
                    memberListDialogFragment.setContext(getActivity());
                    memberListDialogFragment.setData(memberArrayList, MemberTypeEnum.MIGRATION);
                    memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
                } else {

                }
            }
        });

        ///pregnancy registration button handle
        noPregnancyBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                //pregnancyRegRadio.setChecked(true);
                memberHistoryPresenter.fetchMemberList(MemberTypeEnum.ELCO);
                ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
                MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
                memberListDialogFragment.setContext(getActivity());
                memberListDialogFragment.setData(memberArrayList, MemberTypeEnum.DEATH);
                memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
            }
        });

        pregnancyRegLay.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //newborn component handling
        if (HouseHoldVisitActivity.memberListJson.size() > 0) {
            newBornCountTv.setVisibility(View.VISIBLE);
            newBornCountTv.setText(String.format(new Locale("bn"), "%d%s", HouseHoldVisitActivity.memberListJson.size(), getActivity().getString(R.string.record_added)));
            noNewBornBt.setText(R.string.add_more_member);
            noNewBornBt.setTag(true);
            newBornCheckIm.setImageResource(R.drawable.success);
            newBornCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            newBornLay.setClickable(false);
            isValidateNewborn = true;
        } else {
            newBornCountTv.setVisibility(View.GONE);
        }

        //death component handling
        if (HouseHoldVisitActivity.removedMemberListJson.size() > 0) {
            deathCountTv.setVisibility(View.VISIBLE);
            deathCountTv.setText(String.format(new Locale("bn"), "%d%s", HouseHoldVisitActivity.removedMemberListJson.size(), getActivity().getString(R.string.record_added)));
            noDeathBt.setText(R.string.add_more_member);
            noDeathBt.setTag(true);
            deathCheckIm.setImageResource(R.drawable.success);
            deathCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            deathInfoLay.setClickable(false);
            isValidateDeath = true;
        } else {
            deathCountTv.setVisibility(View.GONE);
        }

        //migration component handling
        if (HouseHoldVisitActivity.migratedMemberListJson.size() > 0) {
            migrationCountTv.setVisibility(View.VISIBLE);
            migrationCountTv.setText(String.format(new Locale("bn"), "%d%s", HouseHoldVisitActivity.migratedMemberListJson.size(), getActivity().getString(R.string.record_added)));
            noMigrationBt.setText(R.string.add_more_member);
            noMigrationBt.setTag(true);
            migrationCheckIm.setImageResource(R.drawable.success);
            migrationCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            migrationInfoLay.setClickable(false);
            isValidateMigration = true;
        } else {
            migrationCountTv.setVisibility(View.GONE);
        }

    }


    @Override
    public void showProgressBar() {
        showProgressDialog(R.string.loading_location, R.string.exit_app_message, getActivity());
    }

    @Override
    public void hideProgressBar() {
        hideProgressDialog();
    }

    @Override
    public void initializeMemberPresenter() {
        memberHistoryPresenter = new MemberListPresenter(this, getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, ""));
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