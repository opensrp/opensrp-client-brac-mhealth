package org.smartregister.brac.hnpp.fragment;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeFormNameMapping;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.simprints.libsimprints.Tier;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppAncJsonFormActivity;
import org.smartregister.brac.hnpp.activity.HnppAncRegisterActivity;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.activity.HnppHomeVisitActivity;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.adapter.HnppMemberProfileDueAdapter;
import org.smartregister.brac.hnpp.adapter.HouseHoldMemberProfileDueAdapter;
import org.smartregister.brac.hnpp.contract.HnppMemberProfileContract;
import org.smartregister.brac.hnpp.interactor.HnppMemberProfileInteractor;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnEachMemberDueValidate;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.model.Survey;
import org.smartregister.brac.hnpp.presenter.HnppMemberProfilePresenter;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.brac.hnpp.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.Constants;
import org.smartregister.simprint.SimPrintsConstantHelper;
import org.smartregister.simprint.SimPrintsVerification;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HouseHoldMemberDueFragment extends Fragment implements View.OnClickListener, HnppMemberProfileContract.View {
    public static String TAG = "HouseHoldMemberDueFragment";
    public static final int TAG_OPEN_ANC1 = 101;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_OPEN_CORONA = 88888;
    public static final int TAG_OPEN_ANC_REGISTRATION = 555;
    private static final int TAG_PREGNANT_WOMAN_DIETARY_DIVERSITY = 556;
    public static final int REQUEST_HOME_VISIT = 5555;


    private String baseEntityId;
    private String familyBaseEntityId;
    private LinearLayout otherServiceView;
    private ProgressBar loadingProgressBar;
    private CommonPersonObjectClient commonPersonObjectClient;
    private Handler handler;
    private boolean isStart = true;
    Activity mActivity;
    private HnppMemberProfilePresenter presenter;
    private RecyclerView dueRecyclerView;
    boolean isComesFromIdentity;
    private boolean isProcessingHV = false;
    private boolean isProcessingANCVisit = false;
    private ArrayList<MemberProfileDueData> serviceList = new ArrayList<>();
    HouseHoldMemberProfileDueAdapter adapter;

    public int validate(){
        if(listValidation() == 1){
            ((HouseHoldVisitActivity) getActivity()).onEachMemberDueValidate.validate(1,currentMemberPosition);
            return 1;
        }else if(listValidation() == 2){
            Toast.makeText(getActivity(),"Invalid",Toast.LENGTH_SHORT).show();
            return 2;
        }else {
            return 3;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    public static HouseHoldMemberDueFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        HouseHoldMemberDueFragment fragment = new HouseHoldMemberDueFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !isStart) {
            //addStaticView();
            fetchData();
        }
    }

    public void updateStaticView() {
        // if(FormApplicability.isDueAnyForm(baseEntityId,eventType)){

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // addStaticView();
                fetchData();

            }
        }, 500);
