package org.smartregister.brac.hnpp.fragment;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.ChildFollowupActivity;
import org.smartregister.brac.hnpp.activity.ChildGMPActivity;
import org.smartregister.brac.hnpp.activity.ChildVaccinationActivity;
import org.smartregister.brac.hnpp.activity.HnppAncJsonFormActivity;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.interactor.HnppMemberProfileInteractor;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.ChildService;
import org.smartregister.brac.hnpp.model.HHVisitInfoModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.model.MemberProfileDueModel;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.presenter.HnppChildProfileDuePresenter;
import org.smartregister.brac.hnpp.presenter.HnppChildProfilePresenter;
import org.smartregister.brac.hnpp.provider.HnppFamilyDueRegisterProvider;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.adapter.FamilyRecyclerViewCustomAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HouseHoldChildProfileDueFragment extends BaseFamilyProfileDueFragment implements View.OnClickListener {
    public static final String TAG = "HouseHoldChildProfileDueFragment";
    private static final int TAG_OPEN_ANC1 = 101;
    private static final int TAG_OPEN_ANC2 = 102;
    private static final int TAG_OPEN_ANC3 = 103;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_CHILD_FOLLOWUP = 3330;
    private static final int TAG_CHILD_FOLLOWUP_0_3_MONTHS = 3331;
    private static final int TAG_CHILD_FOLLOWUP_3_6_MONTHS = 3332;
    private static final int TAG_CHILD_FOLLOWUP_7_11_MONTHS = 3333;
    private static final int TAG_CHILD_FOLLOWUP_12_18_MONTHS = 3334;
    private static final int TAG_CHILD_FOLLOWUP_19_24_MONTHS = 3335;
    private static final int TAG_CHILD_FOLLOWUP_2_3_YEARS = 3336;
    private static final int TAG_CHILD_FOLLOWUP_3_4_YEARS = 3337;
    private static final int TAG_CHILD_FOLLOWUP_4_5_YEARS = 3338;

    private static final int TAG_CHILD_INFO_EBF12 = 1212;
    private static final int TAG_CHILD_INFO_7_24_months = 1213;
    private static final int TAG_CHILD_INFO_25_months = 1214;
    private static final int TAG_ENC = 333;
    private static final int TAG_CHILD_DUE = 444;
    private static final int TAG_OPEN_CORONA = 88888;
    private static final int TAG_EYE_TEST_BT_CLICK = 4444;
    private static final int TAG_NO_IMMUNIZATION_CLICK = 4446;
    private static final int TAG_CHILD_COUNSELING_BT_CLICK = 4445;
    private static final int TAG_IMMUNIZATION = 3339;
    private static final int TAG_GMP = 3340;


    private int dueCount = 0;
    private View emptyView;
    private String familyName;
    private long dateFamilyCreated;
    private String familyBaseEntityId;
    public String childBaseEntityId;
    private LinearLayout otherServiceView;
    private CommonPersonObjectClient commonPersonObjectClient;
    private boolean isFirstAnc = false;
    private Handler handler;
    private boolean isStart = true;

    public int currentChildPosition = -1;

    public ArrayList<ChildService> serviceList = new ArrayList<>();
    AppExecutors appExecutors = new AppExecutors();
    private View currentView;
    private boolean onClickView = false;


    public static BaseFamilyProfileDueFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        BaseFamilyProfileDueFragment fragment = new HouseHoldChildProfileDueFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.childBaseEntityId = baseEntityId;
    }

    /**
     * child due validation
     * @return status 1-> success, 2-> failed, 3-> no need
     */
    public int validate() {
        if (listValidation() == 1) {
            ((HouseHoldVisitActivity) getActivity()).onEachMemberDueValidate.validate(1, currentChildPosition);
            return 1;
        } else if (listValidation() == 2) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.continue_to_submit_data_msg), Toast.LENGTH_SHORT).show();
            return 2;
        } else {
            return 3;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !isStart) {
            updateStaticView();
        }
    }

    private void updateStaticView() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || getActivity().isFinishing()) return;
                addStaticView();
               /* String dobString = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false);
                Date dob = Utils.dobStringToDate(dobString);
                boolean isImmunizationVisible = FormApplicability.isImmunizationVisible(dob);
                if (isImmunizationVisible) {
                    if (getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity b = (HnppChildProfileActivity) getActivity();
                        b.updateImmunizationData();
                    }
                }*/

                checkDataFromLocalDb();
            }
        }, 500);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView.setBackgroundColor(getResources().getColor(R.color.white));
        rootView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        childBaseEntityId = getArguments().getString(Constants.INTENT_KEY.BASE_ENTITY_ID);
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        currentChildPosition = getArguments().getInt(HnppConstants.POSITION);
    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        familyName = getArguments().getString(Constants.INTENT_KEY.FAMILY_NAME);
        presenter = new HnppChildProfileDuePresenter(this, new MemberProfileDueModel(), null, familyBaseEntityId);
        //TODO need to pass this value as this value using at homevisit rule
        dateFamilyCreated = getArguments().getLong("");

    }

    @Override
    protected boolean isValidFilterForFts(CommonRepository commonRepository) {
        return false;
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //TODO
        Timber.d("setAdvancedSearchFormData");
    }

    @Override
    public void setupViews(View view) {
        try {
            super.setupViews(view);
        } catch (Exception e) {
            HnppApplication.getHNPPInstance().forceLogout();
            return;
        }
        emptyView = view.findViewById(R.id.empty_view);
        otherServiceView = view.findViewById(R.id.other_option);

        isStart = false;

    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        HnppFamilyDueRegisterProvider chwDueRegisterProvider = new HnppFamilyDueRegisterProvider(this.getActivity(), this.commonRepository(), visibleColumns, this.registerActionHandler, this.paginationViewHandler);
        this.clientAdapter = new FamilyRecyclerViewCustomAdapter(null, chwDueRegisterProvider, this.context().commonrepository(this.tablename), Utils.metadata().familyDueRegister.showPagination);
        this.clientAdapter.setCurrentlimit(0);
        this.clientsView.setAdapter(this.clientAdapter);
        this.clientsView.setVisibility(View.GONE);
        if (!onClickView) {
            updateStaticView();
        }
    }

    /**
     * checking data exist or not for particular hh
     */
    private void checkDataFromLocalDb() {
        List<HHVisitInfoModel> datas = HnppApplication.getHHVisitInfoRepository().getMemberDueInfo(familyBaseEntityId,childBaseEntityId, HnppConstants.EVENT_TYPE.HH_MEMBER_DUE);
        for (HHVisitInfoModel data : datas) {
            isExistData(data);
        }
    }

    private void isExistData(HHVisitInfoModel model) {
        for (ChildService member : serviceList) {
            if (member.getEventType().equals(model.eventType)) {
                member.setStatus(model.isDone);
                ImageView checkIm = member.getView().findViewById(R.id.check_im);
                if(model.isDone == 1){
                    //setStatusToList();
                    checkIm.setImageResource(R.drawable.success);
                    checkIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
                    View buttonView = (View)  member.getView().findViewById(R.id.noNeedBt);
                    buttonView.setClickable(false);
                    buttonView.setEnabled(false);

                    member.getView().setClickable(false);
                    member.getView().setEnabled(false);
                }else if(model.isDone == 2){
                    //setNoNeedStatusToList();
                    checkIm.setImageResource(R.drawable.success);
                    checkIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    View buttonView = (View)  member.getView().findViewById(R.id.noNeedBt);
                    buttonView.setClickable(true);
                    buttonView.setEnabled(false);

                    member.getView().setClickable(true);
                    member.getView().setEnabled(true);
                }
                return;
            }
        }
    }

    public void addDataToDb(ChildService member) {
        HHVisitInfoModel hhVisitInfoModel = new HHVisitInfoModel();
        hhVisitInfoModel.pageEventType = HnppConstants.EVENT_TYPE.HH_CHILD_DUE;
        hhVisitInfoModel.eventType = member.getEventType();
        hhVisitInfoModel.hhBaseEntityId = familyBaseEntityId;
        hhVisitInfoModel.memberBaseEntityId = childBaseEntityId;
        hhVisitInfoModel.infoCount = 1;
        hhVisitInfoModel.isDone = member.getStatus();
        HnppApplication.getHHVisitInfoRepository().addOrUpdateHhMemmerData(hhVisitInfoModel);
    }

    @Override
    protected String getMainCondition() {
        return super.getMainCondition();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onViewClicked(View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.patient_column:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                    goToChildProfileActivity(view);
                }
                break;
            case R.id.next_arrow:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NEXT_ARROW) {
                    goToChildProfileActivity(view);
                }
                break;
            default:
                break;
        }
    }

    public void goToChildProfileActivity(View view) {
        if (view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient patient = (CommonPersonObjectClient) view.getTag();
        }

    }

    View encView;

    @SuppressLint("InflateParams")
    public void updateChildDueEntry(int type, String serviceName, String dueDate) {
        if (getActivity() == null || getActivity().isFinishing() || otherServiceView == null || TextUtils.isEmpty(serviceName))
            return;
        serviceName = HnppConstants.immunizationMapping.get(serviceName.toUpperCase());
//       if(handler !=null){
//           handler.postDelayed(new Runnable() {
//               @Override
//               public void run() {
        otherServiceView.setVisibility(View.VISIBLE);
        if (encView != null) otherServiceView.removeView(encView);
        encView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due, null);
        ImageView image1 = encView.findViewById(R.id.image_view);
        TextView name1 = encView.findViewById(R.id.patient_name_age);
        encView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
        image1.setImageResource(R.mipmap.ic_child);
        switch (type) {
            case 1:
                name1.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_due, serviceName, dueDate)));

                break;
            case 2:
                name1.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_overdue, serviceName, dueDate)));
                break;
            case 3:
                name1.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_upcoming, serviceName, dueDate)));
                break;
        }
        encView.setTag(TAG_CHILD_DUE);
        encView.setOnClickListener(HouseHoldChildProfileDueFragment.this);
        otherServiceView.addView(encView);
