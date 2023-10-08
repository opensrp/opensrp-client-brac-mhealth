package org.smartregister.brac.hnpp.fragment;

import static android.app.Activity.RESULT_OK;
import static com.vijay.jsonwizard.constants.JsonFormConstants.EDIT_TYPE;
import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;

import static com.vijay.jsonwizard.utils.Utils.hideProgressDialog;
import static com.vijay.jsonwizard.utils.Utils.showProgressDialog;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.Button;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.activity.MigrationActivity;
import org.smartregister.brac.hnpp.activity.MigrationFilterSearchActivity;
import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.HHVisitInfoModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.MemberListPresenter;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.HouseHoldInfo;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.brac.hnpp.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HouseHoldFormTypeFragment extends Fragment implements MemberListContract.View {
    public static String TAG = "HouseHoldFormTypeFragment";
    public static int REQUEST_CODE_IMPORT_MM = 6011;
    public static String PUT_EXTRA_IMPORT_MM = "import_mm";
    public LinearLayout newBornLay;
    public LinearLayout deathInfoLay;
    public LinearLayout migrationInfoLay;
    public LinearLayout pregnancyRegLay;
    public LinearLayout hhUpdateLay;
    public LinearLayout memberImportLay;
    Button noNewBornBt;
    Button noDeathBt;
    Button noMigrationBt;
    Button noPregnancyBt;
    Button noMemberImportBtn;

    AppCompatImageView newBornCheckIm;
    AppCompatImageView deathCheckIm;
    AppCompatImageView migrationCheckIm;
    AppCompatImageView pregnancyCheckIm;
    AppCompatImageView memberImportCheckIm;
    AppCompatImageView hh_info_CheckIm;

    AppCompatTextView newBornCountTv;
    AppCompatTextView deathCountTv;
    AppCompatTextView migrationCountTv;
    AppCompatTextView pregnancyCountTv;
    AppCompatTextView memberImportCountTV;

    boolean isValidateNewborn = false;
    boolean isValidateDeath = false;
    boolean isValidateMigration = false;
    boolean isValidatePregReg = false;
    boolean isValidateMemberImport = false;
    public static boolean isValidateHhVisit = false;
    private MemberListPresenter memberHistoryPresenter;
    private String familyId = "";

    public static final ArrayList<String> memberListJson = new ArrayList<>();
    public static final ArrayList<String> removedMemberListJson = new ArrayList<>();
    public static final ArrayList<String> migratedMemberListJson = new ArrayList<>();
    public static final ArrayList<String> pregancyMemberListJson = new ArrayList<>();
    public static final ArrayList<String> memberImportListJson = new ArrayList<>();
    Dialog dialog;
    private boolean isProcessing = false;
    private String houseHoldId;
    private String moduleId;
    public static int existingNewMemberCount = 0;
    public static int existingRemovedMemberCount = 0;
    public static int existingMigratedMemberCount = 0;
    public static int existingPregnantMemberCount = 0;
    public static int existingImportedMemberCount = 0;


    public HouseHoldFormTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_house_hold_form_type, container, false);
        HouseHoldVisitActivity activity = ((HouseHoldVisitActivity) getActivity());
        resetData();
        initUi(view);
        checkDataFromLocalDb();

        return view;
    }

    /**
     * checking data exist or not for particular hh
     */
    private void checkDataFromLocalDb() {
        List<HHVisitInfoModel> datas = HnppApplication.getHHVisitInfoRepository().getHhVisitInfoByHH(familyId, HnppConstants.EVENT_TYPE.HH_FORM_TYPE);
        for (HHVisitInfoModel model : datas) {
            switch (model.eventType) {
                case HnppConstants.EVENT_TYPE.MEMBER_ADD:
                    existingNewMemberCount = model.infoCount;
                    isValidateNewborn = model.isDone == 1;
                    break;
                case HnppConstants.EVENT_TYPE.REMOVE_MEMBER:
                    existingRemovedMemberCount = model.infoCount;
                    isValidateDeath = model.isDone == 1;
                    break;
                case HnppConstants.EVENT_TYPE.MIGRATE_MEMBER:
                    existingMigratedMemberCount = model.infoCount;
                    isValidateMigration = model.isDone == 1;
                    break;
                case HnppConstants.EVENT_TYPE.PREGNANCY:
                    existingPregnantMemberCount = model.infoCount;
                    isValidatePregReg = model.isDone == 1;
                    break;
                case HnppConstants.EVENT_TYPE.INPORT_MEMBER:
                    existingImportedMemberCount = model.infoCount;
                    isValidateMemberImport = model.isDone == 1;
                    break;
                case HnppConstants.EVENT_TYPE.HOME_VISIT:
                    isValidateHhVisit = model.isDone == 1;
                    break;
            }
        }
        updateUi();
    }

    /**
     * reset all list
     */
    private void resetData() {
        memberListJson.clear();
        removedMemberListJson.clear();
        migratedMemberListJson.clear();
        pregancyMemberListJson.clear();
        memberListJson.clear();
        memberImportListJson.clear();

        existingNewMemberCount = 0;
        existingRemovedMemberCount = 0;
        existingMigratedMemberCount = 0;
        existingPregnantMemberCount = 0;
        existingImportedMemberCount = 0;
        isValidateHhVisit = false;

    }

    /*    *//**
     * final validation, only hh visit
     * @return type is boolean
     *//*
    public boolean finalValidation(){
        return isValidateHhVisit;
    }*/

    /**
     * initial validation without hh visit
     *
     * @return type is boolean
     */
    public boolean initalValidation() {
        return isValidateNewborn &&
                isValidateDeath &&
                isValidateMigration &&
                isValidatePregReg &&
                isValidateMemberImport &&
                isValidateHhVisit;
    }

    /**
     * handling form submission result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("ON_ACTIVITY_RESULT", "onActivityResult>>requestCode:" + requestCode + ":resultCode:" + resultCode);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMPORT_MM) {
            String status = data.getStringExtra(PUT_EXTRA_IMPORT_MM);
            if (!TextUtils.isEmpty(status) && status.equalsIgnoreCase("done")) {
                memberImportListJson.add(status);
                updateUi();
                addDataToDb(HnppConstants.EVENT_TYPE.INPORT_MEMBER, memberImportListJson, isValidateMemberImport, existingImportedMemberCount);
            } else {
                memberImportCheckIm.setImageResource(R.drawable.success);
                memberImportCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                addDataToDb(HnppConstants.EVENT_TYPE.INPORT_MEMBER, memberImportListJson, isValidateMemberImport, existingImportedMemberCount);
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == MemberListDialogFragment.REQUEST_CODE) {
            MemberTypeEnum memberTypeEnum = (MemberTypeEnum) data.getSerializableExtra(MemberListDialogFragment.MEMBER_TYPE);
            if (memberTypeEnum == MemberTypeEnum.DEATH) {
                Member member = (Member) data.getParcelableExtra(MemberListDialogFragment.MEMBER);
                String form = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                try {
                    assert member != null;
                    confirmRemove(form, member, memberTypeEnum);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else if (memberTypeEnum == MemberTypeEnum.MIGRATION) {
                Member member = (Member) data.getParcelableExtra(MemberListDialogFragment.MEMBER);
                String form = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                try {
                    assert member != null;
                    confirmRemove(form, member, memberTypeEnum);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else if (memberTypeEnum == MemberTypeEnum.ELCO) {
                String json = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                pregancyMemberListJson.add(json);
                updateUi();

                addDataToDb(HnppConstants.EVENT_TYPE.PREGNANCY, pregancyMemberListJson, isValidatePregReg, existingPregnantMemberCount);
            }

        } else if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {

            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);

                Timber.d(jsonString);

                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)) {
                    String[] sss = HnppJsonFormUtils.getHouseholdIdModuleIdFromForm(form);
                    houseHoldId = sss[0];
                    moduleId = sss[1];
                    ((HouseHoldVisitActivity) getActivity()).getfamilyProfilePresenter().updateHouseIdAndModuleId(houseHoldId);
                    ((HouseHoldVisitActivity) getActivity()).model.updateHouseIdAndModuleId(houseHoldId, moduleId);
                    ((HouseHoldVisitActivity) getActivity()).presenter().updateFamilyRegister(jsonString);
                    ((HouseHoldVisitActivity) getActivity()).presenter().verifyHasPhone();
                } else {
                    if (TextUtils.isEmpty(((HouseHoldVisitActivity) getActivity()).getFamilyBaseEntityId())) {
                        Toast.makeText(getActivity(), "familyBaseEntityId no found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] generatedString;
                    String title;
                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                    String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
                    String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                    if (encounterType.equals(HnppConstants.EventType.CHILD_REGISTRATION)) {
                        generatedString = HnppJsonFormUtils.getValuesFromChildRegistrationForm(form);
                        title = String.format(getActivity().getString(R.string.dialog_confirm_save_child), fullName, generatedString[0], generatedString[2], generatedString[1]);

                    } else {
                        generatedString = HnppJsonFormUtils.getValuesFromRegistrationForm(form);
                        title = String.format(getActivity().getString(R.string.dialog_confirm_save), fullName, generatedString[0], generatedString[2], generatedString[1]);

                    }

                    HnppConstants.showSaveFormConfirmationDialog(getActivity(), title, new OnDialogOptionSelect() {
                        @Override
                        public void onClickYesButton() {

                            try {
                                JSONObject formWithConsent = new JSONObject(jsonString);
                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
                                JSONArray field = jobkect.getJSONArray(FIELDS);
                                HnppJsonFormUtils.addConsent(field, true);
                                processForm(encounterType, formWithConsent.toString());
                                //memberListJson.add(jsonString);
                            } catch (JSONException je) {
                                je.printStackTrace();
                            }
                        }

                        @Override
                        public void onClickNoButton() {
                            try {
                                JSONObject formWithConsent = new JSONObject(jsonString);
                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
                                JSONArray field = jobkect.getJSONArray(FIELDS);
                                HnppJsonFormUtils.addConsent(field, false);
                                processForm(encounterType, formWithConsent.toString());
                                //memberListJson.add(jsonString);
                            } catch (JSONException je) {
                                je.printStackTrace();
                            }
                        }
                    });
                }
                memberListJson.add(jsonString);
                updateUi();
                addDataToDb(HnppConstants.EVENT_TYPE.MEMBER_ADD, memberListJson, isValidateNewborn, existingNewMemberCount);
            } catch (Exception e) {
                Timber.e(e);
            }
            HnppConstants.isViewRefresh = true;
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT) {
            if (isProcessing) return;
            AtomicInteger isSave = new AtomicInteger(2);
            showProgressDialog(R.string.please_wait_message, R.string.please_wait, getActivity());

            isProcessing = true;
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
            String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();

            processAndSaveVisitForm(jsonString, formSubmissionId, visitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer aInteger) {
                            isSave.set(aInteger);
                            Log.v("SAVE_VISIT", "onError>>" + aInteger);
                        }

                        @Override
                        public void onError(Throwable e) {
                            hideProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            Log.d("visitCalledCompleted", "true");
                            if (isSave.get() == 1) {
                                isValidateHhVisit = true;
                                hideProgressDialog();
                                showServiceDoneDialog(1);
                            } else if (isSave.get() == 3) {
                                hideProgressDialog();
                                showServiceDoneDialog(3);
                            } else {
                                hideProgressDialog();
                                isProcessing = false;
                                showServiceDoneDialog(2);
                            }
                            updateUi();
                            addDataToDb(HnppConstants.EVENT_TYPE.HOME_VISIT, new ArrayList<>(), isValidateHhVisit, 1);
                        }
                    });

        }
    }

    private void addDataToDb(String eventType, ArrayList<String> data, boolean isValid, int existingCount) {
        HHVisitInfoModel hhVisitInfoModel = new HHVisitInfoModel();
        hhVisitInfoModel.pageEventType = HnppConstants.EVENT_TYPE.HH_FORM_TYPE;
        hhVisitInfoModel.eventType = eventType;
        hhVisitInfoModel.hhBaseEntityId = familyId;
        hhVisitInfoModel.infoCount = data.size() + existingCount;
        hhVisitInfoModel.isDone = isValid ? 1 : 0;
        HnppApplication.getHHVisitInfoRepository().addOrUpdateHHDataType(hhVisitInfoModel);
    }

    /**
     * member remove confirmation
     *
     * @param formStr        // from string
     * @param currentMember  // current member object
     * @param memberTypeEnum // member type
     * @throws JSONException
     */
    public void confirmRemove(final String formStr, Member currentMember, MemberTypeEnum memberTypeEnum) throws JSONException {
        JSONObject form = new JSONObject(formStr);
        String memberName = currentMember.getName();
        if (StringUtils.isNotBlank(memberName) && getFragmentManager() != null) {
            String title = "";
            JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
            JSONObject removeReasonObj = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, "remove_reason");
            try {
                String value = removeReasonObj.getString(CoreJsonFormUtils.VALUE);
                if (value.equalsIgnoreCase("মৃত্যু নিবন্ধন")) {
                    title = String.format(getString(R.string.confirm_remove_text), memberName);
                } else if (value.equalsIgnoreCase("স্থানান্তর")) {
                    title = String.format(getString(R.string.confirm_migrate_text), memberName);
                } else {
                    title = String.format(getString(R.string.confirm_other_text), memberName);
                }
            } catch (Exception e) {

            }
           /* FamilyRemoveMemberConfirmDialog dialog = FamilyRemoveMemberConfirmDialog.newInstance(title);
            dialog.show(this.getSupportFragmentManager(), FamilyRemoveMemberFragment.DIALOG_TAG);*/

            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.family_remove_member_confrim_dialog_fragment, null);
            alertDialog.setView(dialogView);
            CustomFontTextView remove = dialogView.findViewById(R.id.remove);
            CustomFontTextView cancel = dialogView.findViewById(R.id.cancel);

            ((TextView) dialogView.findViewById(R.id.message)).setText(title);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //onUpdateMemberList.update(false);
                    alertDialog.dismiss();
                }
            });

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                        type = HnppJsonFormUtils.getEncounterType(type);
                        Map<String, String> jsonStrings = new HashMap<>();
                        jsonStrings.put("First", form.toString());
                        String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                        String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                        Visit visit = HnppJsonFormUtils.saveVisit(false, false, false, "", currentMember.getBaseEntityId(), type, jsonStrings, "", formSubmissionId, visitId);
                        if (visit != null) {
                            HnppHomeVisitIntentService.processVisits();
                            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                        }

                        if (memberTypeEnum == MemberTypeEnum.DEATH) {
                            removedMemberListJson.add(formStr);
                            addDataToDb(HnppConstants.EVENT_TYPE.REMOVE_MEMBER, removedMemberListJson, isValidateDeath, existingRemovedMemberCount);
                        } else if (memberTypeEnum.name().equals(MemberTypeEnum.MIGRATION.name())) {
                            migratedMemberListJson.add(formStr);
                            addDataToDb(HnppConstants.EVENT_TYPE.MIGRATE_MEMBER, migratedMemberListJson, isValidateMigration, existingMigratedMemberCount);
                        }

                        ((HouseHoldVisitActivity) getActivity()).onUpdateMemberList.update(true);
                        updateUi();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();

        }
    }

    /**
     * proccess and save visit
     *
     * @param jsonString       // json string
     * @param formSubmissionId // form submision id
     * @param visitId          // visit id
     * @return
     */
    private Observable<Integer> processAndSaveVisitForm(String jsonString, String formSubmissionId, String visitId) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(((HouseHoldVisitActivity) getActivity()).getFamilyBaseEntityId())) {
                e.onNext(2);
            }
            Map<String, String> jsonStrings = new HashMap<>();
            //jsonStrings.put("First",jsonString);
            try {
                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                jsonStrings.put("First", form.toString());

                String type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);

                Visit visit = HnppJsonFormUtils.saveVisit(false, false, false, "", ((HouseHoldVisitActivity) getActivity()).getFamilyBaseEntityId(), type, jsonStrings, "", formSubmissionId, visitId);
                if (visit != null && !visit.getVisitId().equals("0")) {
                    HnppHomeVisitIntentService.processVisits();
                    FormParser.processVisitLog(visit);
                    //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                    e.onNext(1);
                    e.onComplete();
                } else if (visit != null && visit.getVisitId().equals("0")) {
                    e.onNext(3);
                    e.onComplete();
                } else {
                    e.onNext(2);
                    e.onComplete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(2);
                e.onComplete();
            }

        });
    }


    /**
     * service done dialog
     *
     * @param isSuccess // status
     */
    private void showServiceDoneDialog(Integer isSuccess) {
        if (dialog != null) return;
        dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess == 1 ? "সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে" : isSuccess == 3 ? "সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে" : "সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        android.widget.Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                isProcessing = false;
                //if(isSuccess){

                  /*  handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onActivityResult(0,0,null);
                            HnppConstants.isViewRefresh = true;
                           // presenter().refreshProfileView();
                        }
                    },2000);
                    // }*/

            }
        });
        dialog.show();

    }

    /**
     * process form
     *
     * @param encounter_type
     * @param jsonString
     */
    private void processForm(String encounter_type, String jsonString) {
        if (encounter_type.equals(CoreConstants.EventType.CHILD_REGISTRATION)) {

            ((HouseHoldVisitActivity) getActivity()).presenter().saveChildForm(jsonString, false);

        } else if (encounter_type.equals(Utils.metadata().familyMemberRegister.registerEventType)) {

            String careGiver = ((HouseHoldVisitActivity) getActivity()).presenter().saveChwFamilyMember(jsonString);
            if (TextUtils.isEmpty(careGiver) || TextUtils.isEmpty(((HouseHoldVisitActivity) getActivity()).getFamilyBaseEntityId())) {
                Toast.makeText(getActivity(), getString(R.string.address_not_found), Toast.LENGTH_LONG).show();
                return;
            }
            if (((HouseHoldVisitActivity) getActivity()).presenter().updatePrimaryCareGiver(getActivity().getApplicationContext(), jsonString, ((HouseHoldVisitActivity) getActivity()).getFamilyBaseEntityId(), careGiver)) {
                ((HouseHoldVisitActivity) getActivity()).setPrimaryCaregiver(careGiver);
                ((HouseHoldVisitActivity) getActivity()).refreshPresenter();
                ((HouseHoldVisitActivity) getActivity()).refreshMemberFragment(careGiver, null);
            }

            ((HouseHoldVisitActivity) getActivity()).presenter().verifyHasPhone();
        }
    }

    /**
     * all view initialization of this fragment
     *
     * @param view
     */
    private void initUi(View view) {
        initializeMemberPresenter();

        newBornLay = view.findViewById(R.id.newborn_lay);
        deathInfoLay = view.findViewById(R.id.death_info_lay);
        migrationInfoLay = view.findViewById(R.id.migration_info_lay);
        pregnancyRegLay = view.findViewById(R.id.pregnancy_lay);
        hhUpdateLay = view.findViewById(R.id.hh_info_update_lay);
        memberImportLay = view.findViewById(R.id.member_migration_lay);

        noNewBornBt = view.findViewById(R.id.no_new_born_bt);
        noDeathBt = view.findViewById(R.id.dead_info_bt);
        noMigrationBt = view.findViewById(R.id.migration_info_bt);
        noPregnancyBt = view.findViewById(R.id.pregnancy_bt);
        noMemberImportBtn = view.findViewById(R.id.member_migration_info_bt);
        newBornCheckIm = view.findViewById(R.id.newborn_check_im);
        deathCheckIm = view.findViewById(R.id.death_info_im);
        migrationCheckIm = view.findViewById(R.id.migration_info_im);
        pregnancyCheckIm = view.findViewById(R.id.pregnancy_check_im);
        hh_info_CheckIm = view.findViewById(R.id.hh_info_check_im);
        memberImportCheckIm = view.findViewById(R.id.member_import_info_im);

        newBornCountTv = view.findViewById(R.id.new_born_count_tv);
        deathCountTv = view.findViewById(R.id.death_count_tv);
        migrationCountTv = view.findViewById(R.id.migration_count_tv);
        pregnancyCountTv = view.findViewById(R.id.pregnancy_count_tv);
        memberImportCountTV = view.findViewById(R.id.member_import_count_tv);
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
                    addDataToDb(HnppConstants.EVENT_TYPE.MEMBER_ADD, memberListJson, isValidateNewborn, existingNewMemberCount);
                    return;
                }

                if (memberListJson.size() > 0 || existingNewMemberCount > 0) {
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
                    addDataToDb(HnppConstants.EVENT_TYPE.REMOVE_MEMBER, removedMemberListJson, isValidateDeath, existingRemovedMemberCount);
                    return;
                }

                if (removedMemberListJson.size() > 0 || existingRemovedMemberCount > 0) {
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
                    addDataToDb(HnppConstants.EVENT_TYPE.MIGRATE_MEMBER, migratedMemberListJson, isValidateMigration, existingMigratedMemberCount);
                    return;
                }

                if (migratedMemberListJson.size() > 0 || existingMigratedMemberCount > 0) {
                    showMemberListDialog(MemberTypeEnum.MIGRATION);
                }
            }
        });
        memberImportLay.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                HouseHoldInfo houseHoldInfo = HnppDBUtils.getHouseHoldInfo(familyId);
                MigrationFilterSearchActivity.startMigrationFilterActivity(getActivity(), HnppConstants.MIGRATION_TYPE.IMPORT_MM.name(), REQUEST_CODE_IMPORT_MM, familyId, houseHoldInfo.getHouseHoldHeadId());

            }
        });
        noMemberImportBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if ((view.getTag()) == null) {
                    memberImportCheckIm.setImageResource(R.drawable.success);
                    memberImportCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    memberImportLay.setClickable(true);
                    isValidateMemberImport = true;
                    addDataToDb(HnppConstants.EVENT_TYPE.INPORT_MEMBER, memberImportListJson, isValidateMemberImport, existingImportedMemberCount);

                    return;
                }

                if (memberImportListJson.size() > 0 || existingImportedMemberCount > 0) {
                    HouseHoldInfo houseHoldInfo = HnppDBUtils.getHouseHoldInfo(familyId);

                    MigrationFilterSearchActivity.startMigrationFilterActivity(getActivity(), HnppConstants.MIGRATION_TYPE.IMPORT_MM.name(), REQUEST_CODE_IMPORT_MM, familyId, houseHoldInfo.getHouseHoldHeadId());
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
                    addDataToDb(HnppConstants.EVENT_TYPE.PREGNANCY, pregancyMemberListJson, isValidatePregReg, existingPregnantMemberCount);
                    return;
                }

                if (pregancyMemberListJson.size() > 0 || existingPregnantMemberCount > 0) {
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
                openHHVisit(familyId);
            }
        });

    }


    /**
     * member list dialog
     *
     * @param memberTypeEnum // member type
     */
    private void showMemberListDialog(MemberTypeEnum memberTypeEnum) {
        memberHistoryPresenter.fetchMemberList(memberTypeEnum);
        ArrayList<Member> memberArrayList = memberHistoryPresenter.getMemberList();
        MemberListDialogFragment memberListDialogFragment = MemberListDialogFragment.newInstance();
        memberListDialogFragment.setContext(getActivity());
        memberListDialogFragment.setData(memberArrayList, memberTypeEnum);
        memberListDialogFragment.show(getActivity().getFragmentManager(), MemberListDialogFragment.DIALOG_TAG);
    }

    @Override
    public void onResume() {
        super.onResume();

        //updateUi();

    }


    /**
     * update ui for specific layout
     */
    private void updateUi() {
        //newborn component handling
        if (memberListJson.size() > 0 || existingNewMemberCount > 0) {
            newBornCountTv.setVisibility(View.VISIBLE);
            newBornCountTv.setText(String.format(new Locale("bn"), "%d%s", memberListJson.size() + existingNewMemberCount, getActivity().getString(R.string.record_added)));
            noNewBornBt.setText(R.string.add_more_member);
            noNewBornBt.setTag(true);
            newBornCheckIm.setImageResource(R.drawable.success);
            newBornCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            newBornLay.setClickable(false);
            isValidateNewborn = true;
        } else {
            if (isValidateNewborn) {
                newBornCheckIm.setImageResource(R.drawable.success);
                newBornCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
            }
            newBornCountTv.setVisibility(View.GONE);
        }

        //death component handling
        if (removedMemberListJson.size() > 0 || existingRemovedMemberCount > 0) {
            deathCountTv.setVisibility(View.VISIBLE);
            deathCountTv.setText(String.format(new Locale("bn"), "%d%s", removedMemberListJson.size() + existingRemovedMemberCount, getActivity().getString(R.string.death_added)));
            noDeathBt.setText(R.string.add_more_member);
            noDeathBt.setTag(true);
            deathCheckIm.setImageResource(R.drawable.success);
            deathCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            deathInfoLay.setClickable(false);
            isValidateDeath = true;
        } else {
            if (isValidateDeath) {
                deathCheckIm.setImageResource(R.drawable.success);
                deathCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
            }
            deathCountTv.setVisibility(View.GONE);
        }

        //migration component handling
        if (migratedMemberListJson.size() > 0 || existingMigratedMemberCount > 0) {
            migrationCountTv.setVisibility(View.VISIBLE);
            migrationCountTv.setText(String.format(new Locale("bn"), "%d%s", migratedMemberListJson.size() + existingMigratedMemberCount, getActivity().getString(R.string.migration_added)));
            noMigrationBt.setText(R.string.add_more_member);
            noMigrationBt.setTag(true);
            migrationCheckIm.setImageResource(R.drawable.success);
            migrationCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            migrationInfoLay.setClickable(false);
            isValidateMigration = true;
        } else {
            if (isValidateMigration) {
                migrationCheckIm.setImageResource(R.drawable.success);
                migrationCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
            }
            migrationCountTv.setVisibility(View.GONE);
        }

        //pregnancy reg component handling
        if (pregancyMemberListJson.size() > 0 || existingPregnantMemberCount > 0) {
            pregnancyCountTv.setVisibility(View.VISIBLE);
            pregnancyCountTv.setText(String.format(new Locale("bn"), "%d%s", pregancyMemberListJson.size() + existingPregnantMemberCount, getActivity().getString(R.string.pregnancy_added)));
            noPregnancyBt.setText(R.string.add_more_member);
            noPregnancyBt.setTag(true);
            pregnancyCheckIm.setImageResource(R.drawable.success);
            pregnancyCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            pregnancyRegLay.setClickable(false);
            isValidatePregReg = true;
        } else {
            if (isValidatePregReg) {
                pregnancyCheckIm.setImageResource(R.drawable.success);
                pregnancyCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
            }
            pregnancyCountTv.setVisibility(View.GONE);
        }
        //member import
        if (memberImportListJson.size() > 0 || existingImportedMemberCount > 0) {
            memberImportCountTV.setVisibility(View.VISIBLE);
            memberImportCountTV.setText(String.format(new Locale("bn"), "%d%s", memberImportListJson.size() + existingImportedMemberCount, getActivity().getString(R.string.member_added)));
            noMemberImportBtn.setText(R.string.add_more_member);
            noMemberImportBtn.setTag(true);
            memberImportCheckIm.setImageResource(R.drawable.success);
            memberImportCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            memberImportLay.setClickable(false);
            isValidateMemberImport = true;
        } else {
            if (isValidateMemberImport) {
                memberImportCheckIm.setImageResource(R.drawable.success);
                memberImportCheckIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
            }
            memberImportCountTV.setVisibility(View.GONE);
        }

        if (isValidateHhVisit) {
            hhUpdateLay.setClickable(false);
            hh_info_CheckIm.setImageResource(R.drawable.success);
            hh_info_CheckIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
        } else {
            hhUpdateLay.setClickable(true);
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
        memberHistoryPresenter = new MemberListPresenter(this, familyId);
    }

    @Override
    public MemberListContract.Presenter getPresenter() {
        return null;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    /**
     * open hh visit form
     *
     * @param familyBaseEntityId // family base entity id
     */
    private void openHHVisit(String familyBaseEntityId) {
        HnppConstants.getGPSLocation(((CoreFamilyProfileActivity) getActivity()), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    Map<String, String> hhByBaseEntityId = HnppDBUtils.getDetails(familyBaseEntityId, "ec_family");
                    JSONObject jsonForm = FormUtils.getInstance(getActivity().getApplicationContext()).getFormJson(HnppConstants.JSON_FORMS.HOME_VISIT_FAMILY);
                    HnppJsonFormUtils.setEncounterDateTime(jsonForm);

                    HnppJsonFormUtils.updateHhVisitForm(jsonForm, hhByBaseEntityId);
                    ArrayList<String[]> memberList = HnppDBUtils.getAllMembersInHouseHold(familyBaseEntityId);
                    HnppJsonFormUtils.updateFormWithAllMemberName(jsonForm, memberList);
                    HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
                    startHHFormActivity(jsonForm, familyBaseEntityId, REQUEST_HOME_VISIT);

                } catch (Exception e) {
                    e.printStackTrace();
                    hideProgressDialog();
                }
            }
        });

    }

    public void startHHFormActivity(JSONObject jsonForm, String familyBaseEntityId, int requestCode) {
        if (TextUtils.isEmpty(familyBaseEntityId)) {
            Toast.makeText(getActivity(), "BaseEntityId showing empty", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            jsonForm.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, familyBaseEntityId);
            Intent intent;
            intent = new Intent(getActivity(), org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            if (!HnppConstants.isReleaseBuild()) {
                form.setActionBarBackground(R.color.test_app_color);

            } else {
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            if (HnppConstants.isPALogin()) {
                form.setHideSaveLabel(true);
                form.setSaveLabel("");
            }

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, requestCode);
            }
        } catch (Exception e) {

        }
    }
}