//        }else{
//           if(otherServiceView!=null && anc1View !=null) otherServiceView.removeView(anc1View);
//        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_profile_due, null);
        // otherServiceView = view.findViewById(R.id.other_option);
        dueRecyclerView = view.findViewById(R.id.due_recycler_view);
        dueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadingProgressBar = view.findViewById(R.id.client_list_progress);
        isStart = false;

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new HnppMemberProfilePresenter(this);
        baseEntityId = getArguments().getString(Constants.INTENT_KEY.BASE_ENTITY_ID);
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        currentMemberPosition = getArguments().getInt(HnppConstants.POSITION);
        handler = new Handler();
        //addStaticView();
        fetchData();
    }

    private void fetchData() {
        showProgressBar();
        presenter.fetchDataForHh(commonPersonObjectClient, baseEntityId);
    }

    private int currentPosition = -1;
    public int currentMemberPosition = -1;
    //private HouseHoldMemberProfileDueAdapter.OnClickAdapter onClickAdapter = (position, content) -> startFormActivity(content);
    private HouseHoldMemberProfileDueAdapter.OnClickAdapter onClickAdapter = new HouseHoldMemberProfileDueAdapter.OnClickAdapter() {
        @Override
        public void onClick(int position, MemberProfileDueData content) {
            currentPosition = position;
            startFormActivity(content);
        }
    };

    private HouseHoldMemberProfileDueAdapter.OnClickAdapter onNoNeedClick = new HouseHoldMemberProfileDueAdapter.OnClickAdapter() {
        @Override
        public void onClick(int position, MemberProfileDueData content) {
            serviceList.get(position).setStatus(2);
            adapter.notifyDataSetChanged();
        }
    };

    private void startFormActivity(MemberProfileDueData content) {
        if (content.getReferralFollowUpModel() instanceof ReferralFollowUpModel) {
            ReferralFollowUpModel referralFollowUpModel = (ReferralFollowUpModel) content.getReferralFollowUpModel();
            openReferealFollowUp(referralFollowUpModel);
            return;
        }
        Integer tag = (Integer) content.getType();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_CORONA:
                    openCoronaIndividualForm();
                    break;
                case TAG_OPEN_ANC_REGISTRATION:
                    startAncRegister();
                    break;
                case TAG_OPEN_REFEREAL:
                    openRefereal();
                    break;

                case TAG_PREGNANT_WOMAN_DIETARY_DIVERSITY:
                    openWomanDietaryDiversity(content.from);
                    break;
                case TAG_OPEN_ANC1:
                    String eventType = (String) content.getEventType();
                    if (!eventType.equals(HnppConstants.EVENT_TYPE.ELCO)
                            && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)
                            && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)
                            && FormApplicability.isFirstTimeAnc(baseEntityId)) {
                        openHomeVisitForm();
                    } else {
                        openHomeVisitSingleForm(eventTypeFormNameMapping.get(eventType));
                    }

                    break;
            }
        }
    }

    private ReferralFollowUpModel referralFollowUpModel;

    public void openReferealFollowUp(ReferralFollowUpModel refFollowModel) {
        this.referralFollowUpModel = refFollowModel;
        HnppConstants.getGPSLocation(((HouseHoldVisitActivity) getActivity()), new OnPostDataWithGps() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPost(double latitude, double longitude) {
                if (TextUtils.isEmpty(baseEntityId)) {
                    Toast.makeText(getActivity(), "baseentity id null", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    HnppConstants.appendLog("SAVE_VISIT", "openREFERREL_FOLLOWUPForm>>>baseEntityId:" + baseEntityId);

                    JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
                    jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
                    try {
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
                    } catch (Exception e) {

                    }
                    HnppJsonFormUtils.addReferrelReasonPlaceField(jsonForm, referralFollowUpModel.getReferralReason(), referralFollowUpModel.getReferralPlace());
                    Intent intent;
                    intent = new Intent(getActivity(), HnppAncJsonFormActivity.class);
                    intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

                    Form form = new Form();
                    form.setWizard(false);
                    if (!HnppConstants.isReleaseBuild()) {
                        form.setActionBarBackground(R.color.test_app_color);
                    } else {
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
                    referralFollowUpModel = null;
                    getActivity().startActivityForResult(intent, REQUEST_HOME_VISIT);

                } catch (Exception e) {

                }
            }
        });

    }

    public void openCoronaIndividualForm() {
        HnppConstants.getGPSLocation(((HouseHoldVisitActivity) getActivity()), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                Intent intent = new Intent(getActivity(), HnppAncJsonFormActivity.class);
                try {
                    HnppConstants.appendLog("SAVE_VISIT", "openCoronaIndividualForm>>>baseEntityId:" + baseEntityId);

                    JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL);


                    Form form = new Form();
                    form.setWizard(false);
                    if (!HnppConstants.isReleaseBuild()) {
                        form.setActionBarBackground(R.color.test_app_color);

                    } else {
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }
                    try {
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
                    getActivity().startActivityForResult(intent, REQUEST_HOME_VISIT);

                } catch (Exception e) {

                }
            }
        });


    }

    public void startAncRegister() {
        HnppConstants.getGPSLocation(((HouseHoldVisitActivity) getActivity()), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                Member member = getArguments().getParcelable(HnppConstants.MEMBER);
                HnppAncRegisterActivity.startHnppAncRegisterActivity(getActivity(),
                        baseEntityId,
                        member.getMobileNo(),
                        HnppConstants.JSON_FORMS.ANC_FORM, null,
                        member.getFamilyBaseEntityId(),
                        member.getFamilyName(),
                        "",
                        latitude, longitude);
            }
        });
    }

    public void openRefereal() {
        Member member = getArguments().getParcelable(HnppConstants.MEMBER);

        if (member.getGender().equalsIgnoreCase("F")) {
            startAnyFormActivity(HnppConstants.JSON_FORMS.WOMEN_REFERRAL, REQUEST_HOME_VISIT, "");
        } else {
            startAnyFormActivity(HnppConstants.JSON_FORMS.MEMBER_REFERRAL, REQUEST_HOME_VISIT, "");

        }
    }

    public void openWomanDietaryDiversity(String from) {
        startAnyFormActivity(HnppConstants.JSON_FORMS.PREGNANT_WOMAN_DIETARY_DIVERSITY, REQUEST_HOME_VISIT, from);
    }

    public void openHomeVisitForm() {
        if (!HnppApplication.getStockRepository().isAvailableStock(CoreConstants.EventType.ANC_HOME_VISIT)) {
            HnppConstants.showOneButtonDialog(getActivity(), getString(R.string.dialog_stock_sell_end), "");
            return;
        }
        HnppConstants.getGPSLocation(((HouseHoldVisitActivity) getActivity()), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppHomeVisitActivity.startMe(getActivity(), new MemberObject(commonPersonObjectClient), false, isComesFromIdentity,
                        false, true, "", latitude, longitude);

            }
        });

    }

    public void openHomeVisitSingleForm(String formName) {
        startAnyFormActivity(formName, REQUEST_HOME_VISIT, "");
    }

    private void getGPSLocation(String formName, int requestCode, String from) {
        HnppConstants.getGPSLocation(((HouseHoldVisitActivity) getActivity()), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                processJsonForm(formName, requestCode, latitude, longitude, from);
            }
        });
    }

    private void processJsonForm(String formName, int requestCode, double latitude, double longitude, String from) {
        try {
            String gender = "", maritalStatus = "";
            List<Map<String, String>> genderMaritalStatus = HnppDBUtils.getGenderMaritalStatus(baseEntityId);
            if (genderMaritalStatus != null && genderMaritalStatus.size() > 0) {
                gender = genderMaritalStatus.get(0).get("gender");
                maritalStatus = genderMaritalStatus.get(0).get("marital_status");
                commonPersonObjectClient.getColumnmaps().put("gender", gender);
                commonPersonObjectClient.getColumnmaps().put("marital_status", maritalStatus);
            }
            HnppConstants.appendLog("SAVE_VISIT", "processJsonForm>>>formName:" + formName);

            JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(formName);
            HnppJsonFormUtils.addEDDField(formName, jsonForm, baseEntityId);
            try {
                HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                HnppJsonFormUtils.addAddToStockValue(jsonForm);
            } catch (Exception e) {

            }
            jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
            Intent intent;
            if (formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.GIRL_PACKAGE)) {
                HnppJsonFormUtils.addMaritalStatus(jsonForm, maritalStatus);
            } else if (formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM) ||
                    formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM) ||
                    formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM)) {
                HnppJsonFormUtils.addLastAnc(jsonForm, baseEntityId, false);
            } else if (formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM) ||
                    formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM_AFTER_48_HOUR)
                    || formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM_BEFORE_48_HOUR)) {
                HnppJsonFormUtils.addLastPnc(jsonForm, baseEntityId, false);
                int pncDay = FormApplicability.getDayPassPregnancyOutcome(baseEntityId);
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm, "pnc_day_passed", String.valueOf(pncDay));
            }
            if (formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PREGNANT_WOMAN_DIETARY_DIVERSITY)) {
                if (from != null && from.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)) {
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm, "is_valid_lmp", "true");
                } else {
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm, "is_valid_lmp", "false");
                }

            }
            if (formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.BLOOD_TEST)) {
                if (gender.equalsIgnoreCase("F")) {
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm, "is_women", "true");
                }
            }
            if (formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.NCD_PACKAGE)) {
                HnppJsonFormUtils.addNcdSugerPressure(baseEntityId, jsonForm);
            }