//               }
//           },500);
        //}

    }

    String eventType = "";
    View childInfo1View, childInfo2View, childInfo3View;

    /**
     * added all service and due
     */
    private void addStaticView() {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (otherServiceView.getVisibility() == View.VISIBLE) {
            otherServiceView.removeAllViews();
        }
        otherServiceView.setVisibility(View.VISIBLE);
        long day = FormApplicability.getDay(commonPersonObjectClient);

        {
            ChildService childService = new ChildService();
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due,null);
            ImageView fImg = view.findViewById(R.id.image_view);
           TextView name =  view.findViewById(R.id.patient_name_age);

            AppCompatButton noNeedButton = view.findViewById(R.id.noNeedBt);
            noNeedButton.setText(getActivity().getString(R.string.no_immunization_info));
            noNeedButton.setVisibility(View.VISIBLE);
            noNeedButton.setTag(TAG_NO_IMMUNIZATION_CLICK);
            noNeedButton.setTag(R.string.child_immunization,view);
            noNeedButton.setOnClickListener(this);

            view.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.VACCINATION));
            name.setText(getActivity().getString(R.string.immunizations));
            view.setTag(TAG_IMMUNIZATION);
            view.setOnClickListener(this);
            otherServiceView.addView(view);

            childService.setTag(TAG_IMMUNIZATION);
            childService.setEventType(HnppConstants.EVENT_TYPE.VACCINATION);
            childService.setView(view);
            serviceList.add(childService);
        }
        {
            ChildService childService = new ChildService();
            @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due,null);
            ImageView fImg = view.findViewById(R.id.image_view);
            TextView name =  view.findViewById(R.id.patient_name_age);
            view.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.GMP));
            name.setText(getActivity().getString(R.string.gmp));
            view.setTag(TAG_GMP);
            view.setOnClickListener(this);
            otherServiceView.addView(view);

            childService.setTag(TAG_GMP);
            childService.setEventType(HnppConstants.EVENT_TYPE.GMP);
            childService.setView(view);
            serviceList.add(childService);
        }

        {
            ChildService childService = new ChildService();
            @SuppressLint("InflateParams") View referelView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView imageReferel = referelView.findViewById(R.id.image_view);
            TextView nameReferel = referelView.findViewById(R.id.patient_name_age);
            referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            imageReferel.setImageResource(R.mipmap.ic_refer);
            nameReferel.setText("রেফারেল");
            referelView.setTag(TAG_OPEN_REFEREAL);
            referelView.setOnClickListener(this);
            otherServiceView.addView(referelView);

            childService.setTag(TAG_OPEN_REFEREAL);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_REFERRAL);
            childService.setView(referelView);
            serviceList.add(childService);
        }
        //if(!isEnc){
        eventType = FormApplicability.isDueChildFollowUp(day);

        if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_0_3_MONTHS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_0_3_MONTHS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_0_3_MONTHS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_0_3_MONTHS);
            childService.setView(followupView);
            serviceList.add(childService);

        } else if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_6_MONTHS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_3_6_MONTHS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_3_6_MONTHS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_6_MONTHS);
            serviceList.add(childService);

        } else if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_7_11_MONTHS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_7_11_MONTHS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_7_11_MONTHS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_7_11_MONTHS);
            childService.setView(followupView);
            serviceList.add(childService);

        } else if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_12_18_MONTHS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_12_18_MONTHS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_12_18_MONTHS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_12_18_MONTHS);
            childService.setView(followupView);
            serviceList.add(childService);

            currentView = followupView;

        } else if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_19_24_MONTHS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_19_24_MONTHS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_19_24_MONTHS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_19_24_MONTHS);
            childService.setView(followupView);
            serviceList.add(childService);
        } else if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_2_3_YEARS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_2_3_YEARS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_2_3_YEARS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_2_3_YEARS);
            childService.setView(followupView);
            serviceList.add(childService);
        } else if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_4_YEARS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_3_4_YEARS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_3_4_YEARS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_4_YEARS);
            childService.setView(followupView);
            serviceList.add(childService);
        } else if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_4_5_YEARS) && FormApplicability.isDueAnyForm(childBaseEntityId, eventType)) {
            ChildService childService = new ChildService();

            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName = followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            followupView.setTag(TAG_CHILD_FOLLOWUP_4_5_YEARS);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);

            childService.setTag(TAG_CHILD_FOLLOWUP_4_5_YEARS);
            childService.setEventType(HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_4_5_YEARS);
            childService.setView(followupView);
            serviceList.add(childService);
        }
           /* if(FormApplicability.isDueAnyForm(childBaseEntityId, HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP)){
                View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
                ImageView fImg = followupView.findViewById(R.id.image_view);
                TextView fName =  followupView.findViewById(R.id.patient_name_age);
                followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
                fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP));
                fName.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP));
                followupView.setTag(TAG_CHILD_FOLLOWUP);
                followupView.setOnClickListener(this);
                otherServiceView.addView(followupView);
            }*/

        //}
     /*   eventType = FormApplicability.isDueChildInfo(day);
        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12) && FormApplicability.isDueChildInfoForm(childBaseEntityId,eventType)){
            childInfo1View = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = childInfo1View.findViewById(R.id.image_view);
            TextView fName =  childInfo1View.findViewById(R.id.patient_name_age);
            childInfo1View.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            childInfo1View.setTag(TAG_CHILD_INFO_EBF12);
            childInfo1View.setOnClickListener(this);
            otherServiceView.addView(childInfo1View);
        }
        else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS) && FormApplicability.isDueChildInfoForm(childBaseEntityId,eventType)){
            childInfo2View = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = childInfo2View.findViewById(R.id.image_view);
            TextView fName =  childInfo2View.findViewById(R.id.patient_name_age);
            childInfo2View.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            childInfo2View.setTag(TAG_CHILD_INFO_7_24_months);
            childInfo2View.setOnClickListener(this);
            otherServiceView.addView(childInfo2View);
        }
        else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS) && FormApplicability.isDueChildInfoForm(childBaseEntityId,eventType)){
            childInfo3View = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = childInfo3View.findViewById(R.id.image_view);
            TextView fName =  childInfo3View.findViewById(R.id.patient_name_age);
            childInfo3View.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(eventType));
            fName.setText(eventTypeMapping.get(eventType));
            childInfo3View.setTag(TAG_CHILD_INFO_25_months);
            childInfo3View.setOnClickListener(this);
            otherServiceView.addView(childInfo3View);
        }
*/
        ArrayList<ReferralFollowUpModel> getList = FormApplicability.getReferralFollowUp(childBaseEntityId);

        for (ReferralFollowUpModel referralFollowUpModel : getList) {
            ChildService childService = new ChildService();
            View referrelFollowUp = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView imgFollowup = referrelFollowUp.findViewById(R.id.image_view);
            TextView nReferel = referrelFollowUp.findViewById(R.id.patient_name_age);
            TextView lastVisitRow = referrelFollowUp.findViewById(R.id.last_visit);
            lastVisitRow.setVisibility(View.VISIBLE);
            referrelFollowUp.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            imgFollowup.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            nReferel.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            lastVisitRow.setText(referralFollowUpModel.getReferralReason());
            referrelFollowUp.setTag(referralFollowUpModel);
            referrelFollowUp.setOnClickListener(this);
            otherServiceView.addView(referrelFollowUp);

            childService.setTag(122);
            childService.setEventType(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP);
            childService.setView(referrelFollowUp);
            serviceList.add(childService);
        }



        if (FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(), HnppConstants.EVENT_TYPE.EYE_TEST)) {
            ChildService childService = new ChildService();
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView img = view.findViewById(R.id.image_view);
            TextView textView = view.findViewById(R.id.patient_name_age);
            view.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            AppCompatButton noNeedButton = view.findViewById(R.id.noNeedBt);
            noNeedButton.setVisibility(View.VISIBLE);
            noNeedButton.setTag(TAG_EYE_TEST_BT_CLICK);
            noNeedButton.setTag(R.string.eye_test_key,view);
            noNeedButton.setOnClickListener(this);

            img.setImageResource(R.drawable.ic_eye);
            textView.setText("চক্ষু পরীক্ষা");
            view.setTag(HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE);
            view.setOnClickListener(this);
            otherServiceView.addView(view);

            childService.setTag(HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE);
            childService.setEventType(HnppConstants.EVENT_TYPE.EYE_TEST);
            childService.setView(view);
            serviceList.add(childService);
        }
       /* if (FormApplicability.isIycfApplicable(day) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(), HnppConstants.EVENT_TYPE.IYCF_PACKAGE)) {
            ChildService childService = new ChildService();
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_hh_member_due, null);
            ImageView img = view.findViewById(R.id.image_view);
            TextView textView = view.findViewById(R.id.patient_name_age);
            AppCompatButton noNeedButton = view.findViewById(R.id.noNeedBt);
            noNeedButton.setVisibility(View.VISIBLE);
            noNeedButton.setTag(R.string.child_counseling_key,view);
            noNeedButton.setOnClickListener(this);

            view.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            img.setImageResource(R.drawable.ic_child);
            textView.setText("শিশু কাউন্সেলিং");
            view.setTag(HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF);
            view.setOnClickListener(this);
            otherServiceView.addView(view);

            childService.setTag(HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF);
            childService.setEventType(HnppConstants.EVENT_TYPE.IYCF_PACKAGE);
            childService.setView(view);
            serviceList.add(childService);
        }*/

    }

    @Override
    public void countExecute() {
        // super.countExecute();
    }

    public void onEmptyRegisterCount(final boolean has_no_records) {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }


    /**
     * opening form by tag
     * @param v view
     */
    @Override
    public void onClick(View v) {
        onClickView = true;
        if (v.getTag() instanceof ReferralFollowUpModel) {
            ReferralFollowUpModel referralFollowUpModel = (ReferralFollowUpModel) v.getTag();
            openReferealFollowUp(referralFollowUpModel);
            currentView = v;
            return;
        }
        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {
                case TAG_ENC:
                    openEnc();
                    break;
                case TAG_CHILD_DUE:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        //activity.openVisitHomeScreen(false);
                    }
                    break;
                case TAG_OPEN_FAMILY:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        // activity.openFamilyDueTab();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    openRefereal();
                    break;

                /*case TAG_CHILD_FOLLOWUP:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openFollowUp();
                    }
                    break;*/

                case TAG_CHILD_FOLLOWUP_0_3_MONTHS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_0_3_MONTHS);
                    break;

                case TAG_CHILD_FOLLOWUP_3_6_MONTHS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_3_6_MONTHS);
                    break;

                case TAG_CHILD_FOLLOWUP_7_11_MONTHS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_7_11_MONTHS);
                    break;


                case TAG_CHILD_FOLLOWUP_12_18_MONTHS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_12_18_MONTHS);
                    break;

                case TAG_CHILD_FOLLOWUP_19_24_MONTHS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_19_24_MONTHS);
                    break;

                case TAG_CHILD_FOLLOWUP_2_3_YEARS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_2_3_YEARS);
                    break;

                case TAG_CHILD_FOLLOWUP_3_4_YEARS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_3_4_YEARS);
                    break;

                case TAG_CHILD_FOLLOWUP_4_5_YEARS:
                    openFollowUpByType(HnppConstants.JSON_FORMS.CHILD_FOLLOW_UP_4_5_YEARS);

                    break;
                case TAG_OPEN_CORONA:
                    openCoronaIndividualForm();
                    break;
                case TAG_CHILD_COUNSELING_BT_CLICK:
                    ImageView checkIm = ((View) v.getTag(R.string.child_counseling_key)).findViewById(R.id.check_im);
                    checkIm.setImageResource(R.drawable.success);
                    checkIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    currentView = ((View) v.getTag(R.string.eye_test_key));
                    setNoNeedStatusToList();
                    return;
                case TAG_EYE_TEST_BT_CLICK:
                    ImageView checkImEyeTest = ((View) v.getTag(R.string.eye_test_key)).findViewById(R.id.check_im);
                    checkImEyeTest.setImageResource(R.drawable.success);
                    checkImEyeTest.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    currentView = ((View) v.getTag(R.string.eye_test_key));
                    setNoNeedStatusToList();
                    return;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF:
                    openServiceForms(HnppConstants.JSON_FORMS.IYCF_PACKAGE);
                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_BLOOD:
                    openServiceForms(HnppConstants.JSON_FORMS.BLOOD_TEST);
                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE:
                    openServiceForms(HnppConstants.JSON_FORMS.EYE_TEST);
                    break;
                case TAG_IMMUNIZATION:
                    startGmpOrImm("im");
                    break;
                case TAG_GMP:
                    startGmpOrImm("gmp");
                    break;
                case TAG_NO_IMMUNIZATION_CLICK:
                    ImageView checkImImm = ((View) v.getTag(R.string.child_immunization)).findViewById(R.id.check_im);
                    checkImImm.setImageResource(R.drawable.success);
                    checkImImm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    currentView = ((View) v.getTag(R.string.child_immunization));
                    setNoNeedStatusToList();
                    return;
            }
        }
        currentView = v;
    }

    private void startGmpOrImm(String type) {
        Bundle bundle = new Bundle();
        bundle.putString(ChildFollowupActivity.BASE_ENTITY_ID,childBaseEntityId);
        bundle.putSerializable(ChildFollowupActivity.COMMON_PERSON,commonPersonObjectClient);

        if(Objects.equals(type, "im")){
            long day = FormApplicability.getDay(commonPersonObjectClient);

            //means greater than 24 month
            boolean isOnlyVacc = day > 730;
            bundle.putBoolean(ChildFollowupActivity.IS_ONLY_SERVICE,isOnlyVacc);
            ChildVaccinationActivity.startChildVaccinationActivity(getActivity(),bundle,commonPersonObjectClient,isOnlyVacc);
        }else {
            ChildGMPActivity.startGMPActivity(getActivity(),bundle,commonPersonObjectClient);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    public void openServiceForms(String formName) {
        startAnyFormActivity(formName, REQUEST_HOME_VISIT);
    }

    public void openEnc() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.ENC_REGISTRATION, REQUEST_HOME_VISIT);
    }

    public void openRefereal() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_REFERRAL, REQUEST_HOME_VISIT);
    }

   /* public void openFollowUp() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_FOLLOWUP,REQUEST_HOME_VISIT);
    }*/

    public void openFollowUpByType(String type) {
        startAnyFormActivity(type, REQUEST_HOME_VISIT);
    }

    public void openReferealFollowUp(ReferralFollowUpModel referralFollowUpModel) {
        HnppConstants.getGPSLocation(((HouseHoldVisitActivity) getActivity()), new OnPostDataWithGps() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    if (TextUtils.isEmpty(childBaseEntityId)) {
                        Toast.makeText(getActivity(), "baseentityid null", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "openReferealFollowUp>>childBaseEntityId:" + childBaseEntityId);

                    JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
                    jsonForm.put(JsonFormUtils.ENTITY_ID, childBaseEntityId);
                    try {
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
                    } catch (Exception e) {
                        e.printStackTrace();
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
                    if (HnppConstants.isPALogin()) {
                        form.setHideSaveLabel(true);
                        form.setSaveLabel("");
                    }
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
                    getActivity().startActivityForResult(intent, REQUEST_HOME_VISIT);

                } catch (Exception e) {

                }
            }
        });


    }

    public void startAnyFormActivity(String formName, int requestCode) {
        Member member = getArguments().getParcelable(HnppConstants.MEMBER);
        MemberObject memberObject = new MemberObject(commonPersonObjectClient);

        if (!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))) {
            HnppConstants.showOneButtonDialog(getActivity(), getString(R.string.dialog_stock_sell_end), "");
            return;
        }
        HnppConstants.getGPSLocation(((HouseHoldVisitActivity) getActivity()), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    if (TextUtils.isEmpty(childBaseEntityId)) {
                        Toast.makeText(getActivity(), "baseentityid null", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "open form>>childBaseEntityId:" + childBaseEntityId + ":formName:" + formName);

                    JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(formName);
                    try {
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        HnppJsonFormUtils.addAddToStockValue(jsonForm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (HnppConstants.JSON_FORMS.IYCF_PACKAGE.equalsIgnoreCase(formName)) {
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        updateFormField(jsonArray, "dob", dobFormate);
                        String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                        updateFormField(jsonArray, "weight", birthWeight);
                    }
                    /*else if(HnppConstants.JSON_FORMS.CHILD_FOLLOWUP.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(childBaseEntityId+"_SOLID_FOOD");
                        if(!TextUtils.isEmpty(prevalue)){
                            updateFormField(jsonArray,"solid_food_month",prevalue);
                            JSONObject solidObj = getFieldJSONObject(jsonArray, "solid_food_month");
                            solidObj.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
                        }
                        updateFormField(jsonArray,"dob",dobFormate);
                    }*/
                   /* else if(HnppConstants.JSON_FORMS.CHILD_INFO_7_24_MONTHS.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);

                        updateFormField(jsonArray,"dob",dobFormate);
                    }*/
                    if (formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.BLOOD_TEST)) {
                        assert member != null;
                        if (member.getGender().equalsIgnoreCase("F")) {
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm, "is_women", "true");
                        }
                    }
                    jsonForm.put(JsonFormUtils.ENTITY_ID, memberObject.getFamilyHead());
                    Intent intent = new Intent(getActivity(), HnppAncJsonFormActivity.class);
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
        });


    }

    public void openCoronaIndividualForm() {
        Intent intent = new Intent(getActivity(), HnppAncJsonFormActivity.class);
        try {
            JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL);
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
            if (HnppConstants.isPALogin()) {
                form.setHideSaveLabel(true);
                form.setSaveLabel("");
            }
            getActivity().startActivityForResult(intent, REQUEST_HOME_VISIT);

        } catch (Exception e) {

        }
    }

    boolean isProcessing = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        HouseHoldVisitActivity activity = ((HouseHoldVisitActivity) getActivity());

        if (TextUtils.isEmpty(childBaseEntityId)) {
            Toast.makeText(getActivity(), "baseentityid null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            HnppConstants.isViewRefresh = true;
            if (data != null && data.getBooleanExtra("VACCINE_TAKEN", false)) {

                appExecutors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            NCUtils.startClientProcessing();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


                VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);

            }

        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT) {
            if (isProcessing) return;
            AtomicInteger isSave = new AtomicInteger(2);
            activity.showProgressDialog(R.string.please_wait_message);

            isProcessing = true;
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
            String visitId = JsonFormUtils.generateRandomUUIDString();
            HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:" + childBaseEntityId + ":formSubmissionId:" + formSubmissionId);

            processVisitFormAndSave(jsonString, formSubmissionId, visitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer integer) {
                            isSave.set(integer);
                        }

                        @Override
                        public void onError(Throwable e) {
                            activity.hideProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            if (isSave.get() == 1) {
                                //serviceList.get(currentPosition).setStatus(true);
                                updateCurrentView(true);
                                activity.hideProgressDialog();
                                showServiceDoneDialog(1);
                            } else if (isSave.get() == 3) {
                                activity.hideProgressDialog();
                                showServiceDoneDialog(3);
                            } else {
                                activity.hideProgressDialog();
                                //showServiceDoneDialog(false);
                            }
                        }
                    });

        }

        //handling gmp submission status
        else  if(resultCode == ChildGMPActivity.GMP_RESULT_CODE){
            if (data != null && data.getBooleanExtra("GMP_TAKEN", false)) {
                updateCurrentView(true);
            }
        }
        //handling vaccine submission status
        else if(resultCode == ChildVaccinationActivity.VACCINE_RESULT_CODE){
            if(data != null){
               boolean isImmunizationTaken = data.getBooleanExtra("VACCINE_TAKEN", false);
                updateCurrentView(isImmunizationTaken);
            }
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT) {
            //if(mViewPager!=null) mViewPager.setCurrentItem(0,true);
        } else if (resultCode == Activity.RESULT_OK && requestCode == ChildVaccinationActivity.VACCINE_REQUEST_CODE) {
            //profileMemberFragment.setUserVisibleHint(true);
        } else if (resultCode == Activity.RESULT_OK && requestCode == HnppConstants.SURVEY_KEY.MM_SURVEY_REQUEST_CODE) {
          /*  if(processSurveyResponse(data)){
                Toast.makeText(this,"Survey done",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"Fail to Survey",Toast.LENGTH_SHORT).show();
            }*/


        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void updateCurrentView(boolean status) {
        ImageView checkIm = currentView.findViewById(R.id.check_im);
        if(status){
            setStatusToList();
            checkIm.setImageResource(R.drawable.success);
            checkIm.setColorFilter(ContextCompat.getColor(getActivity(), R.color.others));
            View buttonView = (View) currentView.findViewById(R.id.noNeedBt);
            buttonView.setClickable(true);
            buttonView.setEnabled(false);

            currentView.setClickable(false);
            currentView.setEnabled(false);
        }else {
            setNoNeedStatusToList();
            checkIm.setImageResource(R.drawable.success);
            checkIm.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
            View buttonView = (View) currentView.findViewById(R.id.noNeedBt);
            buttonView.setClickable(false);
            buttonView.setEnabled(false);

            currentView.setClickable(true);
            currentView.setEnabled(true);
        }

    }

    private void setStatusToList() {
        for (ChildService childService : serviceList) {
            if (childService.getView().equals(currentView)) {
                childService.setStatus(1);
                addDataToDb(childService);
            }
        }
    }

    private void setNoNeedStatusToList() {
        for (ChildService childService : serviceList) {
            if (childService.getView().equals(currentView)) {
                childService.setStatus(2);
                addDataToDb(childService);
            }
        }
    }

    private Observable<Integer> processVisitFormAndSave(String jsonString, String formSubmissionid, String visitId) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(childBaseEntityId)) e.onNext(2);
            try {
                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                String type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);
                Map<String, String> jsonStrings = new HashMap<>();
                jsonStrings.put("First", form.toString());
                HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:" + childBaseEntityId + ":type:" + type);

                Visit visit = HnppJsonFormUtils.saveVisit(false, false, false, "", childBaseEntityId, type, jsonStrings, "", formSubmissionid, visitId);
                if (visit != null && !visit.getVisitId().equals("0")) {
                    HnppHomeVisitIntentService.processVisits();
                    FormParser.processVisitLog(visit);
                    HnppConstants.appendLog("SAVE_VISITSAVE_VISIT", "processVisitLog done:" + formSubmissionid + ":type:" + type);

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
                HnppConstants.appendLog("SAVE_VISIT", "exception processVisitFormAndSave >>" + ex.getMessage());
                e.onNext(1);
                e.onComplete();
            }
        });
    }

    Dialog dialog;

    private void showServiceDoneDialog(Integer isSuccess) {
        if (dialog != null) return;
        dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess == 1 ? "সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে" : isSuccess == 3 ? "সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে" : "সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                isProcessing = false;
            }
        });
        dialog.show();

    }

    public int listValidation() {
        //1-> success
        //2-> no need
        //3-> no input given
        int status = 1;
        int countSucc = 0;
        int countIsCheck = 0;
        if (serviceList.size() == 1) {
            if (serviceList.get(0).getTag() == HnppMemberProfileInteractor.TAG_OPEN_REFEREAL) {
                return serviceList.get(0).getStatus();
            }
        } else {
            for (ChildService data : serviceList) {
                if (data.getTag() != HnppMemberProfileInteractor.TAG_OPEN_REFEREAL) {
                    if (data.getStatus() < 3) {
                        countSucc++;
                    }
                }
                if (data.getStatus() == 3) {
                    countIsCheck++;
                }
            }
        }
        if (countIsCheck == serviceList.size()) return 3; //no data added
        else if (countSucc < serviceList.size() - 1 && countSucc > 0) return 2; //some data added
        else if (countSucc == 0) return 3; //no data added

        return status;
    }
}