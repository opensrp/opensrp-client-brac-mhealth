package org.smartregister.brac.hnpp.fragment;

import static com.vijay.jsonwizard.utils.Utils.hideProgressDialog;
import static com.vijay.jsonwizard.utils.Utils.showProgressDialog;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rey.material.widget.Button;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.listener.OnUpdateMemberList;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.MemberListPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.family.util.Constants;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

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
    AppCompatTextView pregnancyCountTv;

    boolean isValidateNewborn = false;
    boolean isValidateDeath = false;
    boolean isValidateMigration = false;
    boolean isValidatePregReg = false;
    public static MemberListPresenter memberHistoryPresenter;
    private String familyId = "";
    HouseHoldVisitActivity activity;

    public HouseHoldFormTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_house_hold_form_type, container, false);
        activity = ((HouseHoldVisitActivity) getActivity());
        initUi(view);

        ((HouseHoldVisitActivity) getActivity()).listenMemberUpdateStatusFromFrag(new OnUpdateMemberList() {
            @Override
            public void update(boolean isNeedUpdate) {
                updateUi();
            }
        });

        return view;
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
        pregnancyCountTv = view.findViewById(R.id.pregnancy_count_tv);

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

                if (activity.memberListJson.size() > 0) {
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
                showMemberListDialog(MemberTypeEnum.DEATH);
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

                if (activity.removedMemberListJson.size() > 0) {
                    showMemberListDialog(MemberTypeEnum.DEATH);
                } else {

                }
            }
        });

        migrationInfoLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMemberListDialog(MemberTypeEnum.MIGRATION);
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

                if (activity.migratedMemberListJson.size() > 0) {
                    showMemberListDialog(MemberTypeEnum.MIGRATION);
                }
            }
        });

        ///pregnancy registration button handle
        noPregnancyBt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if ((view.getTag()) == null) {
                    pregnancyCheckIm.setImageResource(R.drawable.success);
                    pregnancyCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    pregnancyRegLay.setClickable(true);
                    isValidatePregReg = true;
                    return;
                }

                if (activity.pregancyMemberListJson.size() > 0) {
                    showMemberListDialog(MemberTypeEnum.ELCO);
                } else {

                }
            }
        });

        pregnancyRegLay.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                showMemberListDialog(MemberTypeEnum.ELCO);
            }
        });

        hhUpdateLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGPSLocation(familyId);
            }
        });

    }

    private void showMemberListDialog(MemberTypeEnum memberTypeEnum) {
        memberHistoryPresenter.fetchMemberList(memberTypeEnum);
        ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
        MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
        memberListDialogFragment.setContext(getActivity());
        memberListDialogFragment.setData(memberArrayList,memberTypeEnum);
        memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
    }

    @Override
    public void onResume() {
        super.onResume();

        //updateUi();

    }


    private void updateUi() {
        //newborn component handling
        if (activity.memberListJson.size() > 0) {
            newBornCountTv.setVisibility(View.VISIBLE);
            newBornCountTv.setText(String.format(new Locale("bn"), "%d%s", activity.memberListJson.size(), getActivity().getString(R.string.record_added)));
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
        if (activity.removedMemberListJson.size() > 0) {
            deathCountTv.setVisibility(View.VISIBLE);
            deathCountTv.setText(String.format(new Locale("bn"), "%d%s", activity.removedMemberListJson.size(), getActivity().getString(R.string.record_added)));
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
        if (activity.migratedMemberListJson.size() > 0) {
            migrationCountTv.setVisibility(View.VISIBLE);
            migrationCountTv.setText(String.format(new Locale("bn"), "%d%s", activity.migratedMemberListJson.size(), getActivity().getString(R.string.record_added)));
            noMigrationBt.setText(R.string.add_more_member);
            noMigrationBt.setTag(true);
            migrationCheckIm.setImageResource(R.drawable.success);
            migrationCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            migrationInfoLay.setClickable(false);
            isValidateMigration = true;
        } else {
            migrationCountTv.setVisibility(View.GONE);
        }

        //pregnancy reg component handling
        if (activity.pregancyMemberListJson.size() > 0) {
            pregnancyCountTv.setVisibility(View.VISIBLE);
            pregnancyCountTv.setText(String.format(new Locale("bn"), "%d%s", activity.pregancyMemberListJson.size(), getActivity().getString(R.string.record_added)));
            noPregnancyBt.setText(R.string.add_more_member);
            noPregnancyBt.setTag(true);
            pregnancyCheckIm.setImageResource(R.drawable.success);
            pregnancyCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            pregnancyRegLay.setClickable(false);
            isValidatePregReg = true;
        } else {
            pregnancyCountTv.setVisibility(View.GONE);
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
        familyId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, "");
        memberHistoryPresenter = new MemberListPresenter(this,familyId);
    }

    @Override
    public MemberListContract.Presenter getPresenter() {
        return null;
    }

    @Override
    public Context getContext() {
        return null;
    }

    private void getGPSLocation(String familyBaseEntityId){
        HnppConstants.getGPSLocation(((CoreFamilyProfileActivity) getActivity()), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try{
                    Map<String,String> hhByBaseEntityId = HnppDBUtils.getDetails(familyBaseEntityId,"ec_family");
                    JSONObject jsonForm = FormUtils.getInstance(getActivity().getApplicationContext()).getFormJson(HnppConstants.JSON_FORMS.HOME_VISIT_FAMILY);
                    HnppJsonFormUtils.setEncounterDateTime(jsonForm);

                    HnppJsonFormUtils.updateHhVisitForm(jsonForm, hhByBaseEntityId);
                    ArrayList<String[]> memberList = HnppDBUtils.getAllMembersInHouseHold(familyBaseEntityId);
                    HnppJsonFormUtils.updateFormWithAllMemberName(jsonForm,memberList);
                    HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    startHHFormActivity(jsonForm,familyBaseEntityId,REQUEST_HOME_VISIT);

                }catch (Exception e){
                    e.printStackTrace();
                    hideProgressDialog();
                }
            }
        });

    }

    public void startHHFormActivity(JSONObject jsonForm,String familyBaseEntityId,  int requestCode) {
        if(TextUtils.isEmpty(familyBaseEntityId)){
            Toast.makeText(getActivity(),"BaseEntityId showing empty",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            jsonForm.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, familyBaseEntityId);
            Intent intent;
            intent = new Intent(getActivity(), org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            if(HnppConstants.isPALogin()){
                form.setHideSaveLabel(true);
                form.setSaveLabel("");
            }

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, requestCode);
            }
        }catch (Exception e){

        }
    }
}