//           if(formName.contains("anc"))
            HnppVisitLogRepository visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
            String height = visitLogRepository.getHeight(baseEntityId);
            if (!TextUtils.isEmpty(height)) {
                JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                updateFormField(jsonArray, "height", height);
            }

            intent = new Intent(getActivity(), HnppAncJsonFormActivity.class);
//           else
//               intent = new Intent(this, org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            if (!HnppConstants.isReleaseBuild()) {
                form.setActionBarBackground(R.color.test_app_color);

            } else {
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            getActivity().startActivityForResult(intent, requestCode);

        } catch (Exception e) {

        }
    }

    private void updateFormField(JSONArray formFieldArrays, String formFeildKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFeildKey);
            if (formObject != null) {
                try {
                    formObject.remove(org.smartregister.util.JsonFormUtils.VALUE);
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startAnyFormActivity(String formName, int requestCode, String from) {
        if (!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))) {
            HnppConstants.showOneButtonDialog(getActivity(), getString(R.string.dialog_stock_sell_end), "");
            return;
        }
        getGPSLocation(formName, requestCode, from);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(TextUtils.isEmpty(baseEntityId)){
            Toast.makeText(getActivity(),"BaseEntityId should not be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if(resultCode == Activity.RESULT_OK){
            HnppConstants.isViewRefresh = true;

        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){

            if(isProcessingHV) return;
            Log.d("calledMultiVisit","true");

            isProcessingHV = true;
            AtomicInteger isSave = new AtomicInteger(2); /// 1-> Success / 2-> Regular error  3-> Already submitted visit error
            ((HouseHoldVisitActivity) getActivity()).showProgressDialog(R.string.please_wait_message);
            String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
            String visitId = JsonFormUtils.generateRandomUUIDString();
            HnppConstants.appendLog("SAVE_VISIT","isProcessingHV>>>baseEntityId:"+baseEntityId+":formSubmissionId:"+formSubmissionId);

            processVisitFormAndSave(jsonString,formSubmissionId,visitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer aInteger) {
                            isSave.set(aInteger);
                            Log.d("visitCalledOnnext","true");
                        }

                        @Override
                        public void onError(Throwable e) {

                            ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            Log.d("visitCalledCompleted","true");
                            if(isSave.get() == 1){
                                ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
                                showServiceDoneDialog(1);
                                if(currentPosition!=-1){
                                    serviceList.get(currentPosition).setStatus(1);
                                    adapter.notifyDataSetChanged();
                                }
                            }else if(isSave.get() == 3){
                                ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
                                showServiceDoneDialog(3);
                            }else {
                                ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
                                showServiceDoneDialog(2);
                            }
                        }
                    });
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT){
            if(isProcessingANCVisit) return;
            AtomicBoolean isSave = new AtomicBoolean(false);
            ((HouseHoldVisitActivity) getActivity()).showProgressDialog(R.string.please_wait_message);

            isProcessingANCVisit = true;
            processVisits()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            isSave.set(aBoolean);
                        }

                        @Override
                        public void onError(Throwable e) {
                            ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            if(isSave.get()){
                                ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
                                showServiceDoneDialog(1);
                                if(currentPosition!=-1){
                                    serviceList.get(currentPosition).setStatus(1);
                                    adapter.notifyDataSetChanged();
                                }
                            }else {
                                ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
                                //showServiceDoneDialog(false);
                            }
                        }
                    });

        }
        else if(resultCode == Activity.RESULT_OK && requestCode == org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON){
            String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
            try{
                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                if (form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.family.util.Utils.metadata().familyMemberRegister.updateEventType)) {
                    String[] generatedString;
                    String title;
                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                    String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
                    generatedString = HnppJsonFormUtils.getValuesFromRegistrationForm(form);
                    title = String.format(getString(R.string.dialog_confirm_save),fullName,generatedString[0],generatedString[2],generatedString[1]);

                    HnppConstants.showSaveFormConfirmationDialog(getActivity(), title, new OnDialogOptionSelect() {
                        @Override
                        public void onClickYesButton() {

                            try{
                                JSONObject formWithConsent = new JSONObject(jsonString);
                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
                                JSONArray field = jobkect.getJSONArray(FIELDS);
                                HnppJsonFormUtils.addConsent(field,true);
                                ((HouseHoldVisitActivity) getActivity()).getfamilyProfilePresenter().updateFamilyMember(formWithConsent.toString());
                                if(currentPosition!=-1){
                                    serviceList.get(currentPosition).setStatus(1);
                                    adapter.notifyDataSetChanged();
                                }
                            }catch (JSONException je){
                                je.printStackTrace();
                            }
                        }

                        @Override
                        public void onClickNoButton() {
                            try{
                                JSONObject formWithConsent = new JSONObject(jsonString);
                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
                                JSONArray field = jobkect.getJSONArray(FIELDS);
                                HnppJsonFormUtils.addConsent(field,false);
                                ((HouseHoldVisitActivity) getActivity()).getfamilyProfilePresenter().updateFamilyMember(formWithConsent.toString());
                                if(currentPosition!=-1){
                                    serviceList.get(currentPosition).setStatus(1);
                                    adapter.notifyDataSetChanged();
                                }
                            }catch (JSONException je){
                                je.printStackTrace();
                            }
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        else if(resultCode == Activity.RESULT_OK && requestCode == HnppConstants.SURVEY_KEY.MM_SURVEY_REQUEST_CODE){
            if(processSurveyResponse(data)){
                Toast.makeText(getActivity(),"Survey done",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity(),"Fail to Survey",Toast.LENGTH_SHORT).show();
            }


        }

    }

    private Observable<Boolean> processVisits(){
        return Observable.create(e->{
            try{
                HnppHomeVisitIntentService.processVisits();
                VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                //return true;
                e.onNext(true);
                e.onComplete();
            }catch (Exception ex){
                //return false;
                e.onNext(false);
                e.onComplete();
            }
        });

    }

    private Observable<Integer> processVisitFormAndSave(String jsonString, String formSubmissionId, String visitId){

        return  Observable.create(e->{
                    if(TextUtils.isEmpty(baseEntityId)) e.onNext(2);
                    try {
                        JSONObject form = new JSONObject(jsonString);
                        HnppJsonFormUtils.setEncounterDateTime(form);

                        Log.v("DATEEEE",""+form.getJSONObject("metadata").getJSONObject("today").getString("value"));

                        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                        type = HnppJsonFormUtils.getEncounterType(type);
                        Map<String, String> jsonStrings = new HashMap<>();
                        jsonStrings.put("First",form.toString());
                        HnppConstants.appendLog("SAVE_VISIT","baseEntityId:"+baseEntityId+":formSubmissionId:"+formSubmissionId);

                        Visit visit = HnppJsonFormUtils.saveVisit(isComesFromIdentity,false, true,"", baseEntityId, type, jsonStrings, "",formSubmissionId,visitId);

                        if(visit!=null && !visit.getVisitId().equals("0")){
                            HnppHomeVisitIntentService.processVisits();
                            FormParser.processVisitLog(visit);
                            HnppConstants.appendLog("SAVE_VISIT","processVisitLog done formSubmissionId:"+formSubmissionId+":type:"+type);

                            // return true;
                            e.onNext(1);//success
                            e.onComplete();

                        }else if(visit!=null && visit.getVisitId().equals("0")){
                            e.onNext(3);//already exist
                            e.onComplete();
                        }else{
                            //return false;
                            e.onNext(2);//error
                            e.onComplete();
                        }
                    } catch (Exception ex) {
                        HnppConstants.appendLog("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        Log.d("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        e.onNext(2);//error
                        e.onComplete();
                    }
                    // return false;
                    // e.onNext(false);

                }
        );


    }

    Dialog dialog;
    private void showServiceDoneDialog(Integer isSuccess){
        if(dialog!=null) return;
        dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess==1?"সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে":isSuccess==3?"সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে":"সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                isProcessingHV = false;
                isProcessingANCVisit = false;
                ((HouseHoldVisitActivity) getActivity()).hideProgressDialog();
            }
        });
        dialog.show();
    }

    private boolean processSurveyResponse(Intent data){
        String response = data.getStringExtra(HnppConstants.SURVEY_KEY.DATA);
        Log.v("SURVEY_APP","response processSurveyResponse:"+response);
        try{
            JSONObject jsonObject = new JSONObject(response);
            String form_name = jsonObject.getString("form_name");
            String date_time = jsonObject.getString("date");
            String uuid = jsonObject.getString("uuid");
            Long time_stamp;
            try{
                time_stamp = jsonObject.getLong("time_stamp");
            }catch (Exception e){
                time_stamp = Long.parseLong(jsonObject.getString("time_stamp"));
            }
            String form_id = jsonObject.optString("form_id");

            Survey survey = new Survey();
            survey.formName = form_name;
            survey.formId = form_id;
            survey.uuid = uuid;
            survey.timestamp = time_stamp;
            survey.baseEntityId = baseEntityId;
            survey.dateTime = date_time;
            survey.type = HnppConstants.SURVEY_KEY.MM_TYPE;
            HnppApplication.getSurveyHistoryRepository().addOrUpdate(survey,HnppConstants.SURVEY_KEY.MM_TYPE);
            return true;

        }catch (Exception e){
            e.printStackTrace();

        }
        return false;
    }


    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof ReferralFollowUpModel) {
            openReferealFollowUp(referralFollowUpModel);
            return;
        }
        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_CORONA:
                    openCoronaIndividualForm();
                    break;
                case TAG_OPEN_ANC_REGISTRATION:
                    startAncRegister();
                    break;
                case TAG_OPEN_FAMILY:
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                    activity.openFamilyDueTab();
                    break;
                case TAG_OPEN_REFEREAL:
                    openRefereal();
                    break;
                case TAG_OPEN_ANC1:
                        String eventType = (String) v.getTag(org.smartregister.family.R.id.VIEW_ID);
                        if (!eventType.equals(HnppConstants.EVENT_TYPE.ELCO)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)
                                && FormApplicability.isFirstTimeAnc(baseEntityId)) {
                            openHomeVisitForm();
                        } else {
                            openHomeVisitSingleForm(eventTypeFormNameMapping.get(eventType));
                        }
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        mActivity = null;
    }

    @Override
    public void showProgressBar() {
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        loadingProgressBar.setVisibility(View.GONE);

    }

    @Override
    public void updateView() {
        hideProgressBar();
        serviceList = presenter.getData();
        adapter = new HouseHoldMemberProfileDueAdapter(getActivity(), onClickAdapter,onNoNeedClick);
        adapter.setData(serviceList);
        this.dueRecyclerView.setAdapter(adapter);
        updateOptionMenu(presenter.getLastEventType());
    }

    private void updateOptionMenu(String eventType) {
        if (TextUtils.isEmpty(eventType)) return;
        if (mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HnppFamilyOtherMemberProfileActivity aaa = (HnppFamilyOtherMemberProfileActivity) mActivity;
                    try {
                        aaa.updatePregnancyOutcomeVisible(eventType);
                        aaa.updateAncRegisterVisible(eventType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 500);


        }
    }

    @Override
    public HnppMemberProfileContract.Presenter getPresenter() {
        return presenter;
    }

    public int listValidation(){
        int status = 1;
        int countSucc = 0;
        if(serviceList.size() == 1){
            if(serviceList.get(0).getType() == HnppMemberProfileInteractor.TAG_OPEN_REFEREAL){
               return serviceList.get(0).getStatus();
            }
        }else {
            for(MemberProfileDueData data : serviceList){
                if(data.getType() != HnppMemberProfileInteractor.TAG_OPEN_REFEREAL){
                    if(data.getStatus() == 2){
                        return 2;
                    }

                    if(data.getStatus() < 3){
                        countSucc++;
                    }
                }
            }
        }
        if(countSucc<serviceList.size()-1 && countSucc>0) return 2;
        else if(countSucc == 0) return 3;

        return status;
    }


}